package br.ifes.cir.domain.handler;

import org.springframework.stereotype.Component;

import br.ifes.cir.client.CamundaClient;
import br.ifes.cir.domain.model.CirIdentifiedEvent;

/**
 * Handler responsável pelo processamento do evento
 * VINCULACAO_RECEBIDA.
 *
 * <p>Este handler encapsula a lógica de reação ao evento,
 * incluindo a interação com o motor de processos.</p>
 */
@Component
public class VinculacaoEventHandler {

    private final CamundaClient camundaClient;

    /**
     * Construtor com injeção de dependência.
     *
     * @param camundaClient cliente de integração com o Camunda
     */
    public VinculacaoEventHandler(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    /**
     * Executa o tratamento do evento de vinculação.
     *
     * @param event evento identificado
     * @return resultado da execução
     */
    public HandlerResult handle(CirIdentifiedEvent event) {
        try {
            /*
             * Inicia o processo correspondente no Camunda.
             */
            camundaClient.startProcess("processo_vinculacao", event);

            return HandlerResult.success();

        } catch (Exception e) {
            return HandlerResult.failure(
                    "Erro ao iniciar processo de vinculação: " + e.getMessage()
            );
        }
    }
}