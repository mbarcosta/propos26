package br.ifes.cir.domain.handler;

/**
 * Representa o resultado da execução de um handler de evento.
 *
 * <p>Essa classe é fundamental para garantir consistência no fluxo,
 * pois permite ao CIR decidir se deve ou não confirmar o processamento
 * de uma mensagem no GMS.</p>
 *
 * <p>Regras:</p>
 * <ul>
 *   <li>success = true → mensagem pode ser marcada como processada</li>
 *   <li>success = false → mensagem deve permanecer na fila para retry</li>
 * </ul>
 */
public class HandlerResult {

    /**
     * Indica se o processamento foi bem-sucedido.
     */
    private boolean success;

    /**
     * Mensagem de erro (caso exista).
     */
    private String message;

    /**
     * Cria um resultado de sucesso.
     *
     * @return instância com success=true
     */
    public static HandlerResult success() {
        return new HandlerResult(true, null);
    }

    /**
     * Cria um resultado de falha.
     *
     * @param message descrição da falha
     * @return instância com success=false
     */
    public static HandlerResult failure(String message) {
        return new HandlerResult(false, message);
    }

    /**
     * Construtor privado (uso via métodos estáticos).
     */
    private HandlerResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}