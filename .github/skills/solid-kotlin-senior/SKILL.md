---
name: SOLID Kotlin Senior Skill
description: Use this skill to enforce senior-level SOLID decisions in Kotlin code inside this clean architecture project.
---

# SOLID Kotlin Senior Skill

## When to use
- Designing or modifying classes, interfaces, use cases, repositories, and view models.
- Evaluating dependency direction and abstraction boundaries.
- Splitting responsibilities in large or mixed-concern units.

## Out of scope
- UI style or visual design decisions.
- Build tooling changes unrelated to SOLID enforcement.

## Hard rules
- Single Responsibility: one reason to change per class/function.
- Open/Closed: extend behavior through composition or new implementations, not editing stable core flows.
- Liskov Substitution: subtype contracts must not weaken preconditions or alter expected postconditions.
- Interface Segregation: keep interfaces narrow and consumer-driven.
- Dependency Inversion: `presentation` and `data` depend on `domain` abstractions, not concrete details.
- No layer violations: no `data` symbols imported into `presentation` UI code.
- Constructor injection only for required collaborators.

## Execution protocol
1. Identify the violated SOLID principle(s).
2. Define the target dependency graph before editing.
3. Apply the smallest architecture-safe transformation.
4. Verify imports and ownership by layer (`presentation`, `domain`, `data`, `di`).
5. Validate unchanged behavior via existing flows.

## Output contract
- `Violation:` list violated principle(s).
- `Decision:` describe chosen architecture correction in one sentence.
- `Changed files:` with rationale.
- `Layer validation:` explicit confirmation of boundary compliance.
- `Verification:` exact commands executed.

## Do
- Use ports in `domain/repository` and implementations in `data`.
- Keep use cases focused on business intent.
- Keep ViewModels as orchestration units, not infrastructure adapters.
- Keep designs testability-ready with deterministic public APIs.

## Do not
- Inject framework or network clients directly into Composables.
- Expose mutable state outside ownership boundaries.
- Use god classes or multipurpose utility objects.
- Accept temporary layer violations.

