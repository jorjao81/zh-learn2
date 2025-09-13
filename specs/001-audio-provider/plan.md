# Implementation Plan: Audio Provider System


**Branch**: `001-audio-provider` | **Date**: 2025-09-13 | **Spec**: /Users/pauloschreiner/git/zh-learn2-specify/specs/001-audio-provider/spec.md
**Input**: Feature specification from `/specs/001-audio-provider/spec.md`

## Execution Flow (/plan command scope)
```
1. Load feature spec from Input path
   → If not found: ERROR "No feature spec at {path}"
2. Fill Technical Context (scan for NEEDS CLARIFICATION)
   → Detect Project Type from context (web=frontend+backend, mobile=app+api)
   → Set Structure Decision based on project type
3. Evaluate Constitution Check section below
   → If violations exist: Document in Complexity Tracking
   → If no justification possible: ERROR "Simplify approach first"
   → Update Progress Tracking: Initial Constitution Check
4. Execute Phase 0 → research.md
   → If NEEDS CLARIFICATION remain: ERROR "Resolve unknowns"
5. Execute Phase 1 → contracts, data-model.md, quickstart.md, agent-specific template file (e.g., `CLAUDE.md` for Claude Code, `.github/copilot-instructions.md` for GitHub Copilot, or `GEMINI.md` for Gemini CLI).
6. Re-evaluate Constitution Check section
   → If new violations: Refactor design, return to Phase 1
   → Update Progress Tracking: Post-Design Constitution Check
7. Plan Phase 2 → Describe task generation approach (DO NOT create tasks.md)
8. STOP - Ready for /tasks command
```

**IMPORTANT**: The /plan command STOPS at step 7. Phases 2-4 are executed by other commands:
- Phase 2: /tasks command creates tasks.md
- Phase 3-4: Implementation execution (manual or via tools)

## Summary
Enable pronunciation retrieval for Chinese words with multiple pluggable providers (Existing Anki pronunciations, Forvo, and TTS such as Alibaba/Qwen and Tencent), with caching, normalized volume, mp3 output, and a decoupled selection UI. Providers are discovered via the provider pattern in the infrastructure layer; the application layer orchestrates provider queries, auto-selection when allowed (Anki/Forvo), and exposes a selection session to the CLI. Audio is normalized and transcoded to mp3 before caching; filenames encode the source and metadata; cached assets are reused across runs.

## Technical Context
**Language/Version**: Java 24 (preview), modular JVM; GraalVM for native
**Primary Dependencies**: Picocli (CLI), Helidon DI (infrastructure), SLF4J (logging), JUnit 5 + Cucumber (tests); External tool: FFmpeg for mp3 transcode + loudness normalization
**Storage**: Filesystem cache under `~/.zh-learn/cache/audio`; configuration under `~/.zh-learn/config`; Anki media directory (configured)
**Testing**: Cucumber acceptance tests, JUnit 5 unit tests, AssertJ, Mockito for isolates where appropriate (domain); Integration tests for provider orchestrations with local fixtures
**Target Platform**: CLI-first on macOS/Linux; GraalVM native binary supported
**Project Type**: Single CLI app with multi-module Maven (domain, infrastructure, application, cli)
**Performance Goals**: Interactive feel; typical query < 2s for cached audio; normalization/transcode < 500ms per file on commodity laptop; cancel stale playback on navigation
**Constraints**: mp3 only output; volume-normalized; fail-fast on missing config/permissions; timeouts on external calls; Chinese text handling; no UI coupling in provider modules
**Scale/Scope**: Single-user CLI workflow; artifacts O(10k) cached files; providers independently testable

## Constitution Check
*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Simplicity**:
- Projects: 4 runtime modules (domain, infrastructure, application, cli) + tests; within constitutional modular architecture already established
- Using framework directly? Yes; Picocli, Helidon DI used directly; FFmpeg invoked via process (no wrapper abstraction beyond a narrow port)
- Single data model? Yes; One set of domain entities (Term, Pronunciation, ProviderId, CacheEntry). No extra DTOs (CLI formatting is separate concern)
- Avoiding patterns? Yes; Provider pattern is required; no Repository/UoW introduced

**Architecture**:
- EVERY feature as library? Yes; domain defines ports, infrastructure implements providers, application orchestrates, CLI only UI
- Libraries listed: audio-processing (infra subpackage), providers (forvo, tts, anki), caching (infra), selection-service (application)
- CLI per library: Exposed via `zh-learn-cli` command(s); `word <term>` triggers audio flow; `--format` for structured output (existing pattern)
- Library docs: Provided in `specs/001-audio-provider/contracts/` and `quickstart.md`

**Testing (NON-NEGOTIABLE)**:
- RED-GREEN-Refactor cycle enforced: Yes; plan tasks mandate failures first
- Commits: Tests land before implementation in each slice
- Order: Contract (CLI behavior) → Integration (provider orchestration) → Unit (domain) respected
- Real dependencies: Filesystem used; external network providers stubbed with recorded fixtures to remain deterministic; fail if credentials missing (no silent skip)
- Integration tests added for orchestrator and audio pipeline; contract tests reflect acceptance scenarios
- FORBIDDEN: Implementation before tests; enforced in tasks

**Observability**:
- Structured logging via SLF4J; include provider id, term, cache hit/miss, durations
- Error context sufficient; fail-fast with clear messages; no generic catch-alls

**Versioning**:
- Follows repo/versioning policy; feature gated behind branch `001-audio-provider`
- No public API break outside module boundaries; contracts documented
- Cache and filename scheme versioned in path prefix to allow evolution

## Project Structure

### Documentation (this feature)
```
specs/[###-feature]/
├── plan.md              # This file (/plan command output)
├── research.md          # Phase 0 output (/plan command)
├── data-model.md        # Phase 1 output (/plan command)
├── quickstart.md        # Phase 1 output (/plan command)
├── contracts/           # Phase 1 output (/plan command)
└── tasks.md             # Phase 2 output (/tasks command - NOT created by /plan)
```

### Source Code (repository root)
```
# Option 1: Single project (DEFAULT)
src/
├── models/
├── services/
├── cli/
└── lib/

tests/
├── contract/
├── integration/
└── unit/

# Option 2: Web application (when "frontend" + "backend" detected)
backend/
├── src/
│   ├── models/
│   ├── services/
│   └── api/
└── tests/

frontend/
├── src/
│   ├── components/
│   ├── pages/
│   └── services/
└── tests/

# Option 3: Mobile + API (when "iOS/Android" detected)
api/
└── [same as backend above]

ios/ or android/
└── [platform-specific structure]
```

**Structure Decision**: Use repository’s established multi-module Maven layout (domain, infrastructure, application, cli). Documentation under `specs/001-audio-provider/` as shown.

## Phase 0: Outline & Research
1. **Extract unknowns from Technical Context** above:
   - For each NEEDS CLARIFICATION → research task
   - For each dependency → best practices task
   - For each integration → patterns task

2. **Generate and dispatch research agents**:
   ```
   For each unknown in Technical Context:
     Task: "Research {unknown} for {feature context}"
   For each technology choice:
     Task: "Find best practices for {tech} in {domain}"
   ```

3. **Consolidate findings** in `research.md` using format:
   - Decision: [what was chosen]
   - Rationale: [why chosen]
   - Alternatives considered: [what else evaluated]

**Output**: research.md with all NEEDS CLARIFICATION resolved

## Phase 1: Design & Contracts
*Prerequisites: research.md complete*

1. **Extract entities from feature spec** → `data-model.md`:
   - Entity name, fields, relationships
   - Validation rules from requirements
   - State transitions if applicable

2. **Generate API contracts** from functional requirements:
   - For each user action → endpoint
   - Use standard REST/GraphQL patterns
   - Output OpenAPI/GraphQL schema to `/contracts/`

3. **Generate contract tests** from contracts:
   - One test file per endpoint
   - Assert request/response schemas
   - Tests must fail (no implementation yet)

4. **Extract test scenarios** from user stories:
   - Each story → integration test scenario
   - Quickstart test = story validation steps

5. **Update agent file incrementally** (O(1) operation):
   - Run `/scripts/bash/update-agent-context.sh claude` for your AI assistant
   - If exists: Add only NEW tech from current plan
   - Preserve manual additions between markers
   - Update recent changes (keep last 3)
   - Keep under 150 lines for token efficiency
   - Output to repository root

**Output**: data-model.md, /contracts/*, failing tests, quickstart.md, agent-specific file

## Phase 2: Task Planning Approach
*This section describes what the /tasks command will do - DO NOT execute during /plan*

**Task Generation Strategy**:
- Load `/templates/tasks-template.md` as base
- Generate tasks from Phase 1 design docs (contracts, data model, quickstart)
- Each contract → contract test task [P]
- Each entity → model creation task [P] 
- Each user story → integration test task
- Implementation tasks to make tests pass

**Ordering Strategy**:
- TDD order: Tests before implementation 
- Dependency order: Models before services before UI
- Mark [P] for parallel execution (independent files)

**Estimated Output**: 25-30 numbered, ordered tasks in tasks.md

**IMPORTANT**: This phase is executed by the /tasks command, NOT by /plan

## Phase 3+: Future Implementation
*These phases are beyond the scope of the /plan command*

**Phase 3**: Task execution (/tasks command creates tasks.md)  
**Phase 4**: Implementation (execute tasks.md following constitutional principles)  
**Phase 5**: Validation (run tests, execute quickstart.md, performance validation)

## Complexity Tracking
*Fill ONLY if Constitution Check has violations that must be justified*

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |


## Progress Tracking
*This checklist is updated during execution flow*

**Phase Status**:
- [x] Phase 0: Research complete (/plan command)
- [x] Phase 1: Design complete (/plan command)
- [x] Phase 2: Task planning complete (/plan command - describe approach only)
- [x] Phase 3: Tasks generated (/tasks command)
- [ ] Phase 4: Implementation complete
- [ ] Phase 5: Validation passed

**Gate Status**:
- [x] Initial Constitution Check: PASS
- [x] Post-Design Constitution Check: PASS
- [x] All NEEDS CLARIFICATION resolved
- [x] Complexity deviations documented

---
*Based on Constitution v2.1.1 - See `/memory/constitution.md`*
