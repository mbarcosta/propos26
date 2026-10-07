package br.ifes.cir.domain.extraction;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Deterministic label/value extractor. It deliberately does not infer semantic values from free text. */
@Component
public class StructuredTemplateExtractor implements DataExtractionProvider {
    @Override public boolean supports(String strategy) { return "STRUCTURED_TEMPLATE".equalsIgnoreCase(strategy); }

    @Override
    public ExtractionResult extract(InputExtractionContract contract, String content) {
        Map<String, String> scalars = new LinkedHashMap<>();
        Map<String, List<Map<String, String>>> arrays = parse(content, scalars);
        Map<String, Object> values = new LinkedHashMap<>();
        List<String> diagnostics = new ArrayList<>();
        for (ExtractionField field : contract.getOutputs()) {
            try {
                Object value = field.getType().startsWith("Array<")
                        ? convertArray(field, arrays.get(field.getName()))
                        : convert(field.getType(), scalars.get(field.getName()));
                if (value == null && field.isRequired()) diagnostics.add("Required field '" + field.getName() + "' was not found in " + contract.getSourcePart());
                else if (value != null) values.put(field.getName(), value);
            } catch (RuntimeException e) {
                diagnostics.add("Field '" + field.getName() + "' is not a valid " + field.getType() + ": " + e.getMessage());
            }
        }
        return new ExtractionResult(values, diagnostics);
    }

    private Map<String, List<Map<String, String>>> parse(String content, Map<String, String> scalars) {
        Map<String, List<Map<String, String>>> arrays = new LinkedHashMap<>();
        String currentArray = null;
        Map<String, String> currentItem = null;
        for (String raw : (content == null ? "" : content).split("\\R")) {
            String line = raw.trim();
            if (line.isBlank()) continue;
            boolean itemStart = line.startsWith("-");
            if (itemStart) line = line.substring(1).trim();
            int separator = line.indexOf(':');
            if (separator < 0) continue;
            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            if (value.isEmpty() && !itemStart) {
                currentArray = key; currentItem = null; arrays.putIfAbsent(key, new ArrayList<>()); continue;
            }
            if (currentArray != null && (itemStart || raw.startsWith(" ") || raw.startsWith("\t"))) {
                if (itemStart || currentItem == null) { currentItem = new LinkedHashMap<>(); arrays.get(currentArray).add(currentItem); }
                currentItem.put(key, value);
            } else {
                currentArray = null; currentItem = null; scalars.put(key, value);
            }
        }
        return arrays;
    }

    private Object convertArray(ExtractionField field, List<Map<String, String>> rows) {
        if (rows == null) return null;
        List<Object> result = new ArrayList<>();
        for (Map<String, String> row : rows) {
            if (field.getProperties().isEmpty()) result.add(row.values().stream().findFirst().orElse(""));
            else {
                Map<String, Object> item = new LinkedHashMap<>();
                for (ExtractionField property : field.getProperties()) {
                    Object value = convert(property.getType(), row.get(property.getName()));
                    if (value == null && property.isRequired()) throw new IllegalArgumentException("array item misses " + property.getName());
                    if (value != null) item.put(property.getName(), value);
                }
                result.add(item);
            }
        }
        return result;
    }

    private Object convert(String type, String value) {
        if (value == null || value.isBlank()) return null;
        return switch (type) {
            case "Boolean" -> { if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) throw new IllegalArgumentException(value); yield Boolean.valueOf(value); }
            case "Integer" -> Integer.valueOf(value);
            case "Long" -> Long.valueOf(value);
            case "Decimal", "Number" -> new BigDecimal(value);
            case "Date" -> LocalDate.parse(value).toString();
            case "DateTime" -> OffsetDateTime.parse(value).toString();
            default -> value;
        };
    }
}
