package br.ifes.propos.ade.service;

import br.ifes.propos.ade.model.DataRequirement;
import br.ifes.propos.ade.model.DataResolutionPlan;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Analyzes service-task snapshots in graph order. The BPMN adapter supplies the data visible on each
 * incoming path; this core remains independent from BPMN XML and from any domain operation.
 */
@Service
public class ProcessInputRequirementAnalyzer {
    private final CapabilityRegistryService registry;
    private final DataRequirementResolver resolver;
    public ProcessInputRequirementAnalyzer(CapabilityRegistryService registry, DataRequirementResolver resolver) {
        this.registry = registry; this.resolver = resolver;
    }
    public List<TaskAnalysis> analyze(List<TaskSnapshot> tasks) {
        List<TaskAnalysis> result = new ArrayList<>();
        for (TaskSnapshot task : tasks) {
            var requirements = resolver.resolve(registry.capability(task.capabilityId()), task.availableData(), task.plan());
            result.add(new TaskAnalysis(task.elementId(), task.capabilityId(), requirements,
                    requirements.stream().allMatch(item -> !item.required() || item.status() == DataRequirement.Status.SATISFIED)));
        }
        return result;
    }
    public record TaskSnapshot(String elementId, String capabilityId,
                               List<DataRequirementResolver.ProcessData> availableData, DataResolutionPlan plan) {}
    public record TaskAnalysis(String elementId, String capabilityId,
                               List<DataRequirement> requirements, boolean ready) {}
}
