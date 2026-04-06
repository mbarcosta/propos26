package br.ifes.cir.domain.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import br.ifes.cir.client.dto.GmsMessage;
import br.ifes.cir.domain.model.CirIdentifiedEvent;
import br.ifes.cir.domain.store.ProcessedMessageStore;

/**
 * Componente responsável por classificar mensagens retornadas pelo GMS
 * em eventos de negócio reconhecidos pelo CIR.
 *
 * <p>Nesta versão, o classificador possui uma responsabilidade mais limpa:
 * ele apenas interpreta semanticamente as mensagens, sem alterar o estado
 * de processamento delas.</p>
 *
 * <p>Em outras palavras:</p>
 *
 * <ul>
 *   <li>o classificador consulta o {@link ProcessedMessageStore}
 *       apenas para ignorar mensagens já processadas;</li>
 *   <li>mas ele não marca mensagens como processadas;</li>
 *   <li>essa decisão agora pertence à camada de serviço, após o
 *       processamento bem-sucedido do evento e a confirmação no GMS.</li>
 * </ul>
 *
 * <p>Essa mudança é importante porque evita considerar uma mensagem
 * como concluída antes da hora.</p>
 */
@Component
public class MessageEventClassifier {

    /**
     * Componente que mantém o controle local das mensagens já processadas.
     *
     * <p>Nesta fase do projeto, ele ainda funciona como um mecanismo
     * provisório em memória para evitar reprocessamento desnecessário.</p>
     */
    private final ProcessedMessageStore store;

    /**
     * Construtor com injeção de dependência.
     *
     * @param store componente que registra mensagens já processadas
     */
    public MessageEventClassifier(ProcessedMessageStore store) {
        this.store = store;
    }

    /**
     * Classifica uma lista de mensagens do GMS em eventos de negócio.
     *
     * <p>Para cada mensagem:
     * <ol>
     *   <li>verifica se ela já foi processada anteriormente;</li>
     *   <li>se não foi, tenta classificá-la semanticamente;</li>
     *   <li>se houver correspondência com uma regra conhecida,
     *       devolve o evento identificado.</li>
     * </ol>
     * </p>
     *
     * <p>Importante: esta classe não marca mensagens como processadas.
     * Ela apenas identifica eventos.</p>
     *
     * @param messages mensagens retornadas pelo GMS
     * @return lista de eventos de negócio identificados
     */
    public List<CirIdentifiedEvent> classify(List<GmsMessage> messages) {
        List<CirIdentifiedEvent> events = new ArrayList<>();

        if (messages == null) {
            return events;
        }

        for (GmsMessage message : messages) {

            /*
             * Se a mensagem já foi processada em uma execução anterior,
             * ela é ignorada para evitar duplicidade local.
             */
            if (store.isProcessed(message.getMessageId())) {
                continue;
            }

            /*
             * Tenta classificar semanticamente a mensagem.
             */
            CirIdentifiedEvent event = classifySingleMessage(message);

            /*
             * Apenas eventos reconhecidos são adicionados ao resultado.
             * A marcação como processado NÃO acontece aqui.
             */
            if (event != null) {
                events.add(event);
            }
        }

        return events;
    }

    /**
     * Tenta classificar uma única mensagem em um evento de negócio.
     *
     * <p>Regra atual do exemplo:
     * se o assunto da mensagem contiver o termo {@code vinculacao},
     * o CIR entende que a mensagem representa um evento
     * {@code VINCULACAO_RECEBIDA}.</p>
     *
     * @param message mensagem a ser analisada
     * @return evento identificado, ou {@code null} se a mensagem não
     *         corresponder a nenhuma regra conhecida
     */
    private CirIdentifiedEvent classifySingleMessage(GmsMessage message) {
        if (message == null || message.getSubject() == null) {
            return null;
        }

        String normalizedSubject = normalize(message.getSubject());

        if (normalizedSubject.contains("vinculacao")) {
            CirIdentifiedEvent event = new CirIdentifiedEvent();
            event.setMessageId(message.getMessageId());
            event.setEventType("VINCULACAO_RECEBIDA");
            event.setFrom(message.getFrom());
            event.setSubject(message.getSubject());
            event.setHasAttachments(message.isHasAttachments());
            return event;
        }

        return null;
    }

    /**
     * Normaliza texto para facilitar comparações simples.
     *
     * <p>A normalização atual:
     * <ul>
     *   <li>converte para minúsculas;</li>
     *   <li>remove acentuação manualmente para os casos mais comuns.</li>
     * </ul>
     * </p>
     *
     * @param value texto original
     * @return texto normalizado
     */
    private String normalize(String value) {
        return value
                .toLowerCase(Locale.ROOT)
                .replace("á", "a")
                .replace("à", "a")
                .replace("â", "a")
                .replace("ã", "a")
                .replace("é", "e")
                .replace("ê", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ô", "o")
                .replace("õ", "o")
                .replace("ú", "u")
                .replace("ç", "c");
    }
}