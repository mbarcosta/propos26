package br.ifes.cir.domain.extraction;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class StructuredTemplateExtractorTest {
    @Test
    void extractsTypedScalarsAndArrayOfObjects() {
        ExtractionField title = field("title", "String", true);
        ExtractionField date = field("date", "Date", true);
        ExtractionField members = field("committeeMembers", "Array<CommitteeMember>", true);
        members.setProperties(List.of(field("name", "String", true), field("email", "String", true)));
        InputExtractionContract contract = new InputExtractionContract();
        contract.setOutputs(List.of(title, date, members));
        var result = new StructuredTemplateExtractor().extract(contract, """
                title: Dynamic contracts
                date: 2026-11-05
                committeeMembers:
                  - name: Ada
                    email: ada@example.org
                  - name: Linus
                    email: linus@example.org
                """);
        assertThat(result.valid()).isTrue();
        assertThat(result.values()).containsEntry("date", "2026-11-05");
        assertThat((List<?>) result.values().get("committeeMembers")).hasSize(2);
    }

    @Test
    void reportsMissingRequiredFieldAndTypeMismatch() {
        InputExtractionContract contract = new InputExtractionContract();
        contract.setOutputs(List.of(field("studentId", "Long", true), field("date", "Date", true)));
        var result = new StructuredTemplateExtractor().extract(contract, "studentId: not-an-id");
        assertThat(result.valid()).isFalse();
        assertThat(result.diagnostics()).hasSize(2);
    }
    private ExtractionField field(String name, String type, boolean required) {
        ExtractionField result = new ExtractionField(); result.setName(name); result.setType(type); result.setRequired(required); return result;
    }
}
