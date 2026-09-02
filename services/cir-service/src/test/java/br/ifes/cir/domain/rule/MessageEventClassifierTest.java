package br.ifes.cir.domain.rule;

import br.ifes.cir.client.dto.GmsMessage;
import br.ifes.cir.domain.config.CirRouteRepository;
import br.ifes.cir.domain.store.ProcessedMessageStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MessageEventClassifierTest {

    @TempDir
    Path tempDir;

    @Test
    void classifiesCorrelatedEmailReplyUsingGenericEmailReplyRoute() throws Exception {
        Path routesFile = tempDir.resolve("routes.json");
        Files.writeString(routesFile, """
                {
                  "routes": [
                    {
                      "externalEvent": "EMAIL_REPLY",
                      "action": "CORRELATE_MESSAGE",
                      "messageName": "EMAIL_REPLY",
                      "correlationVariable": "correlationId"
                    }
                  ]
                }
                """);

        MessageEventClassifier classifier = new MessageEventClassifier(
                new ProcessedMessageStore(),
                new CirRouteRepository(routesFile.toString(), new ObjectMapper()));

        GmsMessage message = new GmsMessage();
        message.setMessageId("mail-1");
        message.setSubject("Re: Confirmacao de orientacao");
        message.setBody("confirmado\n\nCORRELATION-ID: MSG-12345");

        List<ClassifiedMessage> result = classifier.classify(List.of(message));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKind()).isEqualTo(MessageClassificationKind.REPLY);
        assertThat(result.get(0).getMessageName()).isEqualTo("EMAIL_REPLY");
        assertThat(result.get(0).getCorrelationId()).isEqualTo("MSG-12345");
        assertThat(result.get(0).getVariables()).containsEntry("body", message.getBody());
    }

    @Test
    void acceptsCamelCaseCorrelationIdMarker() throws Exception {
        Path routesFile = tempDir.resolve("routes.json");
        Files.writeString(routesFile, """
                {
                  "routes": [
                    {
                      "externalEvent": "EMAIL_REPLY",
                      "action": "CORRELATE_MESSAGE",
                      "messageName": "EMAIL_REPLY",
                      "correlationVariable": "correlationId"
                    }
                  ]
                }
                """);

        MessageEventClassifier classifier = new MessageEventClassifier(
                new ProcessedMessageStore(),
                new CirRouteRepository(routesFile.toString(), new ObjectMapper()));

        GmsMessage message = new GmsMessage();
        message.setMessageId("mail-2");
        message.setSubject("Confirmado");
        message.setBody("correlationId=VINC-2026-001");

        List<ClassifiedMessage> result = classifier.classify(List.of(message));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCorrelationId()).isEqualTo("VINC-2026-001");
    }

    @Test
    void acceptsLooseIdMarkerAndTokenInStudentReply() throws Exception {
        Path routesFile = tempDir.resolve("routes.json");
        Files.writeString(routesFile, """
                {
                  "routes": [
                    {
                      "externalEvent": "EMAIL_REPLY",
                      "action": "CORRELATE_MESSAGE",
                      "messageName": "EMAIL_REPLY",
                      "correlationVariable": "correlationId"
                    }
                  ]
                }
                """);

        MessageEventClassifier classifier = new MessageEventClassifier(
                new ProcessedMessageStore(),
                new CirRouteRepository(routesFile.toString(), new ObjectMapper()));

        GmsMessage message = new GmsMessage();
        message.setMessageId("mail-3");
        message.setSubject("Confirmacao de Orientacao - MSG-1788313250233");
        message.setBody("""
                --- Em ter., 1 de set. de 2026 as 22:40, <ppcomp.propos@gmail.com> escreveu:
                ID  MSG-1788313250233. - Nao apague este ID

                RECUSADO
                """);

        List<ClassifiedMessage> result = classifier.classify(List.of(message));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKind()).isEqualTo(MessageClassificationKind.REPLY);
        assertThat(result.get(0).getMessageName()).isEqualTo("EMAIL_REPLY");
        assertThat(result.get(0).getCorrelationId()).isEqualTo("MSG-1788313250233");
    }
}
