package br.ifes.email_gateway_service.service;

import br.ifes.email_gateway_service.model.EmailMessage;
import br.ifes.email_gateway_service.model.MailBinding;
import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.search.FlagTerm;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Serviço responsável por acessar uma caixa postal via IMAP
 * e converter as mensagens encontradas em objetos {@link EmailMessage}.
 *
 * <p>Na nova arquitetura do GMS, esta classe atua como
 * camada de leitura e transformação de e-mails.</p>
 *
 * <p>Suas responsabilidades são exclusivamente:</p>
 * <ul>
 *   <li>abrir conexão com a caixa postal definida em um {@link MailBinding}</li>
 *   <li>acessar a pasta de entrada configurada</li>
 *   <li>buscar mensagens conforme a política de polling</li>
 *   <li>extrair metadados e conteúdo relevantes das mensagens</li>
 *   <li>transformar cada mensagem em um objeto {@link EmailMessage}</li>
 * </ul>
 *
 * <p>Importante:</p>
 * <ul>
 *   <li>esta classe não aplica regras de negócio</li>
 *   <li>não decide para qual processo a mensagem deve ir</li>
 *   <li>não interage com Camunda</li>
 *   <li>não move mensagens para outras pastas</li>
 *   <li>não altera o estado das mensagens na caixa postal</li>
 * </ul>
 *
 * <p>Em outras palavras, este serviço apenas lê e padroniza as mensagens
 * recebidas, deixando a decisão de uso para camadas superiores.</p>
 */
@Service
public class MailReaderService {

    /**
     * Lê mensagens da caixa postal configurada no binding e as converte
     * em objetos {@link EmailMessage}.
     *
     * <p>O comportamento da leitura depende da configuração presente
     * no binding, como por exemplo:</p>
     * <ul>
     *   <li>protocolo, host, porta, usuário e senha</li>
     *   <li>pasta de entrada</li>
     *   <li>leitura apenas de mensagens não lidas</li>
     *   <li>limite máximo de mensagens por polling</li>
     *   <li>inclusão ou não do corpo textual da mensagem</li>
     * </ul>
     *
     * @param binding configuração da caixa postal a ser acessada
     * @return lista de mensagens lidas da caixa
     */
    public List<EmailMessage> readEmails(MailBinding binding) {
        List<EmailMessage> emails = new ArrayList<>();

        Store store = null;
        Folder inbox = null;

        try {
            // Monta as propriedades de conexão IMAP/IMAPS a partir do binding.
            Properties props = buildMailProperties(binding);

            // Usa o protocolo configurado no binding; se estiver ausente,
            // assume "imaps" como padrão.
            String protocol = binding.getMailServer().getProtocol() != null
                    ? binding.getMailServer().getProtocol()
                    : "imaps";

            // Cria a sessão Jakarta Mail.
            Session session = Session.getInstance(props);

            // Mantido em true por enquanto para facilitar depuração.
            // Futuramente isso pode virar configuração externa.
            session.setDebug(true);

            // Obtém o Store do protocolo configurado e conecta na caixa.
            store = session.getStore(protocol);
            store.connect(
                    binding.getMailServer().getHost(),
                    binding.getMailServer().getPort(),
                    binding.getMailServer().getUsername(),
                    binding.getMailServer().getPassword()
            );

            // Resolve o nome da pasta de entrada. Se não houver configuração,
            // usa "INBOX" como padrão.
            String inboxFolderName = resolveInboxFolder(binding);

            // Abre a pasta apenas para leitura, pois este serviço não deve
            // alterar o estado da caixa postal.
            inbox = store.getFolder(inboxFolderName);
            inbox.open(Folder.READ_ONLY);

            // Carrega as mensagens conforme a política do binding
            // (por exemplo: somente não lidas ou todas).
            Message[] messages = loadMessages(inbox, binding);

            // Lê o limite máximo de mensagens por polling.
            // Se vier 0 ou valor não configurado, considera sem limite.
            int maxMessages = resolveMaxMessages(binding);

            int count = 0;

            // Converte cada Message em EmailMessage.
            for (Message message : messages) {
                if (maxMessages > 0 && count >= maxMessages) {
                    break;
                }

                EmailMessage email = toEmailMessage(message, binding);
                emails.add(email);
                count++;
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler e-mails do binding " + binding.getId(), e);
        } finally {
            // Fecha a pasta, se ainda estiver aberta.
            try {
                if (inbox != null && inbox.isOpen()) {
                    inbox.close(false);
                }
            } catch (Exception ignored) {
            }

            // Fecha a conexão com o servidor, se ainda estiver ativa.
            try {
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (Exception ignored) {
            }
        }

        return emails;
    }

    /**
     * Monta as propriedades necessárias para conexão IMAP/IMAPS.
     *
     * <p>Estas propriedades são derivadas da configuração do binding.</p>
     *
     * @param binding binding com as informações do servidor de e-mail
     * @return propriedades de conexão para Jakarta Mail
     */
    private Properties buildMailProperties(MailBinding binding) {
        Properties props = new Properties();

        String protocol = binding.getMailServer().getProtocol() != null
                ? binding.getMailServer().getProtocol()
                : "imaps";

        String protocolPrefix = "mail." + protocol;

        props.put("mail.store.protocol", protocol);
        props.put(protocolPrefix + ".host", binding.getMailServer().getHost());
        props.put(protocolPrefix + ".port", String.valueOf(binding.getMailServer().getPort()));
        props.put(protocolPrefix + ".ssl.enable", "true");

        // Garante leitura sem marcar a mensagem como lida.
        props.put("mail.imap.peek", "true");
        props.put("mail.imaps.peek", "true");

        return props;
    }

    /**
     * Resolve a pasta de entrada a ser usada na leitura.
     *
     * <p>Se o binding não configurar uma pasta específica,
     * utiliza "INBOX" como padrão.</p>
     *
     * @param binding binding da caixa postal
     * @return nome da pasta de entrada
     */
    private String resolveInboxFolder(MailBinding binding) {
        if (binding.getFolders() != null
                && binding.getFolders().getInbox() != null
                && !binding.getFolders().getInbox().isBlank()) {
            return binding.getFolders().getInbox();
        }
        return "INBOX";
    }

    /**
     * Carrega as mensagens da pasta de entrada conforme a política de polling.
     *
     * <p>Se o binding estiver configurado para ler apenas mensagens não lidas,
     * aplica filtro por flag SEEN=false. Caso contrário, retorna todas
     * as mensagens da pasta.</p>
     *
     * @param inbox pasta de entrada já aberta
     * @param binding binding com a política de polling
     * @return array de mensagens lidas do servidor
     * @throws Exception em caso de erro de acesso à pasta
     */
    private Message[] loadMessages(Folder inbox, MailBinding binding) throws Exception {
        boolean onlyUnread = binding.getPollingPolicy() != null
                && binding.getPollingPolicy().isOnlyUnread();

        if (onlyUnread) {
            FlagTerm unseenFlag = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            return inbox.search(unseenFlag);
        }

        return inbox.getMessages();
    }

    /**
     * Resolve o limite máximo de mensagens que devem ser lidas em um polling.
     *
     * <p>Se não houver valor configurado, retorna 0, o que significa
     * "sem limite explícito".</p>
     *
     * @param binding binding com a política de polling
     * @return número máximo de mensagens a ler
     */
    private int resolveMaxMessages(MailBinding binding) {
        if (binding.getPollingPolicy() != null) {
            return binding.getPollingPolicy().getMaxMessagesPerPoll();
        }
        return 0;
    }

    /**
     * Informa se o corpo textual da mensagem deve ser incluído
     * no objeto {@link EmailMessage}.
     *
     * <p>Se a política de ingestão não estiver configurada,
     * assume-se que o corpo deve ser incluído.</p>
     *
     * @param binding binding com a política de ingestão
     * @return true se o corpo deve ser lido; false caso contrário
     */
    private boolean shouldIncludeBody(MailBinding binding) {
        return binding.getIngestionPolicy() == null
                || binding.getIngestionPolicy().isIncludeBody();
    }

    /**
     * Converte uma mensagem do Jakarta Mail em um objeto de domínio {@link EmailMessage}.
     *
     * <p>São extraídos os principais metadados e o corpo textual, incluindo:</p>
     * <ul>
     *   <li>Message-ID</li>
     *   <li>número da mensagem na pasta</li>
     *   <li>remetente</li>
     *   <li>destinatários TO e CC</li>
     *   <li>assunto</li>
     *   <li>corpo textual</li>
     *   <li>data/hora de recebimento</li>
     *   <li>In-Reply-To</li>
     *   <li>References</li>
     *   <li>indicador de existência de anexos</li>
     * </ul>
     *
     * @param message mensagem original do servidor
     * @param binding binding que controla a política de leitura
     * @return objeto de domínio simplificado
     */
    private EmailMessage toEmailMessage(Message message, MailBinding binding) {
        EmailMessage email = new EmailMessage();

        email.setMessageId(extractHeader(message, "Message-ID"));
        email.setMessageNumber(message.getMessageNumber());

        email.setFrom(extractFrom(message));
        email.setTo(extractRecipients(message, Message.RecipientType.TO));
        email.setCc(extractRecipients(message, Message.RecipientType.CC));

        email.setSubject(extractSubject(message));
        email.setBody(shouldIncludeBody(binding) ? extractBody(message) : "");

        email.setReceivedAt(extractReceivedAt(message));
        email.setInReplyTo(extractHeader(message, "In-Reply-To"));
        email.setReferences(extractReferences(message));
        email.setHasAttachments(detectAttachments(message));

        return email;
    }

    /**
     * Extrai o endereço de e-mail do remetente principal da mensagem.
     *
     * @param message mensagem lida via Jakarta Mail
     * @return endereço do remetente, ou string vazia se não encontrado
     */
    private String extractFrom(Message message) {
        try {
            Address[] fromAddresses = message.getFrom();

            if (fromAddresses != null && fromAddresses.length > 0) {
                Address address = fromAddresses[0];

                if (address instanceof InternetAddress internetAddress) {
                    return internetAddress.getAddress() != null
                            ? internetAddress.getAddress()
                            : "";
                }

                return address.toString();
            }
        } catch (Exception ignored) {
        }

        return "";
    }

    /**
     * Extrai os destinatários do tipo informado (TO, CC, etc.).
     *
     * @param message mensagem lida via Jakarta Mail
     * @param type tipo de destinatário a extrair
     * @return lista de endereços extraídos
     */
    private List<String> extractRecipients(Message message, Message.RecipientType type) {
        List<String> recipients = new ArrayList<>();

        try {
            Address[] addresses = message.getRecipients(type);

            if (addresses != null) {
                for (Address address : addresses) {
                    if (address instanceof InternetAddress internetAddress) {
                        String email = internetAddress.getAddress();
                        if (email != null && !email.isBlank()) {
                            recipients.add(email);
                        }
                    } else if (address != null) {
                        recipients.add(address.toString());
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return recipients;
    }

    /**
     * Extrai o assunto da mensagem.
     *
     * @param message mensagem lida via Jakarta Mail
     * @return assunto da mensagem, ou string vazia se nulo
     */
    private String extractSubject(Message message) {
        try {
            String subject = message.getSubject();
            return subject != null ? subject : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    /**
     * Extrai o corpo textual principal da mensagem.
     *
     * <p>Este método tenta localizar a primeira parte textual relevante
     * do conteúdo MIME, ignorando anexos explícitos.</p>
     *
     * @param message mensagem lida via Jakarta Mail
     * @return corpo textual extraído, ou string vazia se não encontrado
     */
    private String extractBody(Message message) {
        try {
            Object content = message.getContent();
            String extracted = extractTextFromContent(content);
            return extracted != null ? extracted : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    /**
     * Percorre recursivamente o conteúdo MIME da mensagem em busca
     * da primeira parte textual relevante.
     *
     * <p>Regras atuais:</p>
     * <ul>
     *   <li>se o conteúdo for String, retorna diretamente</li>
     *   <li>se for Multipart, percorre as partes</li>
     *   <li>ignora anexos explícitos</li>
     *   <li>retorna a primeira parte textual não vazia</li>
     * </ul>
     *
     * @param content conteúdo MIME da mensagem ou de uma parte
     * @return texto encontrado, ou string vazia
     */
    private String extractTextFromContent(Object content) {
        try {
            if (content == null) {
                return "";
            }

            if (content instanceof String text) {
                return text;
            }

            if (content instanceof Multipart multipart) {
                for (int i = 0; i < multipart.getCount(); i++) {
                    Part part = multipart.getBodyPart(i);

                    String disposition = part.getDisposition();
                    boolean isAttachment =
                            Part.ATTACHMENT.equalsIgnoreCase(disposition)
                                    || part.getFileName() != null;

                    if (isAttachment) {
                        continue;
                    }

                    String extracted = extractTextFromContent(part.getContent());
                    if (extracted != null && !extracted.isBlank()) {
                        return extracted;
                    }
                }
            }

            return "";
        } catch (Exception ignored) {
            return "";
        }
    }

    /**
     * Extrai o valor do primeiro header encontrado com o nome informado.
     *
     * <p>Exemplos típicos de uso:</p>
     * <ul>
     *   <li>Message-ID</li>
     *   <li>In-Reply-To</li>
     * </ul>
     *
     * @param message mensagem lida via Jakarta Mail
     * @param headerName nome do cabeçalho a ser extraído
     * @return valor do cabeçalho, ou null se ausente
     */
    private String extractHeader(Message message, String headerName) {
        try {
            String[] values = message.getHeader(headerName);
            if (values != null && values.length > 0) {
                return values[0];
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * Extrai os valores do cabeçalho References.
     *
     * <p>Esse cabeçalho é útil para rastrear encadeamentos de resposta
     * em conversas por e-mail.</p>
     *
     * @param message mensagem lida via Jakarta Mail
     * @return lista de valores de References; lista vazia se ausente
     */
    private List<String> extractReferences(Message message) {
        List<String> refs = new ArrayList<>();

        try {
            String[] values = message.getHeader("References");
            if (values != null) {
                for (String value : values) {
                    if (value != null && !value.isBlank()) {
                        refs.add(value);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return refs;
    }

    /**
     * Extrai a data/hora de recebimento da mensagem e a converte
     * para {@link LocalDateTime}.
     *
     * @param message mensagem lida via Jakarta Mail
     * @return data/hora de recebimento, ou null se indisponível
     */
    private LocalDateTime extractReceivedAt(Message message) {
        try {
            if (message.getReceivedDate() != null) {
                return message.getReceivedDate()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * Detecta se a mensagem possui anexos.
     *
     * <p>Por enquanto, o serviço não extrai os anexos em si.
     * Ele apenas sinaliza sua presença.</p>
     *
     * @param message mensagem lida via Jakarta Mail
     * @return true se houver anexo; false caso contrário
     */
    private boolean detectAttachments(Message message) {
        try {
            Object content = message.getContent();
            return hasAttachmentsInContent(content);
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Percorre recursivamente o conteúdo MIME em busca de anexos.
     *
     * <p>Uma parte é considerada anexo quando:</p>
     * <ul>
     *   <li>tem disposition ATTACHMENT</li>
     *   <li>ou possui nome de arquivo associado</li>
     * </ul>
     *
     * @param content conteúdo MIME da mensagem ou de uma parte
     * @return true se encontrar anexo; false caso contrário
     */
    private boolean hasAttachmentsInContent(Object content) {
        try {
            if (content instanceof Multipart multipart) {
                for (int i = 0; i < multipart.getCount(); i++) {
                    Part part = multipart.getBodyPart(i);

                    String disposition = part.getDisposition();
                    boolean isAttachment =
                            Part.ATTACHMENT.equalsIgnoreCase(disposition)
                                    || part.getFileName() != null;

                    if (isAttachment) {
                        return true;
                    }

                    if (hasAttachmentsInContent(part.getContent())) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return false;
    }
}