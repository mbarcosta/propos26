package br.ifes.cir.domain.rule;

/**
 * Classificação lógica mínima usada pelo CIR para decidir
 * se uma mensagem deve ou não ser encaminhada ao Camunda.
 *
 * <p>O objetivo deste enum é reduzir o acoplamento do CIR
 * com processos específicos. Em vez de o CIR conhecer
 * Vinculação, Defesa, resposta do aluno, resposta do coordenador etc.,
 * ele passa a trabalhar apenas com três categorias gerais.</p>
 *
 * <ul>
 *   <li><b>START</b>: mensagem que inicia uma nova instância de processo;</li>
 *   <li><b>REPLY</b>: mensagem intermediária, normalmente correlacionável
 *       por chave presente no assunto ou no corpo;</li>
 *   <li><b>IRRELEVANT</b>: mensagem que não deve ser encaminhada ao Camunda.</li>
 * </ul>
 */
public enum MessageClassificationKind {

    /**
     * Mensagem de início de processo.
     *
     * <p>Exemplo:
     * uma nova solicitação de vinculação ou uma nova solicitação de defesa.</p>
     */
    START,

    /**
     * Mensagem intermediária ou resposta correlacionável.
     *
     * <p>Exemplo:
     * resposta a um e-mail previamente enviado por um processo já em execução.</p>
     */
    REPLY,

    /**
     * Mensagem irrelevante para o fluxo do CIR.
     *
     * <p>Exemplo:
     * spam, resposta sem padrão reconhecível, e-mails administrativos
     * sem relação com processos monitorados.</p>
     */
    IRRELEVANT
}