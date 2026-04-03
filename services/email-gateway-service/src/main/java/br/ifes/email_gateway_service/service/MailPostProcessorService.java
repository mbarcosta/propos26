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
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            props.put("mail.imaps.host", binding.getImapHost());
            props.put("mail.imaps.port", String.valueOf(binding.getImapPort()));
            props.put("mail.imaps.ssl.enable", "true");

            Session session = Session.getInstance(props);
            session.setDebug(true);

            store = session.getStore("imaps");
            store.connect(
                    binding.getImapHost(),
                    binding.getMailboxAddress(),
                    binding.getAppPassword()
            );

            sourceFolder = store.getFolder(binding.getSourceFolder());
            sourceFolder.open(Folder.READ_WRITE);

            Folder targetFolder = store.getFolder(binding.getProcessedFolder());

            if (!targetFolder.exists()) {
                boolean created = targetFolder.create(Folder.HOLDS_MESSAGES);
                if (!created) {
                    throw new RuntimeException(
                            "Não foi possível criar a pasta de processados: " + binding.getProcessedFolder()
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
                    "Erro ao mover e-mail para a pasta Processed: " + e.getClass().getSimpleName() + " - " + e.getMessage(),
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