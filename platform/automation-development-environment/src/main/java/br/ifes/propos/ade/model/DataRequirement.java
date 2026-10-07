package br.ifes.propos.ade.model;

import java.util.List;

public record DataRequirement(String name, String type, boolean required, Status status,
                              String source, List<DataResolutionPlan.Strategy> allowedStrategies,
                              String diagnostic) {
    public enum Status { SATISFIED, MISSING, TYPE_MISMATCH, TRANSFORMATION_REQUIRED, LOOKUP_REQUIRED }
}
