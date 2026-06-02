---
name: Conventional Commits Skill
description: Use this skill to produce deterministic Conventional Commits 1.0.0 messages for this repository.
---

# Conventional Commits Skill

## When to use
- Writing commit messages for local commits.
- Reviewing commit messages before publishing.

## Out of scope
- Deciding commit strategy for unrelated changes.
- Combining unrelated intents into one commit.

## Hard rules
- Format is `<type>[optional scope]: <description>`.
- `type` is lowercase and must be one of: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`.
- Description is imperative, concise, and without trailing period.
- Scope is optional and must be short and meaningful when used.
- One commit message must represent one cohesive conceptual change.
- Breaking changes must use `!` and include a `BREAKING CHANGE:` footer.

## Execution protocol
1. Identify the single conceptual change.
2. Choose the most specific valid type.
3. Choose a scope only if it improves clarity.
4. Write a concise imperative description.
5. Add body/footer only when necessary.

## Output contract
- `Selected type:` one of the allowed types.
- `Selected scope:` explicit value or `none`.
- `Commit message:` final subject line.
- `Optional body/footer:` include only when needed.

## Do
- Keep messages specific and concrete.
- Keep subject length short enough to scan quickly.

## Do not
- Use vague subjects like `update` or `changes`.
- Capitalize the type.
- End the subject with punctuation.
- Mix multiple intents in one commit message.
