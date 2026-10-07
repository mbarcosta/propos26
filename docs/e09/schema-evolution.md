# Schema evolution, refresh and failure

Each discovered operation has a normalized SHA-256 fingerprint and provider version. A project records the fingerprint used when the capability is selected. When refresh changes it, the Wizard marks the project `CONFIGURATION_REVIEW_REQUIRED`; it never silently treats the old configuration as current.

Refresh is explicit through the Capabilities screen and automatic after the configured TTL (default five minutes). Rendering the Wizard reads the registry cache and does not fetch OpenAPI directly.

Failure states:

- provider unavailable: cached capabilities have `stale=true`, retain `lastDiscoveredAt`, and include `discoveryError`;
- operation removed after a successful refresh: it is absent from the dynamic registry and an existing binding cannot resolve it;
- malformed/oversized/unsupported contract: refresh fails and the last known contract is used with a warning;
- changed fingerprint: configuration review is mandatory.

The architectural fixture test adds `A_CAPABILITY_NEVER_SEEN_BY_ADE` only to an OpenAPI document and verifies discovery of its required `Array<Member>` input. A second fixture changes required fields and verifies a different fingerprint.

