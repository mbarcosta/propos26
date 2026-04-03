package br.ifes.email_gateway_service.service;

import br.ifes.email_gateway_service.model.EmailMessage;
import br.ifes.email_gateway_service.model.MailBinding;
import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.search.FlagTerm;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Serviço responsável por acessar uma caixa postal via IMAP
 * e converter as mensagens encontradas em objetos {@link EmailMessage}.
 *
 * <p>Este serviço atua como camada de infraestrutura de leitura de e-mails.
 * Sua responsabilidade é exclusivamente:</p>
 *
 * <ul>
 *   <li>abrir conexão com a caixa postal definida por um {@link MailBinding}</li>
 *   <li>buscar mensagens não lidas</li>
 *   <li>extrair dados relevantes da mensagem</li>
 *   <li>converter cada mensagem em um objeto de domínio simplificado</li>
 * </ul>
 *
 * <p>Importante:</p>
 * <ul>
 *   <li>este serviço não aplica regras</li>
 *   <li>não decide ações de negócio</li>
 *   <li>não despacha processos</li>
 *   <li>não marca mensagens como lidas</li>
 * </ul>
 *
 * <p>Ele apenas lê e transforma as mensagens para uso posterior
 * pelo serviço de polling.</p>
 */
@Service
public class MailReaderService {

    /**
     * Lê as mensagens não lidas da caixa postal configurada no binding
     * e as converte em objetos {@link EmailMessage}.
     *
     * @param binding configuração da caixa postal a ser acessada
     * @return lista de mensagens lidas da caixa
     */
    public List<EmailMessage> readEmails(MailBinding binding) {

        List<EmailMessage> emails = new ArrayList<>();

        try {
            Properties props = buildMailProperties(binding);

            Session session = Session.getInstance(props);
            session.setDebug(true); // depois pode ser externalizado

            Store store = session.getStore("imaps");
            store.connect(
                    binding.getImapHost(),
                    binding.getMailboxAddress(),
                    binding.getAppPassword()
            );

            Folder inbox = store.getFolder(binding.getSourceFolder());
            inbox.open(Folder.READ_WRITE);

            FlagTerm unseenFlag = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            Message[] messages = inbox.search(unseenFlag);

            for (Message message : messages) {
                String from = extractFrom(message);
                String subject = extractSubject(message);
                String body = extractBody(message);
                int messageNumber = message.getMessageNumber();

                emails.add(new EmailMessage(from, subject, body, messageNumber));
            }

            inbox.close(false);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return emails;
    }

    /**
     * Monta as propriedades necessárias para conexão IMAPS.
     *
     * @param binding binding com host e porta configurados
     * @return propriedades de conexão
     */
    private Properties buildMailProperties(MailBinding binding) {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", binding.getImapHost());
        props.put("mail.imaps.port", String.valueOf(binding.getImapPort()));
        props.put("mail.imaps.ssl.enable", "true");

        // Para garantir leitura sem marcar como lido
        props.put("mail.imap.peek", "true");
        props.put("mail.imaps.peek", "true");

        return props;
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

        } catch (Exception e) {
            return "";
        }

        return "";
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
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Extrai o corpo textual da mensagem.
     *
     * <p>Este método tenta obter o conteúdo textual principal do e-mail,
     * tratando casos comuns como:</p>
     *
     * <ul>
     *   <li>mensagens simples em texto</li>
     *   <li>mensagens HTML</li>
     *   <li>mensagens multipart</li>
     *   <li>multipart aninhado</li>
     * </ul>
     *
     * <p>Por enquanto, anexos são ignorados. A estratégia atual consiste
     * em localizar a primeira parte textual não vazia.</p>
     *
     * @param message mensagem lida via Jakarta Mail
     * @return corpo textual extraído, ou string vazia se não encontrado
     */
    private String extractBody(Message message) {
        try {
            Object content = message.getContent();
            String extracted = extractTextFromContent(content);
            return extracted != null ? extracted : "";
        } catch (Exception e) {
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

            if (content instanceof jakarta.mail.Multipart multipart) {
                for (int i = 0; i < multipart.getCount(); i++) {
                    var part = multipart.getBodyPart(i);

                    String disposition = part.getDisposition();
                    boolean isAttachment =
                            jakarta.mail.Part.ATTACHMENT.equalsIgnoreCase(disposition)
                                    || part.getFileName() != null;

                    if (isAttachment) {
                        continue;
                    }

                    Object partContent = part.getContent();
                    String extracted = extractTextFromContent(partContent);

                    if (extracted != null && !extracted.isBlank()) {
                        return extracted;
                    }
                }
            }

            return "";
        } catch (Exception e) {
            return "";
        }
    }
}