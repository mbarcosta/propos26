package br.ifes.propos.ade.model;

import java.util.List;

/** Recursive, transport-independent representation of an operation schema. */
public record CapabilitySchema(
        String name, String type, String format, boolean required, String description,
        List<String> enumValues, List<CapabilitySchema> properties,
        CapabilitySchema items, String reference
) {
    public CapabilitySchema {
        enumValues = enumValues == null ? List.of() : List.copyOf(enumValues);
        properties = properties == null ? List.of() : List.copyOf(properties);
    }
}
