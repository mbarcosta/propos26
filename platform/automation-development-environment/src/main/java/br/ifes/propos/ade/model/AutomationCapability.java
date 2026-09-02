package br.ifes.propos.ade.model;

public record AutomationCapability(
        String id,
        String name,
        String type,
        String provider,
        String interfaceType,
        String status
) {
}

