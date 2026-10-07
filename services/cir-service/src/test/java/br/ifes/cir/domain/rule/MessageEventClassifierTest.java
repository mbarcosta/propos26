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

    @Test
    void routesCorrectedDataReplyToDadosComplementaresAndExtractsPayload() throws Exception {
        Path routesFile = tempDir.resolve("routes.json");
        Files.writeString(routesFile, """
                {
                  "routes": [
                    {
                      "externalEvent": "DADOS_COMPLEMENTARES",
                      "action": "CORRELATE_MESSAGE",
                      "messageName": "DADOS_COMPLEMENTARES",
                      "correlationVariable": "correlationId"
                    }
                  ]
                }
                """);

        MessageEventClassifier classifier = new MessageEventClassifier(
                new ProcessedMessageStore(),
                new CirRouteRepository(routesFile.toString(), new ObjectMapper()));

        GmsMessage message = new GmsMessage();
        message.setMessageId("mail-4");
        message.setSubject("Re: Informar Dados Corretamente. MSG-1789480931597");
        message.setBody("""
                 MSG-1789480931597
                Orientador: Joao Souza
                Estudante: Aline
                Titulo: Entrevistas de Processos
                AreaPesquisa: Computacao
                """);

        List<ClassifiedMessage> result = classifier.classify(List.of(message));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKind()).isEqualTo(MessageClassificationKind.REPLY);
        assertThat(result.get(0).getMessageName()).isEqualTo("DADOS_COMPLEMENTARES");
        assertThat(result.get(0).getCorrelationId()).isEqualTo("MSG-1789480931597");
        assertThat(result.get(0).getVariables()).containsEntry("studentName", "Aline");
        assertThat(result.get(0).getVariables()).containsEntry("advisorName", "Joao Souza");
        assertThat(result.get(0).getVariables()).containsEntry("title", "Entrevistas de Processos");
        assertThat(result.get(0).getVariables()).containsEntry("researchArea", "Computacao");
    }

    @Test
    void classifiesAccentedAdvisorshipStartEvenWhenConfiguredSubjectIsTooSpecific() throws Exception {
        Path routesFile = tempDir.resolve("routes.json");
        Files.writeString(routesFile, """
                {
                  "routes": [
                    {
                      "externalEvent": "VINCULACAO_SOLICITADA",
                      "action": "START_PROCESS",
                      "messageName": "VINCULACAO_SOLICITADA",
                      "correlationVariable": "correlationId",
                      "subjectContains": "vinculacao solicitada"
                    }
                  ]
                }
                """);

        MessageEventClassifier classifier = new MessageEventClassifier(
                new ProcessedMessageStore(),
                new CirRouteRepository(routesFile.toString(), new ObjectMapper()));

        GmsMessage message = new GmsMessage();
        message.setMessageId("mail-start-1");
        message.setFrom("mcosta@ifes.edu.br");
        message.setSubject("Nova vinculação");
        message.setBody("""
                Orientador: Joao Souza
                Estudante: Aline
                Título: Entrevistas de Processos
                ÁreaPesquisa: Computação
                """);

        List<ClassifiedMessage> result = classifier.classify(List.of(message));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKind()).isEqualTo(MessageClassificationKind.START);
        assertThat(result.get(0).getMessageName()).isEqualTo("VINCULACAO_SOLICITADA");
        assertThat(result.get(0).getVariables()).containsEntry("studentName", "Aline");
        assertThat(result.get(0).getVariables()).containsEntry("advisorName", "Joao Souza");
        assertThat(result.get(0).getVariables()).containsEntry("title", "Entrevistas de Processos");
        assertThat(result.get(0).getVariables()).containsEntry("researchArea", "Computação");
    }

    @Test
    void configuredStartRouteMatchesSubjectWithUnderscores() throws Exception {
        Path routesFile = tempDir.resolve("defense-routes.json");
        Files.writeString(routesFile, """
                {
                  "routes": [
                    {
                      "externalEvent": "CADASTRO_DE_DEFESA",
                      "action": "START_PROCESS",
                      "messageName": "CADASTRO_DE_DEFESA",
                      "processDefinitionKey": "cadastro_de_defesa",
                      "correlationVariable": "correlationId",
                      "subjectContains": "cadastro de defesa"
                    }
                  ]
                }
                """);

        MessageEventClassifier classifier = new MessageEventClassifier(
                new ProcessedMessageStore(),
                new CirRouteRepository(routesFile.toString(), new ObjectMapper()));

        GmsMessage message = new GmsMessage();
        message.setMessageId("mail-defense-start-1");
        message.setSubject("CADASTRO_DE_DEFESA");
        message.setBody("date: 2026-11-20");

        List<ClassifiedMessage> result = classifier.classify(List.of(message));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKind()).isEqualTo(MessageClassificationKind.START);
        assertThat(result.get(0).getMessageName()).isEqualTo("CADASTRO_DE_DEFESA");
        assertThat(result.get(0).getVariables()).containsEntry("externalEvent", "CADASTRO_DE_DEFESA");
    }

    @Test
    void acceptsAccentedTitleAndResearchAreaLabels() throws Exception {
        Path routesFile = tempDir.resolve("routes.json");
        Files.writeString(routesFile, """
                {
                  "routes": [
                    {
                      "externalEvent": "DADOS_COMPLEMENTARES",
                      "action": "CORRELATE_MESSAGE",
                      "messageName": "DADOS_COMPLEMENTARES",
                      "correlationVariable": "correlationId"
                    }
                  ]
                }
                """);

        MessageEventClassifier classifier = new MessageEventClassifier(
                new ProcessedMessageStore(),
                new CirRouteRepository(routesFile.toString(), new ObjectMapper()));

        GmsMessage message = new GmsMessage();
        message.setMessageId("mail-5");
        message.setSubject("Re: Informar Dados Corretamente. MSG-1789480931597");
        message.setBody("""
                CORRELATION-ID: MSG-1789480931597
                Título: Entrevistas de Processos
                Área: Computação
                """);

        List<ClassifiedMessage> result = classifier.classify(List.of(message));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMessageName()).isEqualTo("DADOS_COMPLEMENTARES");
        assertThat(result.get(0).getVariables()).containsEntry("title", "Entrevistas de Processos");
        assertThat(result.get(0).getVariables()).containsEntry("researchArea", "Computação");
    }
}
