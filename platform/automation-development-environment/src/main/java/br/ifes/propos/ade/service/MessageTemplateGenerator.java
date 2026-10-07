package br.ifes.propos.ade.service;

import br.ifes.propos.ade.model.CapabilitySchema;
import org.springframework.stereotype.Service;

@Service
public class MessageTemplateGenerator {
    public String generate(CapabilitySchema inputSchema) {
        StringBuilder result = new StringBuilder();
        if (inputSchema == null) return "";
        for (CapabilitySchema field : inputSchema.properties()) append(result, field, 0, false);
        return result.toString().stripTrailing();
    }
    private void append(StringBuilder out, CapabilitySchema field, int indent, boolean arrayItem) {
        String prefix = " ".repeat(indent) + (arrayItem ? "- " : "");
        if (field.type().startsWith("Array<")) {
            out.append(prefix).append(field.name()).append(":\n");
            if (field.items() != null && !field.items().properties().isEmpty()) {
                boolean first = true;
                for (CapabilitySchema child : field.items().properties()) {
                    append(out, child, indent + 2, first); first = false;
                }
            } else out.append(" ".repeat(indent + 2)).append("- <").append(field.items() == null ? "item" : field.items().type()).append(">\n");
        } else if (!field.properties().isEmpty()) {
            out.append(prefix).append(field.name()).append(":\n");
            for (CapabilitySchema child : field.properties()) append(out, child, indent + 2, false);
        } else out.append(prefix).append(field.name()).append(": <").append(field.name()).append(">\n");
    }
}
