package br.ifes.cir.domain.extraction;

import java.util.List;
import java.util.Map;

public record ExtractionResult(Map<String, Object> values, List<String> diagnostics) {
    public boolean valid() { return diagnostics.isEmpty(); }
}
