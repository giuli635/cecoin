---
name: Refactoring Senior Skill
description: Use this skill for strict behavior-preserving refactors in this Kotlin Multiplatform Compose Desktop project.
---

# Refactoring Senior Skill

## When to use
- Reducing duplication without changing user-visible behavior.
- Improving cohesion, naming, function boundaries, and dependency direction.
- Extracting reusable units while preserving current architecture contracts.

## Out of scope
- Feature changes.
- API contract changes.
- Route or argument contract changes.
- Dependency upgrades not required by the refactor intent.

## Hard rules
- Preserve route contracts in `composeApp/src/desktopMain/kotlin/edu/dyds/movies/presentation/navigation/Navigation.kt` (`home`, `detail/{movieId}`, `NavType.IntType`).
- Preserve load triggers in `HomeRoute` and `DetailRoute` (`LaunchedEffect(Unit)` and `LaunchedEffect(id)`).
- Keep orchestration in ViewModels and rendering in Composables.
- Keep mapping responsibilities in mapping/model code, not screens.
- Keep fallback-oriented error behavior unless explicitly requested otherwise.
- Keep code-level text in English.
- Keep each change as one cohesive refactoring intent.

## Execution protocol
1. Define one explicit refactoring intent in a single sentence.
2. Enumerate behavior invariants that cannot change.
3. Perform the minimum set of edits to satisfy the intent.
4. Verify call sites and architecture boundaries.
5. Run project tests relevant to modified behavior.
6. Report files changed, invariants preserved, and residual risk.

## Output contract
- Start with `Intent:` and one sentence.
- List `Changed files:` with one-line reason per file.
- List `Preserved invariants:` as bullets.
- List `Verification:` with exact executed command(s).
- List `Residual risk:` with concrete risk or `None identified`.

## Do
- Prefer extraction over copy-paste.
- Prefer explicit names over comments.
- Keep functions focused and side effects localized.
- Keep dependency direction from `presentation -> domain -> data`.
- Keep design testability-ready through small units and constructor-injected dependencies.

## Do not
- Mix refactor and feature work.
- Move network or persistence concerns into UI.
- Hide breaking behavior behind naming-only changes.
- Increase coupling across architecture layers.

