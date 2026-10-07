# Runtime capability discovery

The PPG-ADM now publishes its machine-readable OpenAPI 3 contract at `GET /v3/api-docs`. Before E09 the repository only contained `openapi.yaml`; inspection showed that it was not served at runtime and omitted operation request/response schemas.

Only backend-registered providers may be queried. `ade.providers[*].contract-url` is deployment configuration, never user input. `OpenApiContractDiscovery` validates the URL scheme, disables redirects, applies connection/read timeouts, limits the response size, accepts OpenAPI 3 JSON, rejects external `$ref`, resolves local references and normalizes operations.

`CapabilityRegistryService` merges legacy local capabilities with discovered capabilities, with discovered IDs taking precedence. `POST /api/capabilities/refresh` forces refresh; the normal read path uses a TTL. Successful results are persisted to `ADE_CAPABILITY_CACHE_FILE`. If refresh fails, the last known contract remains visible as `stale`, with `discoveryError` and its original discovery time. An absent operation and an unavailable provider therefore have different representations.

Security controls are: provider allowlist in backend configuration, absolute HTTP(S) URL validation, no redirects, optional server-side bearer token, timeout, size limit, OpenAPI version validation and local-reference-only resolution.

