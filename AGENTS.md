# Repository Guidelines

## Project Structure & Module Organization
- Multi-module Maven project targeting Java 24 (preview) and Java modules.
- Modules: `zh-learn-domain` (core models/providers), `zh-learn-infrastructure` (LLM/dictionary, caching, config), `zh-learn-application` (service orchestration), `zh-learn-cli` (Picocli entrypoint). Each module has `src/main/java` and `module-info.java`.
- Entrypoint: `com.zhlearn.cli.ZhLearnApplication`. Run via `./zh-learn.sh` (modular JVM) or native binary under `zh-learn-cli/target`.

## Build, Test, and Development Commands
- Build all: `mvn clean package` (compiles with `--enable-preview`).
- Run (JVM modules): `./zh-learn.sh word 学习`.
- Run tests: `mvn test` (JUnit 5 + Cucumber).
- Native image (GraalVM): `cd zh-learn-cli && mvn native:compile-no-fork -Pnative` then `./target/zh-learn word 学习`.
- Quick module build: `mvn -pl zh-learn-cli -am package` (build CLI and dependencies).

## Coding Style & Naming Conventions
- Java 24, 4-space indentation, UTF-8. Keep public APIs in `domain` and `application`; keep LLM/dictionary specifics in `infrastructure`.
- Packages: `com.zhlearn.<module>…` (lowercase). Classes: PascalCase. Methods/fields: camelCase. Constants: UPPER_SNAKE_CASE.
- Keep module boundaries strict; only export what’s needed in `module-info.java`. Prefer immutable domain types.

## Testing Guidelines
- Frameworks: JUnit 5, AssertJ, Mockito, Cucumber.
- Naming: unit tests `*Test.java`; integration tests `*IT.java`; Cucumber runner `RunCucumberTest.java`. Feature files under `src/test/resources/features`.
- Run all: `mvn test`. Run a single test: `mvn -Dtest=TerminalFormatterTest test`.

## Commit & Pull Request Guidelines
- Use Conventional Commits: `feat:`, `fix:`, `chore:`, `refactor:`, etc. Example: `feat: add file-system cache for providers`.
- Before PR: ensure `mvn clean package` and `mvn test` pass; update README/docs if behavior changes; add/adjust tests.
- PRs should include: concise description, context/rationale, screenshots or CLI examples when UI/CLI output changes, and linked issues.

## Security & Configuration Tips
- Secrets via environment variables (e.g., `OPENAI_API_KEY`, `DEEPSEEK_API_KEY`). `direnv`/1Password integration exists in `.envrc`; do not commit secrets.
- Avoid logging sensitive values. Prefer configuration adapters in `infrastructure.common` and provider-specific classes.

