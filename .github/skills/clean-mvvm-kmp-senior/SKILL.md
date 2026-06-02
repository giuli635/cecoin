---
name: Clean MVVM KMP Senior Skill
description: Use this skill to apply strict Clean MVVM decisions in this Kotlin Multiplatform Compose Desktop architecture.
---

# Clean MVVM KMP Senior Skill

## When to use
- Implementing or updating screen flow, state, or view model orchestration.
- Modifying use case wiring and repository integration.
- Enforcing architecture boundaries in `presentation`, `domain`, `data`, and `di`.

## Out of scope
- Feature-level product decisions outside architecture concerns.
- Styling-only UI changes.

## Hard rules
- `presentation` renders state and emits user actions only.
- ViewModels coordinate state and use cases; they do not perform data mapping from remote models.
- `domain` owns entities, business rules, and use-case contracts.
- `data` owns repository implementations and remote/local orchestration.
- `di` is responsible for object graph wiring.
- Keep route contract unchanged: `home`, `detail/{movieId}`, `NavType.IntType`.
- Preserve loading trigger locations: `LaunchedEffect(Unit)` and `LaunchedEffect(id)`.

## Execution protocol
1. Classify requested change by layer before editing.
2. Verify current ownership of each responsibility.
3. Apply edits only in the owning layer.
4. Validate no forbidden imports cross boundaries.
5. Validate screen behavior and state flow continuity.

## Output contract
- `Layer ownership decision:` mapping of change to layer.
- `Changed files:` with architecture reason.
- `Boundary checks:` explicit pass/fail statement.
- `State flow checks:` how loading/content/empty/error behavior was preserved.
- `Verification:` exact commands executed.

## Do
- Reuse shared composables for loading and empty states when applicable.
- Keep UI state models explicit and immutable from consumers.
- Keep orchestration deterministic and testability-ready.
- Keep API-to-domain mapping centralized in data mapping code.

## Do not
- Trigger network clients directly from Composables.
- Put business rules in UI rendering code.
- Leak remote DTOs into `presentation` or `domain`.
- Mix wiring logic with business logic.

