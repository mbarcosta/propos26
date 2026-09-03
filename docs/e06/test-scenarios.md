# E06 Test Scenarios

## Navigation

- Open wizard from active project.
- Move Next.
- Move Back.
- Cancel.
- Resume.
- Finish.

## Persistence

- Select capability in wizard and verify Automation tab reflects it.
- Edit mapping in Automation tab and rerun wizard.
- Save, close, reopen project, resume wizard.

## Cancelamento de Orientacao

Expected coverage:

- start by message;
- find student/advisorship service tasks;
- PPG Management capability binding;
- gateway condition validation;
- send confirmation email;
- wait response;
- correlation;
- cancel advisorship;
- global validation.

## Vinculacao de Orientacao

Run the wizard over the reference project to validate generic behavior.

## Dissertation Minimal Case

Create/import a process with:

```text
UPLOAD_DISSERTATION
GENERATE_DISSERTATION_DOWNLOAD_LINK
SEND_EMAIL
```

Validate multipart capability representation and output-to-input mapping through `downloadUrl`.
