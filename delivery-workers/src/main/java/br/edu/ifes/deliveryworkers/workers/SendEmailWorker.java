package br.edu.ifes.deliveryworkers.workers;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Map;
import java.util.Properties;

@Component
public class SendEmailWorker {

    private final ExternalTaskClient client;
    private final String smtpHost;
    private final int smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String mailFrom;
    private final boolean sendEnabled;

    public SendEmailWorker(
            ExternalTaskClient client,
            @Value("${spring.mail.host:smtp.gmail.com}") String smtpHost,
            @Value("${spring.mail.port:587}") int smtpPort,
            @Value("${spring.mail.username:}") String smtpUsername,
            @Value("${spring.mail.password:}") String smtpPassword,
            @Value("${mail.from:}") String mailFrom,
            @Value("${mail.send-enabled:true}") boolean sendEnabled) {
        this.client = client;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.mailFrom = mailFrom;
        this.sendEnabled = sendEnabled;
    }

    @PostConstruct
    public void subscribe() {
        client.subscribe("SEND_EMAIL")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    String activityId = externalTask.getActivityId();
                    String recipient = firstNonBlank(
                            variable(externalTask, "outboundEmailTo"),
                            recipientForActivity(activityId, externalTask)
                    );
                    String subject = firstNonBlank(
                            variable(externalTask, "outboundEmailSubject"),
                            subjectForActivity(activityId, externalTask)
                    );
                    String body = firstNonBlank(
                            variable(externalTask, "outboundEmailBody"),
                            bodyForActivity(activityId, externalTask)
                    );

                    if (recipient.isBlank()) {
                        externalTaskService.handleFailure(
                                externalTask,
                                "SEND_EMAIL missing recipient",
                                "Could not resolve recipient for activity " + activityId,
                                3,
                                60000
                        );
                        return;
                    }

                    System.out.println("=== SEND_EMAIL capability ===");
                    System.out.println("Activity: " + activityId);
                    System.out.println("To: " + recipient);
                    System.out.println("Subject: " + subject);
                    System.out.println("Body: " + body);

                    try {
                        sendEmail(recipient, subject, body);
                    } catch (Exception e) {
                        externalTaskService.handleFailure(
                                externalTask,
                                "SEND_EMAIL delivery failed",
                                e.getMessage(),
                                3,
                                60000
                        );
                        return;
                    }

                    externalTaskService.complete(
                            externalTask,
                            Map.of(
                                    "emailSent", true,
                                    "lastEmailActivityId", activityId,
                                    "lastEmailRecipient", recipient,
                                    "lastEmailSubject", subject
                            )
                    );
                })
                .open();
    }

    private void sendEmail(String recipient, String subject, String body) {
        if (!sendEnabled) {
            throw new IllegalStateException("MAIL_SEND_ENABLED=false; email not sent");
        }
        if (mailFrom == null || mailFrom.isBlank()) {
            throw new IllegalStateException("mail.from is not configured");
        }
        if (smtpUsername == null || smtpUsername.isBlank() || smtpPassword == null || smtpPassword.isBlank()) {
            throw new IllegalStateException("SMTP username/password are not configured");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.ssl.trust", smtpHost);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.writetimeout", "30000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject, "UTF-8");
            message.setText(body, "UTF-8");
            Transport.send(message);
            System.out.println("SMTP send completed.");
        } catch (Exception e) {
            throw new IllegalStateException("SMTP send failed: " + e.getMessage(), e);
        }
    }

    private String recipientForActivity(String activityId, org.camunda.bpm.client.task.ExternalTask externalTask) {
        if ("Task_SolicitarDados".equals(activityId)) {
            return firstNonBlank(
                    variable(externalTask, "emailTo"),
                    variable(externalTask, "recipient"),
                    variable(externalTask, "to"),
                    variable(externalTask, "from"),
                    variable(externalTask, "requesterEmail")
            );
        }
        if ("Task_SolicitarConfirmacaoEstudante".equals(activityId)) {
            return firstNonBlank(variable(externalTask, "studentEmail"));
        }
        if ("Task_SolicitarConfirmacaoCoordenador".equals(activityId)) {
            return firstNonBlank(
                    variable(externalTask, "coordinatorEmail"),
                    variable(externalTask, "advisorEmail")
            );
        }
        return firstNonBlank(
                variable(externalTask, "emailTo"),
                variable(externalTask, "recipient"),
                variable(externalTask, "to")
        );
    }

    private String subjectForActivity(String activityId, org.camunda.bpm.client.task.ExternalTask externalTask) {
        String correlationId = firstNonBlank(variable(externalTask, "correlationId"));
        if ("Task_SolicitarDados".equals(activityId)) {
            return firstNonBlank(
                    variable(externalTask, "emailSubject"),
                    variable(externalTask, "subject"),
                    "Pendencia na solicitacao de vinculacao" + suffixCorrelationId(correlationId)
            );
        }
        if ("Task_SolicitarConfirmacaoEstudante".equals(activityId)) {
            return "Confirmacao de vinculacao de orientacao"
                    + suffixCorrelationId(correlationId);
        }
        if ("Task_SolicitarConfirmacaoCoordenador".equals(activityId)) {
            return "Confirmacao final de vinculacao de orientacao"
                    + suffixCorrelationId(correlationId);
        }
        return firstNonBlank(
                variable(externalTask, "emailSubject"),
                variable(externalTask, "subject"),
                "propos26 notification"
        );
    }

    private String bodyForActivity(String activityId, org.camunda.bpm.client.task.ExternalTask externalTask) {
        String correlationId = firstNonBlank(variable(externalTask, "correlationId"));
        String studentName = firstNonBlank(variable(externalTask, "studentName"), "estudante");
        String advisorName = firstNonBlank(variable(externalTask, "advisorName"), "orientador");
        String title = firstNonBlank(variable(externalTask, "title"), "");
        String researchArea = firstNonBlank(variable(externalTask, "researchArea"), "");
        String programName = firstNonBlank(variable(externalTask, "programName"), "");
        String campus = firstNonBlank(variable(externalTask, "campus"), "");
        String coordinatorName = firstNonBlank(variable(externalTask, "coordinatorName"), "coordenador");

        if ("Task_SolicitarDados".equals(activityId)) {
            return firstNonBlank(
                    variable(externalTask, "emailBody"),
                    "Nao foi possivel reconhecer completamente a solicitacao de vinculacao.\n"
                            + "Pendencias: " + firstNonBlank(variable(externalTask, "missingFields"), "-") + "\n"
                            + correlationLine(correlationId)
            );
        }
        if ("Task_SolicitarConfirmacaoEstudante".equals(activityId)) {
            return "Confirme a solicitacao de vinculacao de orientacao.\n\n"
                    + "Estudante: " + studentName + "\n"
                    + "Orientador: " + advisorName + "\n"
                    + "Titulo: " + title + "\n"
                    + "Area de pesquisa: " + researchArea + "\n\n"
                    + "Responda este e-mail mantendo o identificador de correlacao.\n"
                    + correlationLine(correlationId);
        }
        if ("Task_SolicitarConfirmacaoCoordenador".equals(activityId)) {
            return "Confirme a vinculacao de orientacao para registro final.\n\n"
                    + "Coordenador: " + coordinatorName + "\n"
                    + "Programa: " + programName + "\n"
                    + "Campus: " + campus + "\n"
                    + "Estudante: " + studentName + "\n"
                    + "Orientador: " + advisorName + "\n"
                    + "Titulo: " + title + "\n"
                    + "Area de pesquisa: " + researchArea + "\n\n"
                    + "Responda este e-mail mantendo o identificador de correlacao.\n"
                    + correlationLine(correlationId);
        }
        return firstNonBlank(
                variable(externalTask, "emailBody"),
                variable(externalTask, "body"),
                ""
        );
    }

    private String suffixCorrelationId(String correlationId) {
        return correlationId.isBlank() ? "" : " [" + correlationId + "]";
    }

    private String correlationLine(String correlationId) {
        return correlationId.isBlank() ? "" : "CORRELATION-ID: " + correlationId;
    }

    private Object variable(org.camunda.bpm.client.task.ExternalTask externalTask, String name) {
        return externalTask.<Object>getVariable(name);
    }

    private String firstNonBlank(Object... values) {
        for (Object value : values) {
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value);
            }
        }
        return "";
    }
}
