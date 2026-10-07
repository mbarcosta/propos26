package br.ifes.propos.ade.model;

import java.util.List;

public record DataResolutionPlan(List<Entry> entries) {
    public DataResolutionPlan { entries = entries == null ? List.of() : List.copyOf(entries); }
    public record Entry(String target, Strategy strategy, String source, String capabilityId,
                        String capabilityOutput, String transformation) {}
    public enum Strategy { MAPPING, MESSAGE_EXTRACTION, TRANSFORMATION, CAPABILITY_LOOKUP }
}
