package br.edu.ifes.deliveryworkers.workers;

import java.util.Map;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Worker responsável por processar tarefas externas do tipo "cotacao-carro".
 *
 * Funcionamento:
 * - Escuta tarefas do tópico "cotacao-carro"
 * - Recebe a variável "distanciaKm"
 * - Calcula o valor com a fórmula:
 *      V = 20 + D * 1.5
 * - Retorna o resultado ao processo via "valorCotacao"
 *
 * Diferença em relação ao MotoWorker:
 * - Apenas a regra de cálculo muda
 * - O restante da arquitetura permanece idêntico
 *
 * Isso demonstra claramente o padrão:
 * 1 tópico → 1 worker → 1 regra de negócio
 */
@Component
public class CarroWorker {

    private final ExternalTaskClient client;

    /**
     * Construtor com injeção do cliente Camunda.
     */
    public CarroWorker(ExternalTaskClient client) {
        this.client = client;
    }

    /**
     * Registro do worker no tópico "cotacao-carro".
     *
     * Este método é executado automaticamente após a inicialização do Spring.
     */
    @PostConstruct
    public void subscribe() {

        client.subscribe("cotacao-carro") // Nome do tópico no BPMN
                .lockDuration(10000)

                .handler((externalTask, externalTaskService) -> {

                    // Recupera a variável enviada pelo processo
                    Double distanciaKm = externalTask.getVariable("distanciaKm");

                    // Regra de negócio específica para carro
                    double valorCotacao = 20 + distanciaKm * 1.5;

                    // Finaliza a tarefa e devolve o resultado ao Camunda
                    externalTaskService.complete(
                            externalTask,
                            Map.of("valorCotacao", valorCotacao)
                    );

                    // Logs para visualização do comportamento
                    System.out.println("=== Worker Carro ===");
                    System.out.println("Distância: " + distanciaKm + " km");
                    System.out.println("Valor calculado: R$ " + valorCotacao);
                })

                .open();
    }
}