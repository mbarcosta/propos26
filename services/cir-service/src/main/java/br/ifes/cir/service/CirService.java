package br.ifes.cir.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.ifes.cir.client.CamundaClient;
import br.ifes.cir.client.GmsClient;
import br.ifes.cir.client.dto.GmsPollResult;
import br.ifes.cir.client.dto.VariableValue;
import br.ifes.cir.domain.model.CirExecutionResult;
import br.ifes.cir.domain.rule.ClassifiedMessage;
import br.ifes.cir.domain.rule.MessageClassificationKind;
import br.ifes.cir.domain.rule.MessageEventClassifier;
import br.ifes.cir.domain.store.ProcessedMessageStore;

/**
 * Serviço principal do CIR.
 *
 * <p>Nesta versão, o CIR atua como uma camada de entrada e encaminhamento
 * de mensagens para o Camunda. Ele não conhece o fluxo interno dos processos
 * BPMN, nem executa lógica específica de Vinculação, Defesa ou qualquer
 * outro processo.</p>
 *
 * <p>Seu papel passa a ser estritamente este:</p>
 *
 * <ul>
 *   <li>consultar o GMS para obter novas mensagens da caixa postal;</li>
 *   <li>classificar as mensagens em categorias mínimas compreendidas
 *       pelo CIR: START, REPLY ou IRRELEVANT;</li>
 *   <li>encaminhar ao Camunda apenas as mensagens relevantes;</li>
 *   <li>solicitar ao GMS a movimentação para {@code Processed}
 *       apenas após encaminhamento bem-sucedido;</li>
 *   <li>registrar localmente que a mensagem já foi processada.</li>
 * </ul>
 *
 * <p>O princípio arquitetural importante é:
 * o CIR classifica e encaminha;
 * o Camunda interpreta, correlaciona e orquestra.</p>
 */
@Service
public class CirService {

    /**
     * Cliente de comunicação com o GMS.
     *
     * <p>É usado para:</p>
     * <ul>
     *   <li>executar o polling da caixa postal de um binding;</li>
     *   <li>mover mensagens para a pasta {@code Processed}
     *       após sucesso no encaminhamento ao Camunda.</li>
     * </ul>
     */
    private final GmsClient gmsClient;

    /**
     * Classificador responsável por transformar mensagens lidas do GMS
     * em objetos {@link ClassifiedMessage}.
     *
     * <p>Esse classificador já embute a lógica mínima do CIR:
     * distinguir mensagens de início, respostas correlacionáveis
     * e mensagens irrelevantes.</p>
     */
    private final MessageEventClassifier classifier;

    /**
     * Cliente de integração REST com o Camunda.
     *
     * <p>É por meio dele que o CIR envia mensagens ao engine BPMN,
     * seja para iniciar novas instâncias, seja para correlacionar
     * respostas intermediárias.</p>
     */
    private final CamundaClient camundaClient;

    /**
     * Controle local provisório de mensagens já processadas.
     *
     * <p>Nesta fase, esse mecanismo complementa a movimentação
     * para {@code Processed}, ajudando a evitar reprocessamento local.</p>
     */
    private final ProcessedMessageStore store;

    /**
     * Construtor com injeção de dependências.
     *
     * @param gmsClient cliente de integração com o GMS
     * @param classifier classificador de mensagens
     * @param camundaClient cliente de integração com o Camunda
     * @param store controle local de mensagens processadas
     */
    public CirService(
            GmsClient gmsClient,
            MessageEventClassifier classifier,
            CamundaClient camundaClient,
            ProcessedMessageStore store) {

        this.gmsClient = gmsClient;
        this.classifier = classifier;
        this.camundaClient = camundaClient;
        this.store = store;
    }
    
    /**
     * Gera o correlationId para marcar as conversas de uma mesma instancia de process.
     * 
     */
    private String generateCorrelationId(String messageName) {
        long now = System.currentTimeMillis();

        if ("VINCULACAO_START".equals(messageName)) {
            return "VINC-" + now;
        }

        if ("DEFESA_START".equals(messageName)) {
            return "DEF-" + now;
        }

        return "MSG-" + now;
    }
    /**
     * Executa o ciclo principal do CIR para o binding informado.
     *
     * <p>Fluxo desta versão:</p>
     *
     * <ol>
     *   <li>executa polling no GMS;</li>
     *   <li>classifica as mensagens em START, REPLY ou IRRELEVANT;</li>
     *   <li>encaminha ao Camunda apenas START e REPLY;</li>
     *   <li>se o encaminhamento for bem-sucedido, move a mensagem
     *       para {@code Processed} via GMS;</li>
     *   <li>marca a mensagem localmente como processada.</li>
     * </ol>
     *
     * <p>Nesta fase, o CIR já preserva metadados importantes da mensagem,
     * como assunto, corpo, remetente e identificador, para que o Camunda
     * possa utilizá-los na correlação e no processamento do fluxo.</p>
     *
     * @param bindingId identificador do binding a ser processado
     * @return resultado consolidado da execução
     */
    public CirExecutionResult execute(String bindingId) {

        /*
         * ETAPA 1
         * Solicita ao GMS o polling da caixa postal associada ao binding.
         */
        GmsPollResult gmsResult = gmsClient.poll(bindingId);

        /*
         * ETAPA 2
         * Classifica as mensagens retornadas pelo GMS.
         *
         * O resultado agora não é mais um evento específico de processo,
         * mas sim uma representação genérica e desacoplada:
         * START, REPLY ou IRRELEVANT.
         */
        List<ClassifiedMessage> classifiedMessages =
                classifier.classify(gmsResult.getMessages());

        /*
         * ETAPA 3
         * Para cada mensagem classificada como relevante,
         * tenta encaminhá-la ao Camunda.
         */
        for (ClassifiedMessage message : classifiedMessages) {
            try {

                /*
                 * Monta o mapa base de variáveis no formato esperado pelo Camunda.
                 *
                 * Além das variáveis extraídas pelo classificador,
                 * o CIR preserva o binding de origem.
                 */
                Map<String, VariableValue> variables = camundaClient.newVariables();

                for (Map.Entry<String, Object> entry : message.getVariables().entrySet()) {
                    variables.put(entry.getKey(), toCamundaVariable(entry.getValue()));
                }

                variables.put("bindingId", camundaClient.stringVar(bindingId));

                /*
                 * Encaminhamento por tipo lógico da mensagem.
                 */
                if (message.getKind() == MessageClassificationKind.START) {
                	String correlationId = message.getCorrelationId();

                	    if (correlationId == null || correlationId.isBlank()) {
                	        correlationId = generateCorrelationId(message.getMessageName());
                	        variables.put("correlationId", camundaClient.stringVar(correlationId));
                	    }

                	    
                    /*
                     * Mensagem de início:
                     * o CIR envia ao Camunda o messageName específico do processo.
                     *
                     * Exemplo:
                     * - VINCULACAO_START
                     * - DEFESA_START
                     */
                    camundaClient.sendStartMessage(
                            message.getMessageName(),
                            message.getCorrelationId(),
                            variables
                    );

                } else if (message.getKind() == MessageClassificationKind.REPLY) {

                    /*
                     * Mensagem intermediária correlacionável:
                     * o CIR envia ao Camunda um messageName genérico,
                     * acompanhado da chave de correlação extraída da própria mensagem.
                     *
                     * O Camunda é quem decide em qual instância e em qual ponto
                     * do processo essa resposta deve ser entregue.
                     */
                    camundaClient.sendReplyMessage(
                            message.getMessageName(),
                            message.getCorrelationId(),
                            variables
                    );

                } else {

                    /*
                     * Mensagens irrelevantes não devem chegar aqui,
                     * pois o classificador já as filtra.
                     * Ainda assim, mantemos a proteção por clareza.
                     */
                    continue;
                }

                /*
                 * ETAPA 4
                 * Se o encaminhamento ao Camunda ocorreu sem exceção,
                 * a mensagem pode ser confirmada como processada.
                 */
                gmsClient.moveToProcessed(bindingId, message.getMessageId());
                store.markAsProcessed(message.getMessageId());

            } catch (Exception e) {

                /*
                 * Em caso de falha:
                 * - a mensagem não é movida para Processed;
                 * - a mensagem não é marcada localmente como processada.
                 *
                 * Isso preserva a possibilidade de nova tentativa futura.
                 */
                System.out.println(
                        "Erro ao encaminhar mensagem ao Camunda. messageId="
                                + message.getMessageId()
                                + ", kind="
                                + message.getKind()
                                + ", messageName="
                                + message.getMessageName()
                );
                e.printStackTrace();
            }
        }

        /*
         * ETAPA 5
         * Monta o resultado consolidado da execução.
         *
         * Observação:
         * esta implementação assume que o modelo de retorno do CIR
         * já foi ou será ajustado para refletir o novo tipo de saída.
         */
        CirExecutionResult result = new CirExecutionResult();
        result.setBindingId(gmsResult.getBindingId());
        result.setTotalRead(gmsResult.getTotalRead());
        result.setIdentifiedEvents(classifiedMessages);
        System.out.println("classifiedMessages.size() = " + classifiedMessages.size());
        result.setTotalEventsIdentified(classifiedMessages.size());
        

        return result;
    }

    /**
     * Converte um valor genérico para o formato de variável esperado pelo Camunda.
     *
     * <p>Nesta fase, o suporte foi simplificado para os tipos
     * mais comuns usados pelo CIR.</p>
     *
     * @param value valor original
     * @return variável no formato aceito pelo Camunda
     */
    private VariableValue toCamundaVariable(Object value) {
        if (value instanceof Boolean booleanValue) {
            return camundaClient.booleanVar(booleanValue);
        }

        return camundaClient.stringVar(value == null ? null : String.valueOf(value));
    }
}