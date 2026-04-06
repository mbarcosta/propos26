package br.ifes.cir.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.ifes.cir.client.GmsClient;
import br.ifes.cir.client.dto.GmsPollResult;
import br.ifes.cir.domain.handler.HandlerResult;
import br.ifes.cir.domain.handler.VinculacaoHandler;
import br.ifes.cir.domain.model.CirExecutionResult;
import br.ifes.cir.domain.model.CirIdentifiedEvent;
import br.ifes.cir.domain.rule.MessageEventClassifier;
import br.ifes.cir.domain.store.ProcessedMessageStore;

/**
 * Serviço principal do CIR.
 *
 * <p>Esta classe coordena o fluxo central de processamento do componente,
 * atuando como orquestradora entre:</p>
 *
 * <ul>
 *   <li>o GMS, que fornece as mensagens lidas da caixa postal;</li>
 *   <li>o classificador, que interpreta mensagens como eventos de negócio;</li>
 *   <li>os handlers, que executam ações específicas para cada tipo de evento;</li>
 *   <li>o próprio GMS novamente, para confirmar que uma mensagem já foi
 *       efetivamente consumida e pode ser movida para a pasta "Processed".</li>
 * </ul>
 *
 * <p>Fluxo executado nesta versão:</p>
 * <ol>
 *   <li>o CIR solicita ao GMS o polling de um binding;</li>
 *   <li>as mensagens retornadas são classificadas em eventos de negócio;</li>
 *   <li>cada evento identificado é tratado por seu handler correspondente;</li>
 *   <li>somente se o handler indicar sucesso, o CIR chama o GMS para mover
 *       a mensagem para a pasta "Processed";</li>
 *   <li>após a confirmação no GMS, a mensagem é marcada localmente como
 *       processada no controle em memória do CIR.</li>
 * </ol>
 *
 * <p>Essa ordem é importante porque evita confirmar como concluído um evento
 * que ainda não foi realmente processado com sucesso.</p>
 */
@Service
public class CirService {

    /**
     * Cliente responsável pela comunicação do CIR com o GMS.
     *
     * <p>É usado tanto para executar o polling quanto para confirmar
     * o processamento bem-sucedido de uma mensagem.</p>
     */
    private final GmsClient gmsClient;

    /**
     * Componente responsável por classificar mensagens em eventos de negócio.
     */
    private final MessageEventClassifier classifier;

    /**
     * Handler responsável pelo tratamento do evento de vinculação.
     */
    private final VinculacaoHandler vinculacaoHandler;

    /**
     * Controle local provisório de mensagens já processadas.
     *
     * <p>Nesta fase do projeto, este controle ainda é mantido em memória,
     * servindo como proteção adicional contra reprocessamento local.</p>
     */
    private final ProcessedMessageStore store;

    /**
     * Construtor com injeção de dependência.
     *
     * @param gmsClient cliente de integração com o GMS
     * @param classifier classificador de mensagens em eventos
     * @param vinculacaoHandler handler do caso de uso de vinculação
     * @param store controle local de mensagens processadas
     */
    public CirService(
            GmsClient gmsClient,
            MessageEventClassifier classifier,
            VinculacaoHandler vinculacaoHandler,
            ProcessedMessageStore store) {
        this.gmsClient = gmsClient;
        this.classifier = classifier;
        this.vinculacaoHandler = vinculacaoHandler;
        this.store = store;
    }

    /**
     * Executa o fluxo principal do CIR para o binding informado.
     *
     * <p>Este método representa a operação principal do componente.
     * Ele consulta o GMS, identifica eventos de negócio e tenta tratá-los
     * um a um.</p>
     *
     * <p>Regras de consistência aplicadas:</p>
     * <ul>
     *   <li>se o evento for identificado, mas o handler falhar, a mensagem
     *       não é movida para "Processed";</li>
     *   <li>se o handler tiver sucesso, a mensagem é então confirmada no GMS;</li>
     *   <li>somente após essa confirmação a mensagem é marcada localmente
     *       como processada.</li>
     * </ul>
     *
     * @param bindingId identificador do binding a ser processado
     * @return resultado consolidado da execução do CIR
     */
    public CirExecutionResult execute(String bindingId) {

        /*
         * Etapa 1:
         * Solicita ao GMS a execução do polling da caixa associada ao binding.
         *
         * O resultado inclui resumo operacional e a lista de mensagens lidas.
         */
        GmsPollResult gmsResult = gmsClient.poll(bindingId);

        /*
         * Etapa 2:
         * Classifica as mensagens retornadas pelo GMS em eventos de negócio
         * reconhecidos pelo CIR.
         *
         * Exemplo atual:
         * - assunto contendo "vinculacao" -> VINCULACAO_RECEBIDA
         */
        List<CirIdentifiedEvent> identifiedEvents =
                classifier.classify(gmsResult.getMessages());

        /*
         * Etapa 3:
         * Para cada evento identificado, executa o handler correspondente.
         *
         * Nesta versão, temos apenas o tratamento explícito do caso
         * VINCULACAO_RECEBIDA.
         */
        for (CirIdentifiedEvent event : identifiedEvents) {
            try {
                HandlerResult handlerResult = null;

                /*
                 * Seleciona e executa o tratamento apropriado para o tipo
                 * do evento identificado.
                 */
                if ("VINCULACAO_RECEBIDA".equals(event.getEventType())) {
                    handlerResult = vinculacaoHandler.handle(event);
                }

                /*
                 * Se não houver handler configurado para o tipo do evento,
                 * tratamos isso como falha explícita.
                 *
                 * Isso evita considerar como "sucesso" um evento que,
                 * na prática, não foi tratado.
                 */
                if (handlerResult == null) {
                    System.out.println(
                            "Nenhum handler configurado para eventType=" + event.getEventType()
                    );
                    continue;
                }

                /*
                 * Etapa 4:
                 * Só confirma a mensagem como processada se o handler
                 * tiver indicado sucesso explícito.
                 */
                if (handlerResult.isSuccess()) {

                    /*
                     * Confirma no GMS que a mensagem foi efetivamente
                     * consumida com sucesso e pode ser movida para
                     * a pasta "Processed".
                     */
                    gmsClient.moveToProcessed(bindingId, event.getMessageId());

                    /*
                     * Após a confirmação no GMS, registra localmente
                     * que a mensagem já foi processada pelo CIR.
                     */
                    store.markAsProcessed(event.getMessageId());

                } else {

                    /*
                     * Se o handler indicou falha, a mensagem não deve ser
                     * movida para "Processed" nem marcada localmente como
                     * concluída.
                     *
                     * Assim, ela poderá ser reprocessada futuramente.
                     */
                    System.out.println(
                            "Falha no processamento do evento. messageId="
                                    + event.getMessageId()
                                    + ", motivo="
                                    + handlerResult.getMessage()
                    );
                }

            } catch (Exception e) {

                /*
                 * Em caso de falha inesperada:
                 * - a mensagem não é movida para Processed;
                 * - a mensagem não é marcada localmente como processada.
                 *
                 * Isso preserva a possibilidade de nova tentativa futura.
                 */
                System.out.println("Erro ao processar mensagem: " + event.getMessageId());
                e.printStackTrace();
            }
        }

        /*
         * Etapa 5:
         * Monta o resultado consolidado da execução.
         *
         * Observação importante:
         * nesta versão, a lista identifiedEvents representa os eventos
         * identificados na execução, independentemente de terem sido
         * concluídos com sucesso ou não.
         *
         * Mais adiante, esse retorno pode ser refinado para separar:
         * - eventos identificados
         * - eventos processados com sucesso
         * - eventos com falha
         */
        CirExecutionResult result = new CirExecutionResult();
        result.setBindingId(gmsResult.getBindingId());
        result.setTotalRead(gmsResult.getTotalRead());
        result.setIdentifiedEvents(identifiedEvents);
        result.setTotalEventsIdentified(identifiedEvents.size());

        return result;
    }
}
