package br.ifes.propos.ade.discovery;

import br.ifes.propos.ade.config.DiscoveryProperties;
import br.ifes.propos.ade.model.AutomationCapability;
import java.util.List;

public interface DomainContractDiscovery {
    List<AutomationCapability> discover(DiscoveryProperties.Provider provider);
}
