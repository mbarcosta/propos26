package br.ifes.cir.domain.handler;

import org.springframework.stereotype.Component;

import br.ifes.cir.domain.model.CirIdentifiedEvent;

/**
 * Componente responsável por tratar eventos de negócio do tipo
 * {@code VINCULACAO_RECEBIDA}.
 *
 * <p>Nesta fase inicial do projeto, o tratamento é apenas demonstrativo:
 * ele registra no console que um processo de vinculação seria iniciado.</p>
 *
 * <p>Mais adiante, esta classe poderá evoluir para:
 * <ul>
 *   <li>chamar o Camunda para iniciar um processo;</li>
 *   <li>correlacionar mensagem com uma instância existente;</li>
 *   <li>validar remetente, conteúdo e anexos;</li>
 *   <li>acionar o endpoint do GMS para mover a mensagem para "Processed".</li>
 * </ul>
 * </p>
 */
@Component
public class VinculacaoHandler {

    /**
     * Trata um evento de vinculação.
     *
     * <p>No momento, esta implementação apenas imprime informações no console,
     * funcionando como um mock da ação real que será conectada depois.</p>
     *
     * @param event evento identificado pelo CIR
     */
	public HandlerResult handle(CirIdentifiedEvent event) {
	    try {
	        System.out.println("PROCESSO DE VINCULACAO INICIADO");
	        
	        // lógica atual
	        
	        return HandlerResult.success();

	    } catch (Exception e) {
	        return HandlerResult.failure(e.getMessage());
	    }
	}
}