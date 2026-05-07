package br.ifes.cir.domain.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import br.ifes.cir.client.dto.GmsMessage;
import br.ifes.cir.domain.store.ProcessedMessageStore;

/**
 * Componente responsável por classificar mensagens retornadas pelo GMS
 * em categorias lógicas mínimas compreendidas pelo CIR.
 *
 * <p>Nesta arquitetura, o CIR não classifica mais mensagens em eventos
 * específicos de processo, como "VINCULACAO_RECEBIDA" ou
 * "RESPOSTA_COORDENADOR". Em vez disso, ele trabalha apenas com:</p>
 *
 * <ul>
 *   <li><b>START</b>: mensagem que inicia uma nova instância;</li>
 *   <li><b>REPLY</b>: mensagem intermediária correlacionável;</li>
 *   <li><b>IRRELEVANT</b>: mensagem que não deve ser encaminhada ao Camunda.</li>
 * </ul>
 *
 * <p>Com isso, o CIR permanece enxuto e desacoplado do fluxo interno
 * dos processos BPMN. Ele apenas identifica o tipo lógico da mensagem
 * e extrai os dados mínimos necessários para encaminhamento.</p>
 */
@Component
public class MessageEventClassifier {

    /**
     * Controle local provisório de mensagens já processadas.
     *
     * <p>É usado apenas como proteção contra reprocessamento local.</p>
     */
    private final ProcessedMessageStore store;

    /**
     * Padrão explícito para extração da chave de correlação.
     *
     * <p>Formato esperado:</p>
     *
     * <pre>
     * CORRELATION-ID: ABC-123
     * </pre>
     */
    private static final Pattern CORRELATION_ID_PATTERN =
            Pattern.compile("CORRELATION-ID\\s*:\\s*([A-Za-z0-9\\-_./]+)", Pattern.CASE_INSENSITIVE);

    /**
     * Padrão alternativo para extração de chave entre colchetes no assunto.
     *
     * <p>Exemplo:</p>
     *
     * <pre>
     * Re: Defesa [DEF-1775689212826]
     * </pre>
     */
    private static final Pattern BRACKET_ID_PATTERN =
            Pattern.compile("\\[([A-Za-z]+-[0-9]+)\\]");

    public MessageEventClassifier(ProcessedMessageStore store) {
        this.store = store;
    }

    /**
     * Classifica uma lista de mensagens vindas do GMS.
     *
     * <p>Fluxo aplicado a cada mensagem:</p>
     *
     * <ol>
     *   <li>ignora mensagens já processadas localmente;</li>
     *   <li>classifica a mensagem individualmente;</li>
     *   <li>retorna apenas mensagens relevantes ao CIR,
     *       isto é, START ou REPLY;</li>
     *   <li>mensagens IRRELEVANT são descartadas nesta etapa.</li>
     * </ol>
     *
     * @param messages mensagens lidas pelo GMS
     * @return lista de mensagens relevantes classificadas
     */
    public List<ClassifiedMessage> classify(List<GmsMessage> messages) {
        List<ClassifiedMessage> result = new ArrayList<>();

        if (messages == null) {
            return result;
        }

        for (GmsMessage message : messages) {

            /*
             * Proteção local contra reprocessamento.
             */
            if (store.isProcessed(message.getMessageId())) {
                continue;
            }

            ClassifiedMessage classified = classifySingleMessage(message);

            /*
             * O CIR só encaminha START e REPLY.
             * IRRELEVANT é descartada.
             */
            if (classified != null
                    && classified.getKind() != null
                    && classified.getKind() != MessageClassificationKind.IRRELEVANT) {
                result.add(classified);
            }
        }

        return result;
    }

    /**
     * Classifica uma única mensagem.
     *
     * <p>Regras desta versão:</p>
     *
     * <ol>
     *   <li>se houver chave de correlação no assunto ou no corpo,
     *       a mensagem é classificada como REPLY;</li>
     *   <li>se parecer reply (por exemplo, "Re:"), tentamos tratá-la
     *       prioritariamente como resposta, mesmo que o assunto contenha
     *       termos como "defesa" ou "vinculacao";</li>
     *   <li>se não houver chave e o assunto indicar uma solicitação nova,
     *       a mensagem é classificada como START;</li>
     *   <li>caso contrário, a mensagem é IRRELEVANT.</li>
     * </ol>
     *
     * @param message mensagem recebida do GMS
     * @return mensagem classificada
     */
    private ClassifiedMessage classifySingleMessage(GmsMessage message) {
        if (message == null) {
            return irrelevant(message);
        }

        String subject = defaultString(message.getSubject());
        String body = defaultString(message.getBody());
        String normalizedSubject = normalize(subject);

        /*
         * REGRA 1 — Detectar chave de correlação explicitamente.
         *
         * Se a chave existe, esta é uma mensagem REPLY,
         * independentemente de o assunto conter "defesa" ou "vinculacao".
         */
        String correlationId = extractCorrelationId(subject, body);
        if (correlationId != null) {
            return buildReplyMessage(message, correlationId);
        }

        /*
         * REGRA 2 — Se o assunto parece reply, tratamos com cautela.
         *
         * Isso evita que mensagens como "Re: Defesa" ou "Re: Vinculacao"
         * sejam classificadas como START apenas porque contêm palavras-chave.
         *
         * Sem chave de correlação, uma mensagem desse tipo não deve
         * iniciar novo processo automaticamente.
         */
        if (looksLikeReply(normalizedSubject)) {
            return irrelevant(message);
        }

        /*
         * REGRA 3 — Detectar mensagens de início de processo.
         *
         * Nesta etapa, as regras ainda são simples e explícitas por assunto.
         * Depois isso deve migrar para configuração externa.
         */
        if (normalizedSubject.contains("vinculacao")) {
            ClassifiedMessage classified = baseMessage(message);
            classified.setKind(MessageClassificationKind.START);
            classified.setMessageName("VINCULACAO_START");
            return classified;
        }

        if (normalizedSubject.contains("defesa")) {
            ClassifiedMessage classified = baseMessage(message);
            classified.setKind(MessageClassificationKind.START);
            classified.setMessageName("DEFESA_START");
            return classified;
        }
        if (normalizedSubject.contains("matricula")) {
            ClassifiedMessage classified = baseMessage(message);
            classified.setKind(MessageClassificationKind.START);
            classified.setMessageName("MATRICULA_START");
            return classified;
        }

        /*
         * REGRA 4 — Irrelevante
         */
        return irrelevant(message);
    }

    /**
     * Constrói uma mensagem classificada como REPLY.
     *
     * @param message mensagem original
     * @param correlationId chave de correlação encontrada
     * @return mensagem classificada como REPLY
     */
    private ClassifiedMessage buildReplyMessage(GmsMessage message, String correlationId) {
        ClassifiedMessage classified = baseMessage(message);
        classified.setKind(MessageClassificationKind.REPLY);
        classified.setMessageName("EMAIL_REPLY");
        classified.setCorrelationId(correlationId);
        classified.addVariable("correlationId", correlationId);
        return classified;
    }

    /**
     * Cria um objeto base de mensagem classificada,
     * copiando os dados essenciais do e-mail original.
     *
     * <p>Esses dados são preservados para que o Camunda possa utilizá-los
     * na correlação e na interpretação do conteúdo da mensagem.</p>
     *
     * @param message mensagem original do GMS
     * @return mensagem classificada base
     */
    private ClassifiedMessage baseMessage(GmsMessage message) {
        ClassifiedMessage classified = new ClassifiedMessage();

        classified.setMessageId(message.getMessageId());
        classified.setFrom(message.getFrom());
        classified.setSubject(message.getSubject());
        classified.setBody(message.getBody());

        /*
         * Variáveis básicas preservadas para o Camunda.
         */
        classified.addVariable("messageId", message.getMessageId());
        classified.addVariable("from", message.getFrom());
        classified.addVariable("subject", message.getSubject());
        classified.addVariable("body", message.getBody());
        classified.addVariable("hasAttachments", message.isHasAttachments());

        return classified;
    }

    /**
     * Cria um resultado classificado como IRRELEVANT.
     *
     * @param message mensagem original
     * @return objeto classificado como IRRELEVANT
     */
    private ClassifiedMessage irrelevant(GmsMessage message) {
        ClassifiedMessage classified = new ClassifiedMessage();
        classified.setKind(MessageClassificationKind.IRRELEVANT);

        if (message != null) {
            classified.setMessageId(message.getMessageId());
            classified.setFrom(message.getFrom());
            classified.setSubject(message.getSubject());
            classified.setBody(message.getBody());
        }

        return classified;
    }

    /**
     * Verifica se o assunto tem aparência típica de reply.
     *
     * <p>Exemplos:</p>
     * <ul>
     *   <li>Re: Defesa</li>
     *   <li>RES: Vinculacao</li>
     *   <li>Enc: ...</li>
     * </ul>
     *
     * @param normalizedSubject assunto já normalizado
     * @return true se o assunto parecer resposta/encaminhamento
     */
    private boolean looksLikeReply(String normalizedSubject) {
        return normalizedSubject.startsWith("re:")
                || normalizedSubject.startsWith("res:")
                || normalizedSubject.startsWith("enc:");
    }

    /**
     * Tenta extrair a chave de correlação do assunto ou do corpo.
     *
     * <p>Prioridades:</p>
     * <ol>
     *   <li>procura no assunto por {@code CORRELATION-ID: ...};</li>
     *   <li>procura no assunto por identificador entre colchetes;</li>
     *   <li>procura no corpo por {@code CORRELATION-ID: ...}.</li>
     * </ol>
     *
     * @param subject assunto do e-mail
     * @param body corpo do e-mail
     * @return valor da chave de correlação, ou {@code null}
     */
    private String extractCorrelationId(String subject, String body) {
        String fromSubject = extractCorrelationIdFromText(subject);
        if (fromSubject != null) {
            return fromSubject;
        }

        String fromBracket = extractBracketId(subject);
        if (fromBracket != null) {
            return fromBracket;
        }

        return extractCorrelationIdFromText(body);
    }

    /**
     * Extrai uma chave de correlação do padrão textual explícito
     * {@code CORRELATION-ID: valor}.
     *
     * @param text texto a ser inspecionado
     * @return chave encontrada, ou {@code null}
     */
    private String extractCorrelationIdFromText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        Matcher matcher = CORRELATION_ID_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Extrai uma chave de correlação entre colchetes no assunto.
     *
     * <p>Exemplo:</p>
     * <pre>
     * Re: Defesa [DEF-1775689212826]
     * </pre>
     *
     * @param text texto a ser inspecionado
     * @return identificador encontrado, ou {@code null}
     */
    private String extractBracketId(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        Matcher matcher = BRACKET_ID_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Normaliza texto para comparações simples.
     *
     * <p>A normalização atual:</p>
     * <ul>
     *   <li>converte para minúsculas;</li>
     *   <li>remove acentuação manualmente em casos comuns.</li>
     * </ul>
     *
     * @param value texto original
     * @return texto normalizado
     */
    private String normalize(String value) {
        return defaultString(value)
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

    /**
     * Substitui {@code null} por string vazia.
     *
     * @param value valor original
     * @return valor não nulo
     */
    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}