package br.ifes.cir.domain.store;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Armazena, em memória, os identificadores das mensagens que já foram
 * processadas pelo CIR.
 *
 * <p>Esta classe existe como uma solução simples e temporária para evitar
 * reprocessamento da mesma mensagem enquanto ainda não foi implementado
 * o fluxo definitivo de confirmação com o GMS, por exemplo por meio do
 * endpoint que move a mensagem para a caixa "Processed".</p>
 *
 * <p>Com isso, se o CIR executar novamente o polling e o GMS retornar
 * a mesma mensagem, o sistema poderá ignorá-la localmente, desde que
 * seu {@code messageId} já tenha sido registrado como processado.</p>
 *
 * <p><strong>Importante:</strong> esta solução é apenas um mecanismo
 * provisório para desenvolvimento e testes. Como os dados ficam apenas
 * em memória:
 * <ul>
 *   <li>eles são perdidos quando o serviço reinicia;</li>
 *   <li>não funcionam bem em múltiplas instâncias do CIR;</li>
 *   <li>não substituem um controle persistente ou o fluxo correto com o GMS.</li>
 * </ul>
 * </p>
 */
@Component
public class ProcessedMessageStore {

    /**
     * Conjunto de identificadores de mensagens já processadas.
     *
     * <p>O uso de {@link HashSet} permite consulta rápida para verificar
     * se uma mensagem já foi tratada anteriormente.</p>
     */
    private final Set<String> processedMessageIds = new HashSet<>();

    /**
     * Verifica se uma mensagem já foi processada anteriormente.
     *
     * @param messageId identificador técnico da mensagem
     * @return {@code true} se a mensagem já foi processada;
     *         {@code false} caso contrário
     */
    public boolean isProcessed(String messageId) {
        return processedMessageIds.contains(messageId);
    }

    /**
     * Marca uma mensagem como processada.
     *
     * <p>Após essa marcação, novas execuções do CIR poderão ignorar essa
     * mensagem, desde que ela volte a aparecer no polling do GMS.</p>
     *
     * @param messageId identificador técnico da mensagem
     */
    public void markAsProcessed(String messageId) {
        processedMessageIds.add(messageId);
    }
}