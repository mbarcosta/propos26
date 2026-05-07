package br.ifes.cir.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import br.ifes.cir.client.dto.CompleteExternalTaskRequest;
import br.ifes.cir.client.dto.FetchAndLockRequest;
import br.ifes.cir.client.dto.FetchAndLockTopic;
import br.ifes.cir.client.dto.MessageCorrelationRequest;
import br.ifes.cir.client.dto.VariableValue;

/**
 * Cliente responsável pela integração do CIR com a API REST do Camunda 7.
 *
 * <p>Este componente encapsula as operações técnicas de comunicação
 * com o engine BPMN, evitando que o restante do sistema precise lidar
 * diretamente com URLs, payloads JSON e detalhes específicos da API.</p>
 *
 * <p>Nesta fase da arquitetura, o CIR utiliza o CamundaClient para
 * dois tipos principais de encaminhamento:</p>
 *
 * <ul>
 *   <li><b>mensagens de início de processo</b>, que disparam novas instâncias;</li>
 *   <li><b>mensagens intermediárias correlacionadas</b>, que devem ser
 *       entregues a instâncias já existentes.</li>
 * </ul>
 *
 * <p>Além disso, esta classe preserva operações auxiliares já existentes,
 * como fetch and lock e complete de external tasks, mesmo que elas
 * não sejam o foco imediato desta etapa.</p>
 *
 * <p>Importante:
 * esta classe é técnica e genérica. Ela não deve conter lógica de negócio
 * de processos específicos como Vinculação ou Defesa.</p>
 */
@Component
public class CamundaClient {

    /**
     * Cliente HTTP utilizado para enviar requisições REST ao Camunda.
     */
    private final RestTemplate restTemplate;

    /**
     * URL base da API REST do Camunda.
     *
     * <p>Exemplo típico:</p>
     *
     * <pre>
     * http://localhost:8080/engine-rest
     * </pre>
     */
    @Value("${camunda.base-url}")
    private String baseUrl;

    public CamundaClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Envia ao Camunda uma mensagem de início de processo.
     *
     * <p>Este método é usado quando o CIR identificou uma mensagem de e-mail
     * que deve iniciar uma nova instância BPMN, por exemplo:</p>
     *
     * <ul>
     *   <li>{@code VINCULACAO_START}</li>
     *   <li>{@code DEFESA_START}</li>
     * </ul>
     *
     * <p>O parâmetro {@code businessKey} pode ser usado para atribuir
     * desde o início um identificador de negócio à instância criada.
     * Isso é útil quando já existe uma chave conhecida no momento do start.</p>
     *
     * <p>Se não houver uma chave apropriada ainda, o valor pode ser nulo.</p>
     *
     * @param messageName nome da mensagem BPMN de início
     * @param businessKey chave de negócio da instância, se disponível
     * @param variables variáveis a serem entregues ao processo
     */
    public void sendStartMessage(
            String messageName,
            String businessKey,
            Map<String, VariableValue> variables) {

        String url = baseUrl + "/message";

        MessageCorrelationRequest request = new MessageCorrelationRequest();
        request.setMessageName(messageName);
        request.setBusinessKey(businessKey);
        request.setProcessVariables(variables);

        restTemplate.postForObject(url, request, String.class);
    }

    /**
     * Envia ao Camunda uma mensagem intermediária correlacionada.
     *
     * <p>Este método é usado quando o CIR recebe uma resposta de e-mail
     * contendo uma chave de correlação explícita no assunto ou no corpo.
     * Nesse caso, o objetivo não é criar uma nova instância,
     * mas localizar uma instância já existente e entregar a ela
     * a mensagem recebida.</p>
     *
     * <p>Nesta arquitetura, o CIR não precisa saber qual processo está
     * esperando a resposta nem qual participante é o esperado.
     * Ele apenas extrai a chave de correlação e encaminha a mensagem.
     * O Camunda é quem decide se existe uma instância compatível
     * esperando esse evento.</p>
     *
     * <p>Exemplo típico:</p>
     *
     * <pre>
     * messageName  = EMAIL_REPLY
     * correlationId = ORI-2026-015
     * </pre>
     *
     * @param messageName nome da mensagem BPMN intermediária
     * @param correlationId chave de correlação extraída da mensagem
     * @param variables variáveis adicionais da mensagem encaminhada
     */
    public void sendReplyMessage(
            String messageName,
            String correlationId,
            Map<String, VariableValue> variables) {

        String url = baseUrl + "/message";

        MessageCorrelationRequest request = new MessageCorrelationRequest();
        request.setMessageName(messageName);
        request.setCorrelationKeys(buildCorrelationKeys(correlationId));
        request.setProcessVariables(variables);

        restTemplate.postForObject(url, request, String.class);
    }

    /**
     * Método de compatibilidade genérico para envio simples de mensagem.
     *
     * <p>Ele continua disponível por conveniência,
     * mas a preferência da nova arquitetura é usar explicitamente:</p>
     *
     * <ul>
     *   <li>{@link #sendStartMessage(String, String, Map)}</li>
     *   <li>{@link #sendReplyMessage(String, String, Map)}</li>
     * </ul>
     *
     * @param messageName nome da mensagem BPMN
     * @param variables variáveis a serem enviadas ao Camunda
     */
    public void sendMessage(String messageName, Map<String, VariableValue> variables) {
        String url = baseUrl + "/message";

        MessageCorrelationRequest request = new MessageCorrelationRequest();
        request.setMessageName(messageName);
        request.setProcessVariables(variables);

        restTemplate.postForObject(url, request, String.class);
    }

    /**
     * Constrói o mapa de correlationKeys no formato esperado pela API REST do Camunda.
     *
     * <p>Nesta fase, adotamos uma única chave de correlação chamada
     * {@code correlationId}. Mais adiante, esse modelo poderá ser
     * expandido se necessário.</p>
     *
     * @param correlationId valor da chave de correlação
     * @return mapa pronto para ser enviado ao Camunda
     */
    private Map<String, VariableValue> buildCorrelationKeys(String correlationId) {
        Map<String, VariableValue> keys = new HashMap<>();
        keys.put("correlationId", stringVar(correlationId));
        return keys;
    }

    /**
     * Realiza a operação de fetch and lock sobre external tasks.
     *
     * <p>Embora não seja o foco imediato desta etapa,
     * esta operação permanece disponível para futuras integrações
     * com workers externos.</p>
     *
     * @param workerId identificador do worker
     * @param topicName nome do tópico da external task
     * @param maxTasks quantidade máxima de tarefas
     * @param lockDuration duração do lock em milissegundos
     * @return resposta bruta do Camunda
     */
    public String fetchAndLock(String workerId, String topicName, int maxTasks, long lockDuration) {
        String url = baseUrl + "/external-task/fetchAndLock";

        FetchAndLockTopic topic = new FetchAndLockTopic();
        topic.setTopicName(topicName);
        topic.setLockDuration(lockDuration);

        FetchAndLockRequest request = new FetchAndLockRequest();
        request.setWorkerId(workerId);
        request.setMaxTasks(maxTasks);
        request.setTopics(List.of(topic));

        return restTemplate.postForObject(url, request, String.class);
    }

    /**
     * Conclui uma external task previamente bloqueada.
     *
     * @param externalTaskId identificador da tarefa externa
     * @param workerId identificador do worker que realizou o lock
     */
    public void completeExternalTask(String externalTaskId, String workerId) {
        String url = baseUrl + "/external-task/" + externalTaskId + "/complete";

        CompleteExternalTaskRequest request = new CompleteExternalTaskRequest();
        request.setWorkerId(workerId);

        restTemplate.postForObject(url, request, String.class);
    }

    /**
     * Cria uma variável String no formato esperado pelo Camunda.
     *
     * @param value valor textual
     * @return variável do tipo String
     */
    public VariableValue stringVar(String value) {
        return new VariableValue(value, "String");
    }

    /**
     * Cria uma variável Boolean no formato esperado pelo Camunda.
     *
     * @param value valor booleano
     * @return variável do tipo Boolean
     */
    public VariableValue booleanVar(Boolean value) {
        return new VariableValue(value, "Boolean");
    }

    /**
     * Cria um novo mapa mutável de variáveis.
     *
     * @return mapa vazio de variáveis
     */
    public Map<String, VariableValue> newVariables() {
        return new HashMap<>();
    }
}