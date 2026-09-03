# Wizard UI

The wizard is a floating modal over the ADE.

## Layout

```text
Header
Main
  Progress navigation
  Current step content
Footer
```

The modal has:

- backdrop;
- centered window;
- shadow;
- fixed footer;
- scrollable content;
- desktop-first dimensions;
- responsive fallback for smaller screens.

## States

Step state is represented with text and symbols:

- `>` current
- `✓` configured
- `!` error
- `~` warning
- `○` pending

The UI does not rely only on color.

## Interaction

The footer exposes:

- Back
- Cancel
- Next
- Finish

`Finish` is shown only on the global validation step and is disabled while blocking errors exist.
