# ZH-Learn Architecture Documentation

## Overview

ZH-Learn is a comprehensive Chinese language learning tool built with a modular, extensible architecture. The system provides AI-powered linguistic analysis, pronunciation services, and vocabulary management through a clean CLI interface. The architecture emphasizes modularity, clean boundaries, and extensibility while maintaining high performance and reliability.

## System Architecture

### High-Level System Overview

```mermaid
flowchart TB
    subgraph "User Interface Layer"
        CLI[CLI Commands]
        TUI[Terminal UI]
        Native[Native Binary]
    end

    subgraph "Application Layer"
        WAS[WordAnalysisService]
        AO[AudioOrchestrator]
        PWS[ParallelWordService]
        Fmt[Formatters]
    end

    subgraph "Domain Layer"
        Models[Domain Models]
        Interfaces[Provider Interfaces]
        Services[Service Contracts]
    end

    subgraph "Infrastructure Layer"
        Providers[Provider Implementations]
        Cache[Caching System]
        IO[File I/O & Parsing]
        Net[Network & APIs]
        TTS[Text-to-Speech]
    end

    subgraph "External Services"
        AI[AI/LLM Services]
        Audio[Audio Services]
        Dict[Dictionary Sources]
    end

    CLI --> WAS
    CLI --> AO
    TUI --> PWS
    Native --> Fmt

    WAS --> Models
    AO --> Interfaces
    PWS --> Services

    Providers --> AI
    Cache --> IO
    Net --> Audio
    TTS --> Dict

    Models -.-> Interfaces
    Interfaces -.-> Services
    Services -.-> Providers
```

## Core Components and Flows

### Primary Use Cases

The system supports three main workflows:

1. **[Single Word Analysis](WORD_COMMAND_FLOW.md)** - Comprehensive analysis of individual Chinese words
2. **[Batch Processing](PARSE_PLECO_FLOW.md)** - Large-scale vocabulary processing with parallel execution
3. **[Audio Lookup](AUDIO_COMMAND_FLOW.md)** - Direct pronunciation retrieval and management

### Architectural Layers

#### 1. [Module System (JPMS)](MODULE_ARCHITECTURE.md)

```mermaid
flowchart TD
    A[zh-learn-cli] --> B[zh-learn-application]
    A --> C[zh-learn-infrastructure]
    B --> D[zh-learn-domain]
    B --> C
    C --> D
    C --> E[zh-learn-pinyin]

    style D fill:#e1f5fe,stroke:#01579b
    style C fill:#f3e5f5,stroke:#4a148c
    style B fill:#e8f5e8,stroke:#1b5e20
    style A fill:#fff3e0,stroke:#e65100
    style E fill:#fce4ec,stroke:#880e4f
```

**Module Responsibilities:**
- **Domain**: Pure business logic, immutable models, interfaces
- **Infrastructure**: External integrations, provider implementations
- **Application**: Service orchestration, business workflows
- **CLI**: User interface, command processing
- **Pinyin**: Specialized utilities for pinyin processing

#### 2. [Provider System](PROVIDER_ARCHITECTURE.md)

```mermaid
flowchart LR
    subgraph "Provider Categories"
        AI[AI Providers]
        Audio[Audio Providers]
        Dict[Dictionary Providers]
    end

    subgraph "AI Providers"
        OpenAI[OpenAI]
        DeepSeek[DeepSeek]
        Qwen[Qwen]
        Gemini[Gemini]
        Custom[Custom APIs]
    end

    subgraph "Audio Providers"
        Anki[Anki Media]
        Forvo[Forvo API]
        QwenTTS[Qwen TTS]
        Tencent[Tencent TTS]
    end

    subgraph "Dictionary Providers"
        Pleco[Pleco Export]
        Local[Local Files]
        P4J[Pinyin4j]
    end

    AI --> OpenAI
    AI --> DeepSeek
    AI --> Qwen
    AI --> Gemini
    AI --> Custom

    Audio --> Anki
    Audio --> Forvo
    Audio --> QwenTTS
    Audio --> Tencent

    Dict --> Pleco
    Dict --> Local
    Dict --> P4J
```

## Data Flow Architecture

### Information Processing Pipeline

```mermaid
flowchart TD
    A[Input: Chinese Word] --> B[Domain Model Creation]
    B --> C[Provider Resolution]
    C --> D[Parallel Processing]

    D --> E[Synchronous Phase]
    D --> F[Parallel AI Phase]
    D --> G[Audio Generation Phase]

    E --> H[Pinyin + Definition]
    F --> I[Examples + Explanation + Decomposition]
    G --> J[Audio Candidates]

    H --> K[Combine Results]
    I --> K
    J --> K

    K --> L[WordAnalysis + Audio Candidates]
    L --> M[Format Output]
    M --> N[Display Results]

    L --> O[Interactive Audio Selection]
    O --> P[Final WordAnalysis]
    P --> Q[Export/Cache]
```

### Parallel Processing Strategy

```mermaid
gantt
    title Processing Timeline (New Architecture)
    dateFormat X
    axisFormat %s

    section Foundation
    Pinyin + Definition    :0, 1

    section Parallel Phase
    AI: Examples          :1, 4
    AI: Explanation       :1, 4
    AI: Decomposition     :1, 4
    Audio: Generation     :1, 4

    section Interactive
    Audio Selection       :4, 5
    Final Assembly        :5, 6
```

**Performance Benefits:**
- 40-50% reduction in total processing time
- Better resource utilization
- Improved user experience with immediate feedback
- Resilient to individual provider failures

## Key Architectural Patterns

### 1. Clean Architecture Principles

```mermaid
flowchart TD
    A[External World] --> B[Infrastructure Layer]
    B --> C[Application Layer]
    C --> D[Domain Layer]

    E[Database] --> B
    F[APIs] --> B
    G[File System] --> B

    H[CLI] --> C
    I[Services] --> C

    J[Business Logic] --> D
    K[Core Models] --> D
    L[Interfaces] --> D
```

**Dependencies Flow Inward:**
- Domain has no dependencies
- Infrastructure depends on domain
- Application orchestrates infrastructure via domain interfaces
- CLI coordinates application services

### 2. Provider Pattern

```mermaid
classDiagram
    class Provider {
        <<interface>>
        +getName() String
        +getDescription() String
        +getType() ProviderType
    }

    class ExampleProvider {
        <<interface>>
        +getExamples(Hanzi, Definition) Example
    }

    class ConfigurableExampleProvider {
        -ProviderConfig config
        +getExamples(Hanzi, Definition) Example
    }

    class AIProviderFactory {
        +createExampleProvider(String) ExampleProvider
        +createExplanationProvider(String) ExplanationProvider
        +createDecompositionProvider(String) StructuralDecompositionProvider
    }

    Provider <|-- ExampleProvider
    ExampleProvider <|.. ConfigurableExampleProvider
    AIProviderFactory --> ConfigurableExampleProvider
```

**Benefits:**
- Easy addition of new AI services
- Consistent error handling and retry logic
- Configurable behavior via environment variables
- Test-friendly with dummy implementations

### 3. Service Provider Interface (SPI)

```mermaid
sequenceDiagram
    participant App as Application
    participant JVM as Java Module System
    participant Infra as Infrastructure

    App->>JVM: uses AudioProvider
    Infra->>JVM: provides AudioProvider implementations
    App->>JVM: ServiceLoader.load(AudioProvider.class)
    JVM-->>App: Available providers
    App->>App: Select provider by name
    App->>Infra: Use selected provider
```

**Advantages:**
- Runtime provider discovery
- Loose coupling between layers
- Plugin architecture for extensibility
- Clean separation of concerns

## Command Architecture

### CLI Command Structure

```mermaid
flowchart TD
    A[MainCommand] --> B[WordCommand]
    A --> C[AudioCommand]
    A --> D[ParsePlecoCommand]
    A --> E[ParseAnkiCommand]
    A --> F[AudioSelectCommand]
    A --> G[ProvidersCommand]

    B --> H[Single Word Analysis]
    C --> I[Direct Audio Lookup]
    D --> J[Batch Vocabulary Processing]
    E --> K[Anki Collection Analysis]
    F --> L[Interactive Audio Selection]
    G --> M[Provider Information]

    H --> N[WordAnalysisService]
    I --> O[AudioProvider]
    J --> P[ParallelWordAnalysisService]
    K --> Q[AnkiCardParser]
    L --> R[InteractiveAudioUI]
    M --> S[Provider Factories]
```

### Command Flow Patterns

Each command follows a consistent pattern:

1. **Argument Parsing** - Picocli handles CLI option processing
2. **Provider Resolution** - Factory methods create configured providers
3. **Service Orchestration** - Application services coordinate business logic
4. **Output Formatting** - Results formatted for terminal display
5. **Resource Cleanup** - Proper shutdown of executors and connections

## Concurrency and Performance

### Parallel Processing Architecture

```mermaid
flowchart TD
    A[Main Thread] --> B[ThreadPoolExecutor]
    B --> C[AI Provider Tasks]
    B --> D[Audio Provider Tasks]

    C --> E[CompletableFuture: Examples]
    C --> F[CompletableFuture: Explanations]
    C --> G[CompletableFuture: Decomposition]

    D --> H[CompletableFuture: Anki]
    D --> I[CompletableFuture: Forvo]
    D --> J[CompletableFuture: TTS Services]

    E --> K[Combine Results]
    F --> K
    G --> K
    H --> L[Audio Candidates]
    I --> L
    J --> L

    K --> M[WordAnalysis]
    L --> N[Audio Selection]
    M --> O[Final Result]
    N --> O
```

### Caching Strategy

```mermaid
flowchart TD
    A[Request] --> B{Cache Check}
    B -->|Hit| C[Return Cached Result]
    B -->|Miss| D[Process Request]

    D --> E[Provider Call]
    E --> F[Process Response]
    F --> G[Store in Cache]
    G --> H[Return Result]

    I[Cache Types] --> J[File System Cache]
    I --> K[Audio Cache]
    I --> L[LLM Response Cache]

    J --> M[Provider Responses]
    K --> N[Normalized Audio Files]
    L --> O[AI Analysis Results]
```

**Cache Benefits:**
- Reduced API calls and costs
- Faster subsequent lookups
- Offline availability
- Consistent audio quality via normalization

## Error Handling and Resilience

The project adheres to the fail-fast principle. Providers validate configuration and execute external calls once, surfacing failures immediately with contextual logging. The single exception is Qwen TTS HTTP 429 handling: Helidon Fault Tolerance performs an exponential backoff (5 attempts, 5s base delay, ×3 factor, 15 minute cap). If the service still returns 429 after the final attempt, the provider raises an `IOException` to terminate the command.

## Security and Configuration

### Configuration Management

```mermaid
flowchart TD
    A[Configuration Sources] --> B[Environment Variables]
    A --> C[Command Line Options]
    A --> D[Default Values]

    B --> E[API Keys]
    B --> F[Base URLs]
    B --> G[Feature Flags]

    C --> H[Provider Selection]
    C --> I[Model Specification]
    C --> J[Output Options]

    D --> K[Fallback Providers]
    D --> L[Default Models]
    D --> M[Standard Paths]

    E --> N[Runtime Validation]
    F --> N
    G --> N
    H --> O[Provider Factory]
    I --> O
    J --> O
    K --> P[Graceful Defaults]
    L --> P
    M --> P
```

### Security Considerations

```mermaid
flowchart TD
    A[Security Measures] --> B[API Key Protection]
    A --> C[Input Validation]
    A --> D[Output Sanitization]
    A --> E[Module Encapsulation]

    B --> F[Environment Variables Only]
    B --> G[No Logging of Secrets]
    B --> H[Runtime Validation]

    C --> I[Hanzi Character Validation]
    C --> J[Pinyin Format Validation]
    C --> K[File Path Sanitization]

    D --> L[No PII in Logs]
    D --> M[Sanitized Error Messages]
    D --> N[Safe File Operations]

    E --> O[Strong Module Boundaries]
    E --> P[Controlled API Access]
    E --> Q[Minimal Exports]
```

## Testing Architecture

### Testing Strategy by Layer

```mermaid
flowchart TD
    A[Testing Strategy] --> B[Unit Tests]
    A --> C[Integration Tests]
    A --> D[End-to-End Tests]
    A --> E[Performance Tests]

    B --> F[Domain Logic]
    B --> G[Provider Implementations]
    B --> H[Utility Functions]

    C --> I[API Integrations]
    C --> J[Service Orchestration]
    C --> K[Database Operations]

    D --> L[Command Workflows]
    D --> M[User Scenarios]
    D --> N[Error Handling]

    E --> O[Parallel Processing]
    E --> P[Memory Usage]
    E --> Q[Network Performance]
```

### Test Module Organization

```text
src/test/java/
├── unit/           # Fast, isolated tests
├── integration/    # API and service tests
├── e2e/           # Full workflow tests
└── performance/   # Load and stress tests

src/test/resources/
├── fixtures/      # Test data files
├── features/      # Cucumber scenarios
└── config/        # Test configurations
```

## Deployment and Build

### Build Architecture

```mermaid
flowchart TD
    A[Maven Multi-Module] --> B[Parent POM]
    B --> C[Module POMs]

    C --> D[Dependency Management]
    C --> E[Plugin Configuration]
    C --> F[Build Profiles]

    D --> G[Version Management]
    D --> H[Scope Control]

    E --> I[Compiler Settings]
    E --> J[Test Configuration]
    E --> K[Assembly Rules]

    F --> L[Development Profile]
    F --> M[Native Profile]
    F --> N[Production Profile]
```

### Deployment Options

```mermaid
flowchart TD
    A[Deployment Options] --> B[JVM Application]
    A --> C[Native Binary]
    A --> D[Container Image]

    B --> E[Modular JAR]
    B --> F[Fat JAR]

    C --> G[GraalVM Native Image]
    C --> H[Platform-Specific Binary]

    D --> I[Docker Container]
    D --> J[OCI Image]

    E --> K[Fast Development]
    F --> L[Simple Distribution]
    G --> M[Fast Startup]
    H --> N[No JVM Required]
    I --> O[Containerized Deployment]
    J --> P[Cloud Native]
```

## Performance Characteristics

### System Performance Metrics

```mermaid
gantt
    title Performance Comparison: Sequential vs Parallel
    dateFormat X
    axisFormat %s

    section Sequential Processing
    Word 1 (AI + Audio)     :0, 6
    Word 2 (AI + Audio)     :6, 12
    Word 3 (AI + Audio)     :12, 18
    Audio Selection         :18, 20

    section Parallel Processing (Old)
    Words 1-3 (AI only)     :0, 4
    Audio Generation        :4, 12
    Audio Selection         :12, 14

    section Parallel Processing (New)
    Words 1-3 (AI + Audio) :0, 6
    Audio Selection        :6, 8
```

**Performance Improvements:**
- **40-50% faster** overall processing for batch operations
- **Immediate feedback** during parallel processing
- **Better resource utilization** with concurrent API calls
- **Reduced waiting time** with pre-generated audio candidates

### Memory Usage Patterns

```mermaid
flowchart TD
    A[Memory Usage] --> B[Heap Memory]
    A --> C[Off-Heap Memory]
    A --> D[Native Memory]

    B --> E[Domain Objects]
    B --> F[Provider Instances]
    B --> G[Cache Storage]

    C --> H[Audio File Buffers]
    C --> I[HTTP Connection Pools]

    D --> J[Native Image Optimizations]
    D --> K[Direct Memory Buffers]
```

## Future Architecture Considerations

### Extensibility Points

```mermaid
flowchart TD
    A[Extension Points] --> B[New Providers]
    A --> C[New Commands]
    A --> D[New Output Formats]
    A --> E[New Data Sources]

    B --> F[AI/LLM Services]
    B --> G[Audio Services]
    B --> H[Dictionary Sources]

    C --> I[Batch Operations]
    C --> J[Interactive Features]
    C --> K[Import/Export]

    D --> L[JSON/XML Export]
    D --> M[PDF Generation]
    D --> N[Web Interface]

    E --> O[Online Dictionaries]
    E --> P[Language Corpora]
    E --> Q[User Collections]
```

### Scalability Considerations

```mermaid
flowchart TD
    A[Scalability] --> B[Horizontal Scaling]
    A --> C[Vertical Scaling]
    A --> D[Performance Optimization]

    B --> E[Distributed Processing]
    B --> F[Service Mesh]
    B --> G[Load Balancing]

    C --> H[More CPU Cores]
    C --> I[More Memory]
    C --> J[Faster Storage]

    D --> K[Caching Improvements]
    D --> L[Algorithm Optimization]
    D --> M[Resource Pooling]
```

## Documentation Structure

This architecture documentation is organized into focused documents:

- **[ARCHITECTURE.md](ARCHITECTURE.md)** (this document) - High-level system overview
- **[MODULE_ARCHITECTURE.md](MODULE_ARCHITECTURE.md)** - JPMS module structure and boundaries
- **[PROVIDER_ARCHITECTURE.md](PROVIDER_ARCHITECTURE.md)** - Provider system design and patterns
- **[WORD_COMMAND_FLOW.md](WORD_COMMAND_FLOW.md)** - Single word analysis workflow
- **[AUDIO_COMMAND_FLOW.md](AUDIO_COMMAND_FLOW.md)** - Audio lookup and management
- **[PARSE_PLECO_FLOW.md](PARSE_PLECO_FLOW.md)** - Batch processing with parallel optimization

Each document provides detailed diagrams, implementation patterns, and specific technical guidance for that architectural aspect.

## Conclusion

The ZH-Learn architecture successfully balances modularity, performance, and extensibility through:

- **Clean modular boundaries** enforced by JPMS
- **Flexible provider system** enabling easy integration of new services
- **Parallel processing optimization** for improved performance
- **Robust error handling** with retry mechanisms and graceful degradation
- **Comprehensive caching** for efficiency and offline capability
- **User-centric design** with interactive features and clear feedback

This architecture supports the current feature set while providing clear extension points for future growth and enhancement.
