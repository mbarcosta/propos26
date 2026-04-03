package br.ifes.email_gateway_service.service;

import br.ifes.email_gateway_service.model.EmailMessage;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.Flags;
import jakarta.mail.search.FlagTerm;

/**
 * Serviço responsável por acessar uma caixa postal via IMAP (Gmail)
 * e transformar emails recebidos em objetos EmailMessage.
 *
 * Nesta versão:
 * - lê apenas emails NÃO LIDOS (UNSEEN)
 * - extrai corretamente o endereço de email do remetente
 * - marca os emails como lidos após processamento (evita duplicidade)
 */
@Service
public class MailReaderService {

    public List<EmailMessage> readEmails() {

        List<EmailMessage> emails = new ArrayList<>();

        try {

            // Configuração da conexão IMAPS (Gmail)
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            props.put("mail.imaps.host", "imap.gmail.com");
            props.put("mail.imaps.port", "993");
            props.put("mail.imaps.ssl.enable", "true");

            Session session = Session.getInstance(props);
            session.setDebug(true); // log detalhado (remover depois)

            Store store = session.getStore("imaps");

            // ⚠️ TODO: mover credenciais para application.properties
            store.connect("imap.gmail.com",
                    "ppcomp.propos@gmail.com",
                    "friyyjmpxnhksstn");

            // Abre a caixa de entrada com permissão de escrita (necessário para marcar como lido)
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Filtra apenas emails NÃO LIDOS
            FlagTerm unseenFlag = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            Message[] messages = inbox.search(unseenFlag);

            for (Message message : messages) {

                // --- Extrai remetente ---
                String from = "";

                Address[] fromAddresses = message.getFrom();
                if (fromAddresses != null && fromAddresses.length > 0) {
                    from = ((InternetAddress) fromAddresses[0]).getAddress();
                }

                // --- Extrai assunto ---
                String subject = message.getSubject();

                // Proteção contra null
                if (subject == null) {
                    subject = "";
                }

                // Cria objeto de domínio
                emails.add(new EmailMessage(from, subject));

                // Marca como lido para evitar reprocessamento
                message.setFlag(Flags.Flag.SEEN, true);
            }

            inbox.close(false);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return emails;
    }
}
