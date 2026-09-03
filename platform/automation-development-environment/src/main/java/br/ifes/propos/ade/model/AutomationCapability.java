package br.ifes.propos.ade.model;

import java.util.List;
import java.util.Map;

public record AutomationCapability(
        String id,
        String name,
        String description,
        String type,
        String provider,
        String interfaceType,
        String endpoint,
        List<Map<String, String>> inputParameters,
        List<Map<String, String>> outputParameters,
        String implementationType,
        String implementation,
        String deployment,
        String status
) {
}
