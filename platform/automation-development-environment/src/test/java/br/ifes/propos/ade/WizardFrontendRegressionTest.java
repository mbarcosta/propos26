package br.ifes.propos.ade;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class WizardFrontendRegressionTest {

    private static final Path APP_JS = Path.of("src/main/resources/static/app.js");

    @Test
    void wizardValidationIsRecalculatedFromCurrentState() throws IOException {
        String app = Files.readString(APP_JS);

        assertThat(app).contains("configuration changed -> recalculate derived configuration -> validate current state");
        assertThat(app).contains("renderRequirements();\n      renderWizard();");
        assertThat(app).contains("function renderWizardIssues(issues)");
        assertThat(app).contains("if (!issues.length) {\n    return '';");
    }

    @Test
    void invalidMappingCorrectionIsProtectedByCompatibleInputOptions() throws IOException {
        String app = Files.readString(APP_JS);

        assertThat(app).contains("{ source: 'studentName', label: 'Nome do estudante', type: 'String'");
        assertThat(app).contains("{ source: 'advisorName', label: 'Nome do orientador', type: 'String'");
        assertThat(app).contains("consulta o PPG Management");
        assertThat(app).contains("a capability precisa consultar o PPG Management por ID, nome ou e-mail");
        assertThat(app).contains("sortedVariablesForType(context.variables, expectedType)");
        assertThat(app).contains("isCompatibleVariableType(variable.type, expectedType)");
        assertThat(app).contains("Mapping associa dados existentes");
        assertThat(app).contains("body (String), quando existir, e texto recebido");
    }

    @Test
    void startEventDoesNotAskForExistingInstanceCorrelation() throws IOException {
        String app = Files.readString(APP_JS);

        assertThat(app).contains("step.kind === 'START_MESSAGE_EVENT' ? renderStartIdentifierPanel(config) : renderCatchCorrelationPanel");
        assertThat(app).contains("este evento inicial nao localiza uma instancia existente; ele cria uma nova instancia");
        assertThat(app).contains("correlationId - sera criado automaticamente pelo CIR");
        assertThat(app).contains("if (lower.includes('correlation') || lower.includes('request')) return 'String';");
        assertThat(app).contains("Identificador textual usado para correlacionar mensagens");
    }

    @Test
    void coordinatorContactIsProducedByAdvisorshipCheckAndUsedBySendTask() throws IOException {
        String app = Files.readString(APP_JS);

        assertThat(app).contains("{ name: 'coordinatorName', type: 'String' }");
        assertThat(app).contains("{ name: 'coordinatorEmail', type: 'String' }");
        assertThat(app).contains("o worker grava os IDs, e-mails, dados do programa e coordenador");
        assertThat(app).contains("return '${coordinatorEmail}';");
        assertThat(app).contains("config.emailTo = defaultEmailTo(step.elementId, step.name);");
        assertThat(app).contains("selected !== 'coordinatorEmail'");
    }

    @Test
    void deploymentEnsuresBpmnMessageReferencesForConfiguredMessageEvents() throws IOException {
        String app = Files.readString(APP_JS);

        assertThat(app).contains("function ensureMessageRefsForConfiguredEvents(doc)");
        assertThat(app).contains("const messageNode = ensureMessageDefinition(doc, messageName);");
        assertThat(app).contains("messageEvent.setAttribute('messageRef', messageNode.getAttribute('id'));");
        assertThat(app).contains("ensureMessageRefsForConfiguredEvents(doc);\n  consolidateMessageDefinitions(doc);");
    }

    @Test
    void inboundEmailCanCaptureDataRequiredByASelectedCapability() throws IOException {
        String app = Files.readString(APP_JS);

        assertThat(app).contains("Selecionar dados de uma capability");
        assertThat(app).contains("data-inbound-capability");
        assertThat(app).contains("data-capability-input-field");
        assertThat(app).contains("extractionFieldFromSchema(parameter, schemaForParameter(capability, fieldName))");
        assertThat(app).contains("state.dataResolutionPlans[elementId][fieldName] = { strategy: 'MESSAGE_EXTRACTION', source: fieldName }");
        assertThat(app).contains("await publishExtractionContract(inboundId)");
    }
}
