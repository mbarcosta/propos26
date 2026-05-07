package br.ifes.cir.domain.handler;

import org.springframework.stereotype.Component;

import br.ifes.cir.domain.model.CirIdentifiedEvent;

/**
 * Handler do evento VINCULACAO_RECEBIDA.
 *
 * <p>Responsável apenas pela lógica de negócio.
 * NÃO deve chamar o Camunda.</p>
 */
@Component
public class VinculacaoHandler {

    public HandlerResult handle(CirIdentifiedEvent event) {
        try {
            /*
             * Aqui fica apenas regra de negócio.
             * (por enquanto não há lógica complexa)
             */

            return HandlerResult.success();

        } catch (Exception e) {
            return HandlerResult.failure(
                    "Erro no processamento da vinculação: " + e.getMessage()
            );
        }
    }
}