# ZH Learn Constitution

## Core Principles

### I. Modular Architecture (NON-NEGOTIABLE)
Java modules are strictly enforced for separation of concerns, in a Clean Code Architecture:
- Domain layer contains only business logic and interfaces - no external dependencies except Java base
- Infrastructure layer implements domain interfaces - can have external dependencies
- Application layer orchestrates domain and infrastructure - no direct external I/O
- CLI layer handles user interaction only - delegates to application layer
- Module exports/imports must be explicit - never bypass or disable module system

### II. Fail-Fast Philosophy
Never add fallbacks or catch exceptions unless explicitly required:
- Let exceptions bubble up and crash the application - debugging is more important than graceful degradation
- No defensive programming - if something is wrong, fail immediately and visibly
- No "best effort" implementations - either work correctly or fail clearly

### III. Test-First Development (NON-NEGOTIABLE)
All functionality must have tests before implementation:
- All features must start with Cucumber acceptance tests
- Unit tests for domain logic using JUnit 5 and AssertJ
- All tests must pass before commits - no exceptions
- Test structure follows modular boundaries

### IV. Always-Passing Tests (NON-NEGOTIABLE)
The codebase always starts with passing tests - any test failure is developer fault:
- NEVER claim a task is complete while tests are failing
- All tests must pass at all times - if a test breaks, it is your responsibility to fix it
- No work is considered done until the full test suite passes
- Breaking existing functionality while adding new features is unacceptable
- When tests fail, stop all other work and fix them immediately

### V. CLI-First Interface
All functionality exposed via command-line interface:
- Text in/out protocol: arguments → stdout, errors → stderr
- Support both human-readable and structured output
- GraalVM native compilation required for distribution

### VI. Provider Pattern for Extensions
External integrations implemented as providers:
- Providers follow consistent interface
- Service loader pattern for runtime discovery
- Each provider is self-contained and independently testable
- Clear separation between core logic and external service integration

### VI. Module Preference over Implementation
Always prefer existing modules and libraries over custom implementations:
- Search the web thoroughly to find existing solutions before implementing any feature
- Use existing libraries and modules unless there's a compelling reason not to
- Never implement functionality that already exists in well-maintained open source modules
- Document why existing solutions were rejected if implementing custom code
- Prioritize modules with active maintenance, good documentation, and community support

## Technical Standards

### Java Platform Requirements
- Java 24+ with preview features enabled
- Maven 3.8+ for build management
- GraalVM for native compilation
- Helidon for dependency injection framework
- PicoCLI for command-line interface

### Dependencies and Libraries
- LangChain4J for AI model integration
- SLF4J with simple implementation for logging
- Cucumber for integration testing
- Jansi for terminal formatting
- JSoup for HTML parsing
- No fallback dependencies - if primary choice doesn't work, fail
- ALWAYS use latest stable version of libraries - search web to confirm current versions
- Never rely on outdated dependency information

## Development Workflow

### Testing Gates
- All unit tests must pass before any commit
- All acceptance tests must pass for new features
- No test-skipping or test-disabling allowed
- Cucumber scenarios must be green for domain behavior

### Code Quality
- Follow existing code conventions and patterns
- No code comments unless documenting non-obvious behaviour
- Keep classes focused and modules cohesive

#### Java Style Guide
- **Never use Java's `var` keyword** - always use explicit type declarations for better code readability and maintainability
- **Always use Records** when possible - prefer immutable data carriers over traditional classes
- **Prefer switch expressions** over long if-else chains for better readability and maintainability
- **Always import types** and use simple type names, not fully qualified names - unless necessary due to ambiguity
- **Use final fields** by default - make fields mutable only when absolutely necessary
- **Prefer immutable collections** - use `List.of()`, `Map.of()`, etc., over mutable alternatives
- **Use pattern matching** where appropriate - instanceof patterns, record patterns, etc.

### Documentation Standards
- Documentation must be informational, not promotional
- Avoid hyperbole and self-promotion in all documentation
- Focus on how to use the application, not how awesome it is
- Provide clear, factual instructions without marketing language

### Exception Handling
- Never catch exceptions unless explicitly instructed
- No generic exception handlers
- Let application crash on unexpected conditions
- Use checked exceptions for expected failure modes only

## Governance

This constitution supersedes all other development practices. All code changes must comply with these principles. Any deviation requires explicit documentation and justification.

**Implementation over Configuration**: If implementation doesn't match instructions exactly, fail rather than implementing differently.

**Version**: 1.1.0 | **Ratified**: 2025-09-13 | **Last Amended**: 2025-09-20