package br.edu.ifes.deliveryworkers.workers;

import java.util.Map;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Worker responsável por processar tarefas externas do tipo "cotacao-moto".
 *
 * Este worker se conecta ao Camunda 7 via REST e fica "escutando" (polling)
 * tarefas que tenham o tópico (topic) "cotacao-moto".
 *
 * Quando uma tarefa é encontrada, ele:
 * 1. Lê a variável de entrada "distanciaKm"
 * 2. Calcula o valor da cotação usando a fórmula:
 *      V = 20 + D * 0.5
 * 3. Retorna o resultado para o processo através da variável "valorCotacao"
 *
 * Importante:
 * - Este worker NÃO é chamado diretamente pelo Camunda.
 * - Ele busca tarefas ativamente (modelo PULL).
 */
@Component // Indica que esta classe é gerenciada pelo Spring
public class MotoWorker {

    // Cliente responsável por se comunicar com o Camunda via REST
    private final ExternalTaskClient client;

    /**
     * Injeção de dependência do ExternalTaskClient.
     * O Spring automaticamente fornece a instância configurada.
     */
    public MotoWorker(ExternalTaskClient client) {
        this.client = client;
    }

    /**
     * Método executado automaticamente após a inicialização do bean.
     *
     * Aqui registramos (subscribe) este worker no tópico "cotacao-moto".
     * A partir deste momento, o worker começa a buscar tarefas no Camunda.
     */
    @PostConstruct
    public void subscribe() {
    	
    	System.out.println("Worker Moto ativo");
        client.subscribe("cotacao-moto") // Nome do tópico definido no BPMN
                .lockDuration(10000)    // Tempo de lock da tarefa (em ms)

                /**
                 * Handler que será executado quando uma tarefa for recebida.
                 */
                .handler((externalTask, externalTaskService) -> {

                    // Leitura da variável de entrada do processo
                    Double distanciaKm = externalTask.getVariable("distanciaKm");

                    // Regra de negócio (cálculo da cotação)
                    double valorCotacao = 20 + distanciaKm * 0.5;

                    /**
                     * Completa a tarefa no Camunda, enviando o resultado.
                     * O processo continua a partir daqui.
                     */
                    externalTaskService.complete(
                            externalTask,
                            Map.of("valorCotacao", valorCotacao)
                    );

                    // Logs para fins didáticos
                    System.out.println("=== Worker Moto ===");
                    System.out.println("Distância: " + distanciaKm + " km");
                    System.out.println("Valor calculado: R$ " + valorCotacao);
                }).open();

                // Inicia a inscrição no tópico
                
    }
}