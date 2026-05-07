package br.ifes.cir.domain.dispatch;

import org.springframework.stereotype.Component;

import br.ifes.cir.domain.handler.HandlerResult;
import br.ifes.cir.domain.handler.VinculacaoHandler;
import br.ifes.cir.domain.model.CirIdentifiedEvent;

/**
 * Responsável por rotear eventos identificados
 * para seus respectivos handlers.
 */
@Component
public class EventDispatcher {

    private final VinculacaoHandler vinculacaoHandler;

    public EventDispatcher(VinculacaoHandler vinculacaoHandler) {
        this.vinculacaoHandler = vinculacaoHandler;
    }

    public HandlerResult dispatch(CirIdentifiedEvent event) {

        if ("VINCULACAO_RECEBIDA".equals(event.getEventType())) {
            return vinculacaoHandler.handle(event);
        }

        return HandlerResult.failure("Tipo de evento não suportado: " + event.getEventType());
    }
}