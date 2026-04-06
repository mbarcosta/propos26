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
 * <p>Política atual:</p>
 * <ul>
 *   <li>mensagens processadas com sucesso -> movidas para a pasta Processed</li>
 *   <li>mensagens ignoradas -> permanecem na INBOX</li>
 *   <li>mensagens com erro -> permanecem na INBOX</li>
 * </ul>
 *
 * <p>Nesta versão, a mensagem é localizada por {@code messageId}, que é
 * mais adequado como identificador de integração entre serviços do que
 * {@code messageNumber}, que é mais operacional e dependente da pasta.</p>
 */
@Service
public class MailPostProcessorService {

    /**
     * Move para a pasta "Processed" a mensagem identificada por
     * {@code messageId} no contexto do binding informado.
     *
     * <p>Fluxo executado:</p>
     * <ol>
     *   <li>conecta ao servidor IMAP configurado no binding;</li>
     *   <li>abre a pasta de origem (normalmente INBOX);</li>
     *   <li>garante a existência da pasta Processed;</li>
     *   <li>localiza a mensagem pelo cabeçalho {@code Message-ID};</li>
     *   <li>copia a mensagem para a pasta Processed;</li>
     *   <li>marca a mensagem original como deletada;</li>
     *   <li>fecha a pasta com expunge para efetivar a remoção.</li>
     * </ol>
     *
     * @param binding configuração da caixa postal
     * @param email mensagem a ser movida; deve conter {@code messageId}
     * @throws RuntimeException em caso de falha na localização ou movimentação
     */
    public void moveToProcessed(MailBinding binding, EmailMessage email) {
        Store store = null;
        Folder sourceFolder = null;
        Folder targetFolder = null;

        try {
            /*
             * Define o protocolo de acesso ao servidor de e-mail.
             * Se não houver valor explícito no binding, usa IMAPS.
             */
            String protocol = binding.getMailServer().getProtocol() != null
                    ? binding.getMailServer().getProtocol()
                    : "imaps";

            /*
             * Monta as propriedades de conexão da sessão Jakarta Mail.
             */
            Properties props = new Properties();
            props.put("mail.store.protocol", protocol);
            props.put("mail." + protocol + ".host", binding.getMailServer().getHost());
            props.put("mail." + protocol + ".port", String.valueOf(binding.getMailServer().getPort()));
            props.put("mail." + protocol + ".ssl.enable", "true");

            Session session = Session.getInstance(props);
            session.setDebug(true);

            /*
             * Abre conexão com o servidor de e-mail.
             */
            store = session.getStore(protocol);
            store.connect(
                    binding.getMailServer().getHost(),
                    binding.getMailServer().getPort(),
                    binding.getMailServer().getUsername(),
                    binding.getMailServer().getPassword()
            );

            /*
             * Resolve a pasta de origem.
             * Se o binding não definir uma pasta específica, usa INBOX.
             */
            String sourceFolderName = binding.getFolders() != null
                    && binding.getFolders().getInbox() != null
                    && !binding.getFolders().getInbox().isBlank()
                    ? binding.getFolders().getInbox()
                    : "INBOX";

            sourceFolder = store.getFolder(sourceFolderName);
            sourceFolder.open(Folder.READ_WRITE);

            /*
             * Resolve a pasta de destino ("Processed").
             */
            String processedFolderName = binding.getFolders() != null
                    ? binding.getFolders().getProcessed()
                    : null;

            if (processedFolderName == null || processedFolderName.isBlank()) {
                throw new RuntimeException("Pasta Processed não configurada no binding: " + binding.getId());
            }

            targetFolder = store.getFolder(processedFolderName);

            /*
             * Se a pasta de processados ainda não existir, tenta criá-la.
             */
            if (!targetFolder.exists()) {
                boolean created = targetFolder.create(Folder.HOLDS_MESSAGES);
                if (!created) {
                    throw new RuntimeException(
                            "Não foi possível criar a pasta de processados: " + processedFolderName
                    );
                }
            }

            /*
             * Valida a entrada.
             */
            if (email == null || email.getMessageId() == null || email.getMessageId().isBlank()) {
                throw new RuntimeException("messageId inválido para movimentação para Processed.");
            }

            /*
             * Localiza a mensagem real na pasta de origem a partir do Message-ID.
             */
            Message message = findMessageByMessageId(sourceFolder, email.getMessageId());

            if (message == null) {
                throw new RuntimeException(
                        "Mensagem não encontrada na pasta de origem. messageId=" + email.getMessageId()
                );
            }

            /*
             * Copia a mensagem para a pasta de processados.
             */
            sourceFolder.copyMessages(new Message[]{message}, targetFolder);

            /*
             * Marca a mensagem original como deletada.
             * A remoção efetiva acontece ao fechar a pasta com expunge=true.
             */
            message.setFlag(Flags.Flag.DELETED, true);

            /*
             * Fecha a pasta de origem com expunge para efetivar a remoção
             * das mensagens marcadas como deletadas.
             */
            sourceFolder.close(true);
            sourceFolder = null;

            /*
             * Fecha a conexão com o servidor.
             */
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
                if (targetFolder != null && targetFolder.isOpen()) {
                    targetFolder.close(false);
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

    /**
     * Localiza, dentro de uma pasta IMAP já aberta, a mensagem cujo cabeçalho
     * {@code Message-ID} corresponda ao valor informado.
     *
     * <p>Observação: essa busca é linear sobre as mensagens da pasta.
     * Para o estágio atual do projeto isso é aceitável. Se depois houver
     * necessidade de escala maior, dá para otimizar usando mecanismos
     * de busca do provedor ou cache.</p>
     *
     * @param folder pasta já aberta em modo de leitura/escrita
     * @param targetMessageId messageId procurado
     * @return a mensagem encontrada, ou {@code null} se não houver correspondência
     * @throws Exception em caso de erro ao ler as mensagens
     */
    private Message findMessageByMessageId(Folder folder, String targetMessageId) throws Exception {
        Message[] messages = folder.getMessages();

        for (Message message : messages) {
            String[] headerValues = message.getHeader("Message-ID");

            if (headerValues == null || headerValues.length == 0) {
                continue;
            }

            for (String currentMessageId : headerValues) {
                if (targetMessageId.equals(currentMessageId)) {
                    return message;
                }
            }
        }

        return null;
    }
}