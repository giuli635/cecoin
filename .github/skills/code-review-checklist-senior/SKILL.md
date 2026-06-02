---
name: Code Review Checklist Senior Skill
description: Use this skill to guide findings-oriented senior code reviews with strict architecture and quality criteria.
---

# Code Review Checklist Senior Skill

## When to use
- Reviewing pull requests or local diffs.
- Auditing architectural conformance and maintainability risk.
- Identifying regressions, coupling issues, and future testing blockers.

## Out of scope
- Implementing fixes directly unless explicitly requested.
- Approving merge policy decisions.

## Hard rules
- Findings must be listed before any summary.
- Findings must include severity, file path, and precise location when possible.
- Focus on defects, regressions, and risk; avoid stylistic nitpicks unless they impact maintainability.
- State assumptions and unknowns explicitly.
- Checklist guides findings only; it does not impose merge gates.

## Execution protocol
1. Inspect change intent and touched layers.
2. Run checklist categories in this order:
   - Correctness and regressions.
   - Architecture boundaries and SOLID.
   - State and concurrency safety.
   - Error handling and fallback behavior.
   - Readability and maintainability.
   - Testability readiness for future tests.
3. Record findings with severity (`Critical`, `High`, `Medium`, `Low`).
4. Add open questions or assumptions.
5. Add short secondary summary.

## Output contract
- `Findings:` ordered by severity, each including file reference and rationale.
- `Open questions / assumptions:` only if needed.
- `Secondary summary:` brief and optional.
- If no findings: explicitly state `No findings identified` and mention residual risk or testing gaps.

## Do
- Prioritize high-impact risks first.
- Be explicit about behavior change risk.
- Highlight design choices that reduce future testing effort.
- Keep feedback actionable and concrete.

## Do not
- Lead with a general summary before findings.
- Report style-only comments as high severity.
- Claim certainty without evidence.
- Convert checklist output into hard merge policy.

