# Normalized capability contract

`AutomationCapability` is the Wizard-facing model. It contains operation identity, provider, HTTP protocol/method/endpoint, recursive input/output schemas, flattened top-level parameters for existing UI compatibility, contract version, SHA-256 fingerprint and discovery status.

`CapabilitySchema` represents `String`, `Boolean`, `Integer`, `Long`, `Decimal`, `Date`, `DateTime`, `Enum`, objects and arrays. It preserves properties, array items, enum values, requiredness, format and the source reference.

For the current PPG-ADM `CREATE_DEFENSE` request, the provider declares:

| Input | Type | Required |
|---|---|---|
| studentId | Long | yes |
| advisorId | Long | yes |
| title | String | yes |
| date | Date | yes |
| location | String | yes |
| committeeMembers | Array<CommitteeMember> | yes |

The real `CommitteeMember` schema is also provider-owned and currently has required `name`, `email`, `institution` and `role`, all strings. Neither schema is declared in ADE code.

The operation fingerprint is computed from normalized method, endpoint, input and output. Changes in requiredness, shape or type change the fingerprint even if the provider version is not bumped.

