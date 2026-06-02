---
name: Clean Code Kotlin Senior Skill
description: Use this skill to enforce strict senior-level clean code standards in Kotlin source files.
---

# Clean Code Kotlin Senior Skill

## When to use
- Writing or editing Kotlin code in any layer.
- Refactoring large functions or unclear control flow.
- Standardizing readability and maintainability.

## Out of scope
- Non-code operational tasks.
- Architecture migrations not requested by the task.

## Hard rules
- Names must encode intent; avoid ambiguous names.
- Functions must be small, single-purpose, and linear in flow.
- Avoid boolean flags that switch unrelated behavior.
- Prefer immutable values (`val`) and explicit state ownership.
- Minimize side effects and isolate them at boundaries.
- Remove duplication through extraction when semantic intent is shared.
- Comments are exceptional; code must be self-explanatory by structure and naming.

## Execution protocol
1. Identify readability and maintainability defects.
2. Define target function/class responsibilities.
3. Refactor toward explicit names and focused units.
4. Re-check complexity, branching, and duplication.
5. Confirm behavior remains unchanged.

## Output contract
- `Code smells found:` concise list.
- `Applied corrections:` list tied to each smell.
- `Changed files:` with one-line reason.
- `Behavior impact:` `None expected` or explicit impact.
- `Verification:` exact commands executed.

## Do
- Keep APIs explicit and predictable.
- Keep data transformations pure when possible.
- Keep cross-layer dependencies obvious from imports.
- Keep code testability-ready via deterministic and side-effect-light units.

## Do not
- Use generic names like `data`, `value`, `manager`, `helper` without domain context.
- Keep dead code or commented-out code paths.
- Hide side effects in mappers or property getters.
- Introduce clever but opaque Kotlin constructs that reduce clarity.

