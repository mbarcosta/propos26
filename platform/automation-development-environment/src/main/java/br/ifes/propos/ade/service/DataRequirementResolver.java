package br.ifes.propos.ade.service;

import br.ifes.propos.ade.model.AutomationCapability;
import br.ifes.propos.ade.model.DataRequirement;
import br.ifes.propos.ade.model.DataResolutionPlan;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DataRequirementResolver {
    public record ProcessData(String name, String type, String origin) {}

    public List<DataRequirement> resolve(AutomationCapability capability, List<ProcessData> available,
                                         DataResolutionPlan plan) {
        List<DataRequirement> result = new ArrayList<>();
        Map<String, DataResolutionPlan.Entry> entries = (plan == null ? List.<DataResolutionPlan.Entry>of() : plan.entries())
                .stream().collect(java.util.stream.Collectors.toMap(DataResolutionPlan.Entry::target, item -> item, (a, b) -> b));
        for (Map<String, String> input : capability.inputParameters()) {
            String name = input.get("name");
            String type = input.getOrDefault("type", "Object");
            boolean required = !"false".equalsIgnoreCase(input.get("required"));
            DataResolutionPlan.Entry entry = entries.get(name);
            ProcessData sameName = available.stream().filter(item -> item.name().equals(name)).findFirst().orElse(null);
            ProcessData selected = entry == null ? sameName : available.stream()
                    .filter(item -> item.name().equals(entry.source())).findFirst().orElse(sameName);
            DataRequirement.Status status;
            String diagnostic;
            if (!required && selected == null && entry == null) { status = DataRequirement.Status.SATISFIED; diagnostic = "Optional input"; }
            else if (entry != null && entry.strategy() == DataResolutionPlan.Strategy.MESSAGE_EXTRACTION) { status = DataRequirement.Status.SATISFIED; diagnostic = "Produced by configured message extraction"; }
            else if (entry != null && entry.strategy() == DataResolutionPlan.Strategy.CAPABILITY_LOOKUP) { status = DataRequirement.Status.LOOKUP_REQUIRED; diagnostic = "Will be produced by capability " + entry.capabilityId(); }
            else if (selected == null) { status = DataRequirement.Status.MISSING; diagnostic = "No previous producer or configured origin"; }
            else if (compatible(selected.type(), type)) { status = DataRequirement.Status.SATISFIED; diagnostic = "Compatible process data is available"; }
            else if (entry != null && entry.strategy() == DataResolutionPlan.Strategy.TRANSFORMATION) { status = DataRequirement.Status.TRANSFORMATION_REQUIRED; diagnostic = "Explicit conversion is required before invocation"; }
            else { status = DataRequirement.Status.TYPE_MISMATCH; diagnostic = selected.type() + " cannot be mapped directly to " + type; }
            result.add(new DataRequirement(name, type, required, status, selected == null ? null : selected.name(),
                    allowed(available, type), diagnostic));
        }
        return result;
    }

    private List<DataResolutionPlan.Strategy> allowed(List<ProcessData> available, String targetType) {
        List<DataResolutionPlan.Strategy> result = new ArrayList<>();
        if (available.stream().anyMatch(item -> compatible(item.type(), targetType))) result.add(DataResolutionPlan.Strategy.MAPPING);
        result.add(DataResolutionPlan.Strategy.MESSAGE_EXTRACTION);
        if (!available.isEmpty()) result.add(DataResolutionPlan.Strategy.TRANSFORMATION);
        result.add(DataResolutionPlan.Strategy.CAPABILITY_LOOKUP);
        return result;
    }

    public boolean compatible(String source, String target) {
        if (source == null || target == null) return false;
        if (source.equalsIgnoreCase(target)) return true;
        return "Integer".equalsIgnoreCase(source) && ("Long".equalsIgnoreCase(target)
                || "Decimal".equalsIgnoreCase(target) || "Number".equalsIgnoreCase(target));
    }
}
