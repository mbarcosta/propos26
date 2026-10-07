package br.ifes.propos.ade.model;

import java.time.Instant;
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
        String status,
        String operationId,
        String protocol,
        String method,
        CapabilitySchema inputSchema,
        CapabilitySchema outputSchema,
        String contractVersion,
        String contractFingerprint,
        Instant lastDiscoveredAt,
        boolean stale,
        String discoveryError
) {
}
