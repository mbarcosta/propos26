package br.ifes.cir.domain.dispatch;

import org.springframework.stereotype.Component;

import br.ifes.cir.domain.handler.HandlerResult;
import br.ifes.cir.domain.handler.VinculacaoEventHandler;
import br.ifes.cir.domain.model.CirIdentifiedEvent;

/**
 * Componente responsável por encaminhar eventos identificados
 * para seus respectivos handlers.
 *
 * <p>Este componente remove do serviço principal (CirService)
 * a responsabilidade de decidir qual handler executar.</p>
 *
 * <p>Isso melhora:</p>
 * <ul>
 *   <li>modularidade</li>
 *   <li>extensibilidade</li>
 *   <li>legibilidade do fluxo principal</li>
 * </ul>
 */
@Component
public class EventDispatcher {

    private final VinculacaoEventHandler vinculacaoEventHandler;

    /**
     * Construtor com injeção de dependência.
     *
     * @param vinculacaoEventHandler handler de vinculação
     */
    public EventDispatcher(VinculacaoEventHandler vinculacaoEventHandler) {
        this.vinculacaoEventHandler = vinculacaoEventHandler;
    }

    /**
     * Encaminha o evento para o handler apropriado.
     *
     * @param event evento identificado
     * @return resultado do processamento
     */
    public HandlerResult dispatch(CirIdentifiedEvent event) {

        /*
         * Seleção simples baseada no tipo do evento.
         * Pode evoluir para estratégia/mapa no futuro.
         */
        if ("VINCULACAO_RECEBIDA".equals(event.getEventType())) {
            return vinculacaoEventHandler.handle(event);
        }

        /*
         * Caso não exista handler para o evento.
         */
        return HandlerResult.failure(
                "Nenhum handler configurado para eventType=" + event.getEventType()
        );
    }
}