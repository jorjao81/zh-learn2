# Module Architecture Documentation

## Overview

The zh-learn project implements a modular architecture using the Java Platform Module System (JPMS) to enforce clean boundaries, explicit dependencies, and maintainable code organization. The system is divided into five distinct modules, each with specific responsibilities and well-defined interfaces.

## Module Hierarchy

### High-Level Module Structure

```mermaid
flowchart TD
    A[zh-learn-cli] --> B[zh-learn-application]
    A --> C[zh-learn-infrastructure]
    B --> D[zh-learn-domain]
    B --> C
    C --> D
    C --> E[zh-learn-pinyin]
    E --> D

    F[External Dependencies] --> A
    F --> B
    F --> C
    F --> E

    style D fill:#e1f5fe
    style C fill:#f3e5f5
    style B fill:#e8f5e8
    style A fill:#fff3e0
    style E fill:#fce4ec
```

**Dependency Flow Rules:**
- **Domain**: No dependencies on other modules (pure business logic)
- **Infrastructure**: Depends on domain (implements interfaces)
- **Application**: Depends on domain and infrastructure (orchestration)
- **CLI**: Depends on application and infrastructure (user interface)
- **Pinyin**: Utility module, depends only on domain

## Module-by-Module Analysis

### zh-learn-domain (Core)

```mermaid
classDiagram
    class DomainModule {
        <<module>>
        +exports com.zhlearn.domain.model
        +exports com.zhlearn.domain.provider
        +exports com.zhlearn.domain.service
    }

    class Models {
        Hanzi
        Pinyin
        WordAnalysis
        Definition
        Example
        Explanation
        StructuralDecomposition
        ProviderInfo
        ProviderConfiguration
    }

    class Providers {
        <<interfaces>>
        ExampleProvider
        ExplanationProvider
        StructuralDecompositionProvider
        AudioProvider
        PinyinProvider
        DefinitionProvider
        DefinitionFormatterProvider
    }

    class Services {
        <<interfaces>>
        WordAnalysisService
    }

    DomainModule --> Models
    DomainModule --> Providers
    DomainModule --> Services
```

**Responsibilities:**
- Define core business entities as immutable records
- Establish provider interfaces for external services
- Declare service contracts for business operations
- Maintain zero external dependencies (pure domain logic)

**Key Design Principles:**
- Immutable data structures (`record` types)
- Interface-driven design for extensibility
- No infrastructure concerns
- Rich domain models with behavior

### zh-learn-infrastructure (Implementations)

```mermaid
classDiagram
    class InfrastructureModule {
        <<module>>
        +requires com.zhlearn.domain
        +requires com.zhlearn.pinyin
        +requires java.net.http
        +requires langchain4j.core
        +requires org.slf4j
        +requires io.helidon.faulttolerance
        +exports com.zhlearn.infrastructure.dummy
        +exports com.zhlearn.infrastructure.common
        +exports com.zhlearn.infrastructure.audio
        +exports com.zhlearn.infrastructure.cache
        +exports com.zhlearn.infrastructure.forvo
        +exports com.zhlearn.infrastructure.qwen
        +exports com.zhlearn.infrastructure.tencent
        +exports com.zhlearn.infrastructure.anki
        +exports com.zhlearn.infrastructure.pleco
        +exports com.zhlearn.infrastructure.pinyin4j
    }

    class ProviderImplementations {
        DummyExampleProvider
        ConfigurableExampleProvider
        QwenAudioProvider
        ForvoAudioProvider
        TencentAudioProvider
        AnkiPronunciationProvider
        Pinyin4jProvider
    }

    class Infrastructure {
        AIProviderFactory
        AudioDownloadExecutor
        AudioCache
        FileSystemCache
        PlecoExportParser
        AnkiCardParser
    }

    class ExternalIntegrations {
        HTTP_Clients
        LangChain4j_Models
        TTS_Services
        File_Systems
    }

    InfrastructureModule --> ProviderImplementations
    InfrastructureModule --> Infrastructure
    InfrastructureModule --> ExternalIntegrations
```

**Responsibilities:**
- Implement all domain provider interfaces
- Manage external API integrations (OpenAI, Qwen, Forvo, etc.)
- Handle caching, file I/O, and data persistence
- Provide configuration and factory classes
- Manage HTTP clients and network communications

**External Dependencies:**
- **LangChain4j**: AI model abstractions
- **Jackson**: JSON/YAML processing
- **Helidon**: Fault tolerance and retry
- **Apache Commons**: CSV parsing
- **Provider SDKs**: Tencent Cloud, etc.

### zh-learn-application (Services)

```mermaid
classDiagram
    class ApplicationModule {
        <<module>>
        +requires com.zhlearn.domain
        +requires com.zhlearn.infrastructure
        +requires org.slf4j
        +exports com.zhlearn.application.service
        +exports com.zhlearn.application.format
        +exports com.zhlearn.application.audio
        +uses com.zhlearn.domain.provider.*
    }

    class Services {
        WordAnalysisServiceImpl
        ParallelWordAnalysisService
    }

    class AudioOrchestration {
        AudioOrchestrator
        PronunciationCandidate
    }

    class Formatting {
        ExamplesHtmlFormatter
        AnalysisPrinter
    }

    ApplicationModule --> Services
    ApplicationModule --> AudioOrchestration
    ApplicationModule --> Formatting
```

**Responsibilities:**
- Implement domain service interfaces
- Orchestrate provider calls and business workflows
- Manage parallel processing and concurrency
- Format output for different consumers
- Handle audio candidate generation and selection

**Service Provider Pattern:**
- Uses `uses` directive for dynamic provider discovery
- Enables runtime provider selection and configuration
- Supports dependency injection patterns

### zh-learn-cli (User Interface)

```mermaid
classDiagram
    class CLIModule {
        <<module>>
        +requires com.zhlearn.domain
        +requires com.zhlearn.infrastructure
        +requires com.zhlearn.application
        +exports com.zhlearn.cli
    }

    class Commands {
        MainCommand
        WordCommand
        AudioCommand
        ParsePlecoCommand
        ParseAnkiCommand
        AudioSelectCommand
        ProvidersCommand
    }

    class Utilities {
        TerminalFormatter
        AnalysisPrinter
        InteractiveAudioUI
    }

    class Dependencies {
        Picocli_Framework
        JLine_Terminal
        ANSI_Colors
        Native_Image_Support
    }

    CLIModule --> Commands
    CLIModule --> Utilities
    CLIModule --> Dependencies
```

**Responsibilities:**
- Define CLI commands and option parsing
- Handle user interaction and terminal I/O
- Format output for console display
- Manage application lifecycle and resource cleanup
- Provide interactive features (audio selection, progress display)

**CLI Framework Integration:**
- **Picocli**: Command-line parsing and help generation
- **JLine**: Advanced terminal capabilities
- **ANSI Colors**: Rich console formatting
- **Native Image**: GraalVM compilation support

### zh-learn-pinyin (Utilities)

```mermaid
classDiagram
    class PinyinModule {
        <<module>>
        +requires com.zhlearn.domain
        +exports com.zhlearn.pinyin
    }

    class PinyinUtilities {
        PinyinToneConverter
        ToneMarkProcessor
        NumberedPinyinParser
    }

    class ToneConversion {
        numbered_to_marks()
        marks_to_numbered()
        validate_pinyin()
        normalize_format()
    }

    PinyinModule --> PinyinUtilities
    PinyinUtilities --> ToneConversion
```

**Responsibilities:**
- Convert between pinyin formats (numbered ↔ tone marks)
- Validate pinyin syntax and structure
- Normalize pinyin representations
- Provide utilities for pinyin processing across the system

**Standalone Design:**
- Self-contained utility module
- Minimal dependencies (only domain for types)
- Reusable across different contexts
- Could be extracted as separate library

## Module Boundary Enforcement

### Dependency Validation

```mermaid
flowchart TD
    A[JPMS Compiler] --> B[Module Dependencies Check]
    B --> C{Valid Dependencies?}
    C -->|Yes| D[Compilation Success]
    C -->|No| E[Compilation Error]

    E --> F[Circular Dependency]
    E --> G[Unauthorized Access]
    E --> H[Missing Exports]
    E --> I[Invalid Requires]

    F --> J[Refactor Module Structure]
    G --> K[Add Proper Exports]
    H --> L[Update module-info.java]
    I --> L
```

### Export Control Strategy

```mermaid
flowchart TD
    A[Module Exports] --> B[Public API Packages]
    A --> C[Internal Implementation]

    B --> D[Exported Packages]
    C --> E[Non-exported Packages]

    D --> F[Other Modules Can Access]
    E --> G[Module-internal Only]

    F --> H[Interface Contracts]
    F --> I[Public Models]
    F --> J[Factory Classes]

    G --> K[Implementation Details]
    G --> L[Internal Utilities]
    G --> M[Private State]
```

**Export Principles:**
- **Minimal Exports**: Only export what's necessary for consumers
- **Interface Focus**: Export interfaces, not implementations when possible
- **Stable Contracts**: Exported APIs should be stable and well-documented
- **Implementation Hiding**: Keep internal details unexported

## Module Communication Patterns

### Service Provider Interface (SPI)

```mermaid
sequenceDiagram
    participant App as Application Module
    participant Domain as Domain Module
    participant Infra as Infrastructure Module
    participant JVM as Java Module System

    App->>Domain: uses AudioProvider
    Domain->>JVM: Register service interface
    Infra->>JVM: provides AudioProvider implementations
    JVM->>App: ServiceLoader.load(AudioProvider.class)
    App->>App: Select provider by name
    App->>Infra: Use selected provider
```

### Factory Pattern Integration

```mermaid
flowchart TD
    A[CLI Module] --> B[MainCommand Factory Methods]
    B --> C[Infrastructure Factories]
    C --> D[Provider Implementations]

    B --> E[createExampleProvider()]
    B --> F[createAudioProvider()]
    B --> G[createPinyinProvider()]

    E --> H[AIProviderFactory]
    F --> I[Direct Provider List]
    G --> J[Switch Statement]

    H --> K[ConfigurableProvider]
    I --> L[Provider Instance]
    J --> L

    K --> M[Domain Interface]
    L --> M
```

**Benefits:**
- Loose coupling between modules
- Runtime provider selection
- Easy testing with mock implementations
- Clear separation of creation and usage

## Build System Integration

### Maven Module Configuration

```mermaid
flowchart TD
    A[Parent POM] --> B[Module POMs]
    B --> C[zh-learn-domain]
    B --> D[zh-learn-pinyin]
    B --> E[zh-learn-infrastructure]
    B --> F[zh-learn-application]
    B --> G[zh-learn-cli]

    C --> H[No External Dependencies]
    D --> I[Domain + Minimal Utils]
    E --> J[Many External Dependencies]
    F --> K[Domain + Infrastructure]
    G --> L[All Modules + CLI Framework]

    H --> M[Pure Business Logic]
    I --> N[Utility Functions]
    J --> O[External Integration]
    K --> P[Service Orchestration]
    L --> Q[User Interface]
```

### Compilation Order and Dependencies

```mermaid
gantt
    title Module Compilation Order
    dateFormat X
    axisFormat %d

    section Compilation
    Domain         :1, 2
    Pinyin         :2, 3
    Infrastructure :3, 4
    Application    :4, 5
    CLI           :5, 6
```

**Build Characteristics:**
- **Parallel Builds**: Independent modules can compile concurrently
- **Incremental Builds**: Only changed modules need recompilation
- **Dependency Management**: Maven enforces module dependency order
- **Artifact Generation**: Each module produces its own JAR

## Testing Strategy per Module

### Module-Specific Testing Approaches

```mermaid
flowchart TD
    A[Testing Strategy] --> B[Domain Module]
    A --> C[Infrastructure Module]
    A --> D[Application Module]
    A --> E[CLI Module]
    A --> F[Pinyin Module]

    B --> G[Unit Tests Only]
    C --> H[Unit + Integration Tests]
    D --> I[Unit + Service Tests]
    E --> J[Command Tests + E2E]
    F --> K[Unit Tests Only]

    G --> L[Pure Logic Testing]
    H --> M[External API Testing]
    I --> N[Orchestration Testing]
    J --> O[User Workflow Testing]
    K --> P[Utility Function Testing]
```

### Test Module Organization

```java
// Test module-info.java example
module com.zhlearn.infrastructure.test {
    requires com.zhlearn.infrastructure;
    requires com.zhlearn.domain;
    requires org.junit.jupiter.api;
    requires org.mockito.core;
    requires org.assertj.core;

    // Test-specific exports for test utilities
    exports com.zhlearn.infrastructure.test.utils;
}
```

## Performance Implications

### Module Loading Characteristics

```mermaid
flowchart TD
    A[Application Startup] --> B[Module Graph Resolution]
    B --> C[Dependency Loading]
    C --> D[Service Provider Discovery]
    D --> E[Runtime Optimization]

    B --> F[Compile-time Validation]
    C --> G[Reduced Classpath Scanning]
    D --> H[Explicit Service Loading]
    E --> I[JIT Optimization Benefits]

    F --> J[Faster Startup]
    G --> J
    H --> J
    I --> J
```

**JPMS Performance Benefits:**
- **Reduced Startup Time**: Explicit dependencies eliminate classpath scanning
- **Memory Efficiency**: Only required modules are loaded
- **Better JIT Optimization**: Module boundaries enable better code optimization
- **Reliable Resolution**: Compile-time dependency validation prevents runtime errors

### Native Image Compilation

```mermaid
flowchart TD
    A[GraalVM Native Image] --> B[Static Analysis]
    B --> C[Module Graph]
    C --> D[Reachability Analysis]
    D --> E[Dead Code Elimination]
    E --> F[Optimized Native Binary]

    C --> G[JPMS Helps Analysis]
    G --> H[Clear Module Boundaries]
    G --> I[Explicit Dependencies]
    G --> J[Reduced Reflection]

    H --> K[Better Optimization]
    I --> K
    J --> K
```

## Migration and Evolution Strategy

### Module Boundary Evolution

```mermaid
flowchart TD
    A[Module Evolution] --> B[Adding Exports]
    A --> C[Splitting Modules]
    A --> D[Merging Modules]
    A --> E[Changing Dependencies]

    B --> F[Backward Compatible]
    C --> G[Requires Coordination]
    D --> H[Major Version Change]
    E --> I[Careful Planning Required]

    F --> J[Safe Change]
    G --> K[Breaking Change]
    H --> K
    I --> K
```

### Versioning Strategy

```mermaid
flowchart LR
    A[Module Versioning] --> B[Semantic Versioning]
    B --> C[Major: Breaking API Changes]
    B --> D[Minor: New Features]
    B --> E[Patch: Bug Fixes]

    C --> F[Module Split/Merge]
    C --> G[Export Removal]
    C --> H[Interface Changes]

    D --> I[New Exports]
    D --> J[New Implementations]

    E --> K[Bug Fixes]
    E --> L[Performance Improvements]
```

## Security and Encapsulation

### Module Security Benefits

```mermaid
flowchart TD
    A[JPMS Security] --> B[Strong Encapsulation]
    A --> C[Explicit Dependencies]
    A --> D[Reduced Attack Surface]

    B --> E[Internal Implementation Hidden]
    B --> F[Controlled API Access]
    B --> G[Prevention of Reflection Abuse]

    C --> H[No Hidden Dependencies]
    C --> I[Clear Trust Boundaries]

    D --> J[Only Exported APIs Accessible]
    D --> K[Reduced Classpath Pollution]
```

### Access Control Matrix

| Module | Domain | Infrastructure | Application | CLI | Pinyin |
|--------|--------|---------------|-------------|-----|--------|
| **Domain** | ✓ | ✗ | ✗ | ✗ | ✗ |
| **Infrastructure** | ✓ | ✓ | ✗ | ✗ | ✓ |
| **Application** | ✓ | ✓ | ✓ | ✗ | ✓ |
| **CLI** | ✓ | ✓ | ✓ | ✓ | ✓ |
| **Pinyin** | ✓ | ✗ | ✗ | ✗ | ✓ |

**Legend:**
- ✓ = Can depend on/access
- ✗ = Cannot depend on/access

This modular architecture provides strong boundaries, clear responsibilities, and maintainable code organization while leveraging the full power of the Java Platform Module System for security, performance, and reliability.