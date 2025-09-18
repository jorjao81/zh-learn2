# AGENTS.md — Contributor Guide

This document is the authoritative contributor guide for this repository. It defines project structure, coding standards, build/test workflows, and agent-specific rules. For AI agents and humans alike, follow this guide and the Constitution in `.specify`.

## Overview
- Language/tooling: Java 24 (preview features enabled) + Maven multi-module + Java Platform Module System (JPMS).
- Primary goal: CLI for Chinese learning with modular architecture and clean boundaries.
- Entrypoint: `com.zhlearn.cli.ZhLearnApplication` (Picocli). Run via `./zh-learn.sh` or the native binary under `zh-learn-cli/target`.

## Modules & Boundaries
- `zh-learn-domain`: immutable core types and provider interfaces. Keep public API here and free of external concerns.
- `zh-learn-pinyin`: standalone Pinyin utilities (e.g., numbered → tone-mark conversion). Export only `com.zhlearn.pinyin`.
- `zh-learn-infrastructure`: provider implementations (LLM/dictionary/audio), configuration, caching, filesystem, adapters. No UI/CLI code.
- `zh-learn-application`: orchestration/services/use-cases. Depends on `domain` and infrastructure interfaces; avoid provider-specific details leaking out.
- `zh-learn-cli`: Picocli commands and terminal formatting. No business logic; delegate to `application` services.

Boundary rules
- Respect module boundaries; don’t cross layers. Move code to the right module instead of adding illegal dependencies.
- Keep `module-info.java` exports minimal. Export only packages that must be consumed by other modules.
- Prefer immutable domain types and explicit value objects. Avoid leaking infrastructure types into domain/application.

## Development Setup
- JDK: Install Java 24; ensure `--enable-preview` is used by Maven (already configured in root `pom.xml`).
- Maven: 3.8+.
- Optional: GraalVM for native builds.
- Env management: `direnv` + 1Password via `.envrc`. Do not commit secrets.

## Build & Run
- Build all modules: `mvn clean package`
- Quick build (CLI and deps): `mvn -pl zh-learn-cli -am package`
- Run (modular JVM): `./zh-learn.sh word 学习`
- Build native (GraalVM): `cd zh-learn-cli && mvn native:compile-no-fork -Pnative`
- Run native: `./zh-learn-cli/target/zh-learn word 学习`

## Testing
- Frameworks: JUnit 5, AssertJ, Mockito, Cucumber.
- Naming: unit `*Test.java`; integration `*IT.java`; Cucumber runner `RunCucumberTest.java`.
- Features: `src/test/resources/features` in the relevant module.
- Run all tests: `mvn test`
- Run a single test: `mvn -Dtest=TerminalFormatterTest test`
- Guidance: tests live alongside the module that owns the logic; keep tests fast and deterministic. Prefer testing public APIs at module boundaries.

## Coding Standards
- Style: 4-space indentation, UTF-8, standard Java conventions.
- Packages: `com.zhlearn.<module>…` (lowercase). Classes: PascalCase. Methods/fields: camelCase. Constants: UPPER_SNAKE_CASE.
- Nullability: avoid `null` in public APIs; prefer `Optional` or documented preconditions.
- Errors: use precise exceptions; don’t swallow exceptions; surface actionable messages.
- Logging: use SLF4J; never log secrets or full payloads from providers.
- Immutability: default to `final` fields, unmodifiable collections, and records where appropriate.
- CLI: keep command classes thin; delegate to `application` services.

## Providers & Infrastructure
- Implement provider specifics (LLM/dictionary/audio, caching, config) under `zh-learn-infrastructure`.
- Add new providers behind `domain` interfaces; wire via `application` services, and expose selection in CLI where relevant.
- Configuration belongs in infrastructure adapters; application code should consume typed config, not raw environment variables.
- Qwen TTS provider expectations: emit three pronunciations (Cherry, Serena, Chelsie) per request, fail fast when `DASHSCOPE_API_KEY` is missing, and rely on the shared audio cache for normalization.

## Configuration & Secrets
- Source secrets from env vars: e.g., `OPENAI_API_KEY`, `DEEPSEEK_API_KEY`, `DASHSCOPE_API_KEY` (required for Qwen text-to-speech and currently limited to Alibaba's CN-Beijing region accounts). See `.envrc` for local setup patterns.
- Don’t commit secrets or sensitive files. Avoid printing keys, prompts, or provider responses containing PII.

## CLI Usage (quick reference)
- Main: `./zh-learn.sh` (modular JVM) or `./zh-learn-cli/target/zh-learn` (native).
- Examples:
  - Word lookup: `./zh-learn.sh word 学习`
  - List providers: `./zh-learn.sh providers -d`
  - Audio lookup (existing Anki TSV): `./zh-learn.sh audio 学习 xuéxí`
  - Qwen TTS audio (requires `DASHSCOPE_API_KEY`): `./zh-learn.sh audio 学习 xuéxí --audio-provider qwen-tts`

## Pull Requests & Commits
- Conventional Commits: `feat:`, `fix:`, `chore:`, `refactor:`, etc.
- PR checklist:
  - Code follows module boundaries and coding standards.
  - `mvn clean package` and `mvn test` pass locally.
  - Docs updated when behavior or CLI changes (README/AGENTS.md).
  - Adequate tests added or adjusted.
  - Include CLI examples or screenshots for notable output changes.

## Performance & Native Image
- Use native profile for best startup and small footprint: `cd zh-learn-cli && mvn native:compile-no-fork -Pnative`.
- Keep reflection to a minimum; rely on JPMS-friendly libraries and explicit configuration when needed.

## Security & Privacy
- Least-privilege configuration; avoid broad filesystem or network access in providers unless required.
- Sanitize logs and user-facing output; never echo secrets.
- Validate all external inputs (files, env vars, provider responses) before use.

## Agent-Specific Guidance
- This AGENTS.md governs the entire repository. If in doubt, prefer:
  - Moving logic to the correct module instead of adding cross-module deps.
  - Explicit interfaces in `domain` and clean adapters in `infrastructure`.
  - Immutable domain types and minimal exports in `module-info.java`.
- Before significant changes, review the Constitution in `.specify` (see below) and ensure your plan and PR explicitly pass the Constitution Check.

## Constitution (in `.specify`)
- Location: `.specify/memory/constitution.md` (version referenced in templates: 2.1.1).
- Principle: “This constitution supersedes all other development practices. All code changes must comply with these principles. Any deviation requires explicit documentation and justification.”
- Related resources:
  - `.specify/templates/plan-template.md` — includes a “Constitution Check” section you must satisfy.
  - `.specify/memory/constitution_update_checklist.md` — process for amending the constitution.
- Requirement: PR descriptions should include an explicit statement that the Constitution Check passes, or a documented exception with rationale.

## Common Tasks
- Add a new provider:
  - Define/extend interfaces in `zh-learn-domain` if needed.
  - Implement in `zh-learn-infrastructure` and register wiring in `zh-learn-application`.
  - Expose selection in `zh-learn-cli` (new subcommand/option) and add tests.
- Add a new CLI command:
  - Create a Picocli command in `zh-learn-cli`, delegate to `application`.
  - Update `MainCommand` to register it; document usage in README and tests.
- Add a pinyin utility:
  - Implement in `zh-learn-pinyin`; export minimal API via `module-info.java`.

## Troubleshooting
- Preview features: ensure Java 24 is active; Maven compiler plugin already passes `--enable-preview`.
- Native image issues: verify GraalVM and `native` profile; reduce reflection and ensure JPMS compatibility.
- Test discovery: unit tests run via Surefire (`*Test.java`), IT via Failsafe (`*IT.java`), Cucumber via `RunCucumberTest.java`.

## License & Governance
- See `LICENSE` for licensing.
- Architectural direction: follow this guide and the Constitution in `.specify`.

— End of Contributor Guide —
