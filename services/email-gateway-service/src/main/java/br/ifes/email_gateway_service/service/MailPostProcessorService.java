package br.ifes.email_gateway_service.service;

import br.ifes.email_gateway_service.model.EmailMessage;
import br.ifes.email_gateway_service.model.MailBinding;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Serviço responsável pelo pós-processamento de mensagens no servidor IMAP.
 *
 * Política atual:
 * - mensagens processadas com sucesso -> movidas para a pasta Processed
 * - mensagens ignoradas -> permanecem na INBOX
 * - mensagens com erro -> permanecem na INBOX
 */
@Service
public class MailPostProcessorService {

    /**
     * Move a mensagem indicada para a pasta de processados definida no binding.
     *
     * @param binding configuração da caixa postal
     * @param email mensagem a ser movida
     */
    public void moveToProcessed(MailBinding binding, EmailMessage email) {
        Store store = null;
        Folder sourceFolder = null;

        try {
            String protocol = binding.getMailServer().getProtocol() != null
                    ? binding.getMailServer().getProtocol()
                    : "imaps";

            Properties props = new Properties();
            props.put("mail.store.protocol", protocol);
            props.put("mail." + protocol + ".host", binding.getMailServer().getHost());
            props.put("mail." + protocol + ".port", String.valueOf(binding.getMailServer().getPort()));
            props.put("mail." + protocol + ".ssl.enable", "true");

            Session session = Session.getInstance(props);
            session.setDebug(true);

            store = session.getStore(protocol);
            store.connect(
                    binding.getMailServer().getHost(),
                    binding.getMailServer().getPort(),
                    binding.getMailServer().getUsername(),
                    binding.getMailServer().getPassword()
            );

            String sourceFolderName = binding.getFolders() != null
                    && binding.getFolders().getInbox() != null
                    && !binding.getFolders().getInbox().isBlank()
                    ? binding.getFolders().getInbox()
                    : "INBOX";

            sourceFolder = store.getFolder(sourceFolderName);
            sourceFolder.open(Folder.READ_WRITE);

            String processedFolderName = binding.getFolders() != null
                    ? binding.getFolders().getProcessed()
                    : null;

            if (processedFolderName == null || processedFolderName.isBlank()) {
                throw new RuntimeException("Pasta Processed não configurada no binding: " + binding.getId());
            }

            Folder targetFolder = store.getFolder(processedFolderName);

            if (!targetFolder.exists()) {
                boolean created = targetFolder.create(Folder.HOLDS_MESSAGES);
                if (!created) {
                    throw new RuntimeException(
                            "Não foi possível criar a pasta de processados: " + processedFolderName
                    );
                }
            }

            Message message = sourceFolder.getMessage(email.getMessageNumber());

            if (message == null) {
                throw new RuntimeException(
                        "Mensagem não encontrada na pasta de origem. messageNumber=" + email.getMessageNumber()
                );
            }

            sourceFolder.copyMessages(new Message[]{message}, targetFolder);
            message.setFlag(Flags.Flag.DELETED, true);

            sourceFolder.close(true);
            sourceFolder = null;

            store.close();
            store = null;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao mover e-mail para a pasta Processed: "
                            + e.getClass().getSimpleName()
                            + " - "
                            + e.getMessage(),
                    e
            );
        } finally {
            try {
                if (sourceFolder != null && sourceFolder.isOpen()) {
                    sourceFolder.close(false);
                }
            } catch (Exception ignored) {
            }

            try {
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (Exception ignored) {
            }
        }
    }
}