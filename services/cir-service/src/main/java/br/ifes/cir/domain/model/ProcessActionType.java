package br.ifes.cir.domain.model;

/**
 * Enumeração que define os tipos de ação que o CIR pode executar
 * em um motor de processo (ex: Camunda).
 *
 * <p>Essas ações representam duas formas clássicas de interação
 * com um engine BPM:</p>
 *
 * <ul>
 *   <li><b>START_PROCESS</b>: iniciar uma nova instância de processo</li>
 *   <li><b>CORRELATE_MESSAGE</b>: correlacionar um evento/mensagem
 *       com uma instância de processo já existente</li>
 * </ul>
 *
 * <p>Essa enumeração permite desacoplar a decisão de negócio
 * da implementação técnica do engine.</p>
 */
public enum ProcessActionType {

    /**
     * Indica que deve ser iniciada uma nova instância de processo.
     */
    START_PROCESS,

    /**
     * Indica que deve ser feita a correlação de uma mensagem
     * com uma instância já existente.
     */
    CORRELATE_MESSAGE
}