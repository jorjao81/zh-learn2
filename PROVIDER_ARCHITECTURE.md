# Provider Architecture Documentation

## Overview

The zh-learn provider system implements a flexible, pluggable architecture for linguistic analysis and pronunciation services. Providers are organized into domain interfaces with multiple implementations, enabling easy switching between local, dictionary-based, and AI-powered services.

## Provider Categories

### Core Provider Types

```mermaid
flowchart TD
    A[Provider System] --> B[Analysis Providers]
    A --> C[Audio Providers]
    A --> D[Utility Providers]

    B --> E[ExampleProvider]
    B --> F[ExplanationProvider]
    B --> G[StructuralDecompositionProvider]
    B --> H[DefinitionFormatterProvider]

    C --> I[AudioProvider]

    D --> J[PinyinProvider]
    D --> K[DefinitionProvider]

    E --> L[AI-Powered]
    F --> L
    G --> L
    H --> L

    I --> M[File-Based + API-Based]

    J --> N[Dictionary-Based]
    K --> N
```

## Domain Interface Design

### Provider Interface Hierarchy

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

    class ExplanationProvider {
        <<interface>>
        +getExplanation(Hanzi, Definition) Explanation
    }

    class StructuralDecompositionProvider {
        <<interface>>
        +getStructuralDecomposition(Hanzi) StructuralDecomposition
    }

    class AudioProvider {
        <<interface>>
        +getPronunciation(Hanzi, Pinyin) Optional~Path~
        +getPronunciations(Hanzi, Pinyin) List~Path~
        +getPronunciationsWithDescriptions(Hanzi, Pinyin) List~PronunciationDescription~
    }

    class PinyinProvider {
        <<interface>>
        +getPinyin(Hanzi) Pinyin
    }

    class DefinitionProvider {
        <<interface>>
        +getDefinition(Hanzi) Definition
    }

    Provider <|-- ExampleProvider
    Provider <|-- ExplanationProvider
    Provider <|-- StructuralDecompositionProvider
    Provider <|-- AudioProvider
    Provider <|-- PinyinProvider
    Provider <|-- DefinitionProvider
```

### Provider Type Classification

```mermaid
flowchart LR
    A[ProviderType Enum] --> B[AI]
    A --> C[DICTIONARY]
    A --> D[AUDIO]
    A --> E[CACHE]

    B --> F[LLM-powered analysis]
    C --> G[Local data lookup]
    D --> H[Pronunciation services]
    E --> I[Caching layer]
```

## AI Provider System

### AIProviderFactory Architecture

```mermaid
flowchart TD
    A[AIProviderFactory] --> B[Provider Name Resolution]
    B --> C{Provider Type}

    C -->|openai| D[OpenAI Configuration]
    C -->|deepseek| E[DeepSeek Configuration]
    C -->|qwen| F[Qwen Configuration]
    C -->|glm| G[Zhipu Configuration]
    C -->|openrouter| H[OpenRouter Configuration]
    C -->|gemini| I[Gemini Configuration]
    C -->|dummy| J[Dummy Provider]

    D --> K[Create OpenAI ChatModel]
    E --> L[Create OpenAI-compatible ChatModel]
    F --> L
    G --> M[Create Custom Zhipu Client]
    H --> N[Create OpenRouter ChatModel]
    I --> O[Create Gemini ChatModel]
    J --> P[Create Static Response Provider]

    K --> Q[Wrap in ConfigurableProvider]
    L --> Q
    M --> R[Wrap in Custom Provider]
    N --> Q
    O --> Q
    P --> S[Direct Provider Instance]

    Q --> T[Ready for Use]
    R --> T
    S --> T
```

### Configurable Provider Pattern

```mermaid
classDiagram
    class ConfigurableExampleProvider {
        -ProviderConfig~Example~ config
        -String providerName
        -String description
        +getExamples(Hanzi, Definition) Example
        +getName() String
        +getDescription() String
    }

    class ProviderConfig~T~ {
        -String apiKey
        -String baseUrl
        -String model
        -Path templatePath
        -Path outputDirectory
        -ResponseMapper~T~ responseMapper
        -String errorContext
        +process(Hanzi, Definition) T
    }

    class ResponseMapper~T~ {
        <<interface>>
        +mapResponse(String) T
    }

    class ChatModelProcessor~T~ {
        -ChatLanguageModel chatModel
        -PromptTemplate template
        +process(Hanzi, Definition) T
    }

    ConfigurableExampleProvider --> ProviderConfig
    ProviderConfig --> ResponseMapper
    ProviderConfig --> ChatModelProcessor
```

**Benefits of Configurable Pattern:**
- Consistent error handling across all AI providers
- Standardized prompt template loading
- Unified response parsing and validation
- Centralized caching and logging
- Easy addition of new LLM providers

### Provider Configuration Flow

```mermaid
sequenceDiagram
    participant Factory as AIProviderFactory
    participant Config as ProviderConfig
    participant Client as HTTP Client
    participant LLM as LangChain4j ChatModel
    participant Template as PromptTemplate
    participant Mapper as ResponseMapper

    Factory->>Config: createProviderConfig(apiKey, baseUrl, model, ...)
    Config->>Client: createHttpClient(apiKey, baseUrl)
    Config->>LLM: createChatModel(client, model)
    Config->>Template: loadTemplate(templatePath)
    Config->>Mapper: createResponseMapper(outputDirectory)

    Note over Config: All components configured
    Factory->>Factory: wrap in ConfigurableProvider
    Factory-->>Factory: Return provider instance
```

## Audio Provider Architecture

### Audio Provider Ecosystem

```mermaid
flowchart TD
    A[Audio Provider System] --> B[Local Providers]
    A --> C[API Providers]

    B --> D[AnkiPronunciationProvider]
    C --> E[ForvoAudioProvider]
    C --> F[QwenAudioProvider]
    C --> G[TencentAudioProvider]

    D --> H[File System Scanning]
    E --> I[Community Database]
    F --> J[AI Text-to-Speech]
    G --> K[Professional TTS]

    H --> L[AudioNormalizer]
    I --> M[AudioDownloadExecutor]
    J --> M
    K --> M

    L --> N[AudioCache]
    M --> N
```

### Audio Processing Pipeline

```mermaid
sequenceDiagram
    participant Provider as Audio Provider
    participant Executor as AudioDownloadExecutor
    participant Cache as AudioCache
    participant Normalizer as AudioNormalizer
    participant FS as File System

    Provider->>Provider: Check local cache
    alt Cache miss
        Provider->>Executor: Download/generate audio
        Executor->>Executor: Get audio data (API/TTS)
        Executor->>Cache: ensureCachedNormalized()
        Cache->>Normalizer: Normalize audio
        Normalizer->>FS: Run FFmpeg normalization
        FS-->>Normalizer: Normalized audio file
        Normalizer-->>Cache: Normalized path
        Cache-->>Executor: Cached file path
        Executor-->>Provider: Audio file ready
    else Cache hit
        Provider->>FS: Return cached file path
    end
```

### Audio Caching Strategy

```mermaid
flowchart TD
    A[Audio Request] --> B[Generate Cache Key]
    B --> C[provider_word_voice_hash]
    C --> D{File Exists?}
    D -->|Yes| E[Return Cached Path]
    D -->|No| F[Process Audio]

    F --> G[Download/Generate]
    G --> H[Decode if Needed]
    H --> I{FFmpeg Available?}
    I -->|Yes| J[Normalize Audio]
    I -->|No| K[Copy Raw Audio]
    J --> L[Store in Cache]
    K --> L
    L --> M[Return New Path]

    E --> N[Ready for Use]
    M --> N
```

**Cache Benefits:**
- Eliminates redundant API calls
- Consistent audio quality via normalization
- Faster subsequent lookups
- Reduced bandwidth usage
- Offline availability after first download

## Provider Resolution and Injection

### Dependency Injection Pattern

```mermaid
flowchart TD
    A[MainCommand] --> B[Provider Factory Methods]
    B --> C[Command-Specific Resolution]

    C --> D[WordCommand]
    C --> E[ParsePlecoCommand]
    C --> F[AudioCommand]

    D --> G[Single Provider Per Type]
    E --> H[Configurable Provider Set]
    F --> I[Audio Provider Only]

    G --> J[WordAnalysisServiceImpl]
    H --> J
    I --> J

    J --> K[Provider Interface Calls]
    K --> L[Concrete Provider Implementations]
```

### Provider Lifecycle Management

```mermaid
sequenceDiagram
    participant Main as MainCommand
    participant Factory as Provider Factory
    participant Service as WordAnalysisService
    participant Provider as Concrete Provider
    participant Resource as External Resource

    Main->>Factory: createProvider(name, model)
    Factory->>Factory: Validate configuration
    Factory->>Provider: Initialize with config
    Provider->>Resource: Establish connection/validate

    Main->>Service: Create service with providers
    Service->>Provider: Use provider for analysis
    Provider->>Resource: Make API calls

    Note over Main: Command completion
    Main->>Main: shutdown()
    Main->>Provider: cleanup resources (if needed)
    Provider->>Resource: Close connections
```

## Error Handling and Resilience

### Provider Error Strategy

Qwen TTS continues to follow the fail-fast rule: configuration is validated up front and any non-rate-limit failure immediately propagates as an exception. The only tolerance is for HTTP 429 responses, where we delegate backoff handling to Helidon Fault Tolerance. The Helidon retry policy performs exponential backoff (5s initial, ×3 factor, maximum five attempts within a 15 minute window). If the API still returns 429 after the final attempt, the provider throws an `IOException` and the CLI crashes as required by the constitution.

## Provider Extension Patterns

### Adding New AI Providers

#### Pattern 1: OpenAI-Compatible Provider

```mermaid
flowchart TD
    A[New Provider Requirements] --> B[OpenAI-Compatible API]
    B --> C[Create Provider Config Class]
    C --> D[Add Factory Method Cases]
    D --> E[Environment Variable Setup]
    E --> F[Test Integration]

    C --> G[Define API_KEY_ENV]
    C --> H[Define DEFAULT_BASE_URL]
    C --> I[Implement getApiKey()]
    C --> J[Implement getBaseUrl()]
```

#### Pattern 2: Custom Provider Implementation

```mermaid
flowchart TD
    A[Custom API Requirements] --> B[Implement Custom Client]
    B --> C[Create Provider Wrapper]
    C --> D[Add Factory Support]
    D --> E[Test Custom Flow]

    B --> F[Custom HTTP Handling]
    B --> G[Custom Request/Response Format]
    B --> H[Custom Authentication]
```

### Adding New Audio Providers

```mermaid
flowchart TD
    A[New Audio Provider] --> B[Implement AudioProvider Interface]
    B --> C[Define Provider Metadata]
    C --> D[Implement Core Methods]
    D --> E[Add to MainCommand List]
    E --> F[Test Integration]

    C --> G[getName()]
    C --> H[getDescription()]
    C --> I[getType()]

    D --> J[getPronunciation()]
    D --> K[getPronunciations()]
    D --> L[getPronunciationsWithDescriptions()]
```

## Provider Testing Strategy

### Test Architecture

```mermaid
flowchart TD
    A[Provider Testing] --> B[Unit Tests]
    A --> C[Integration Tests]
    A --> D[Contract Tests]

    B --> E[Mock External APIs]
    B --> F[Test Error Conditions]
    B --> G[Validate Responses]

    C --> H[Real API Calls]
    C --> I[End-to-End Flows]
    C --> J[Performance Testing]

    D --> K[Interface Compliance]
    D --> L[Response Format Validation]
    D --> M[Error Handling Verification]
```

### Test Implementation Patterns

```java
// Unit Test Pattern
@Test
void shouldReturnExamplesForValidInput() {
    // Arrange
    ExampleProvider provider = new DummyExampleProvider();
    Hanzi word = new Hanzi("学习");
    Definition definition = new Definition("to study");

    // Act
    Example result = provider.getExamples(word, definition);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.sentences()).isNotEmpty();
}

// Integration Test Pattern
@Test
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
void shouldCallRealOpenAIAPI() {
    // Real API integration test
    ExampleProvider provider = AIProviderFactory.createExampleProvider("openai");
    // ... test with real API
}
```

## Configuration and Environment Management

### Environment Variable Patterns

```mermaid
flowchart TD
    A[Environment Variables] --> B[API Keys]
    A --> C[Base URLs]
    A --> D[Model Names]
    A --> E[Feature Flags]

    B --> F[OPENAI_API_KEY]
    B --> G[DEEPSEEK_API_KEY]
    B --> H[DASHSCOPE_API_KEY]

    C --> I[OPENAI_BASE_URL]
    C --> J[CUSTOM_API_BASE_URL]

    D --> K[DEFAULT_MODEL]
    D --> L[FALLBACK_MODEL]

    E --> M[ENABLE_CACHING]
    E --> N[DEBUG_PROVIDERS]
```

### Configuration Validation

```mermaid
sequenceDiagram
    participant Factory as AIProviderFactory
    participant Validator as Configuration Validator
    participant Env as Environment

    Factory->>Validator: validateConfiguration(providerName)
    Validator->>Env: checkRequiredEnvVars(provider)
    Env-->>Validator: Environment status

    alt Missing required variables
        Validator-->>Factory: ConfigurationException
        Factory->>Factory: Fail fast with clear message
    else Valid configuration
        Validator-->>Factory: Validation passed
        Factory->>Factory: Proceed with provider creation
    end
```

## Performance and Monitoring

### Provider Performance Metrics

```mermaid
flowchart TD
    A[Provider Metrics] --> B[Response Time]
    A --> C[Success Rate]
    A --> D[Cache Hit Rate]
    A --> E[Error Classification]

    B --> F[Average Latency]
    B --> G[95th Percentile]
    B --> H[Timeout Rate]

    C --> I[Successful Requests]
    C --> J[Failed Requests]
    C --> K[Failure Rate]

    D --> L[Cache Efficiency]
    D --> M[Storage Usage]

    E --> N[Network Errors]
    E --> O[Rate Limit Errors]
    E --> P[Authentication Errors]
```

### Logging Strategy

```mermaid
flowchart TD
    A[Provider Logging] --> B[Request Logging]
    A --> C[Response Logging]
    A --> D[Error Logging]
    A --> E[Performance Logging]

    B --> F[Sanitized Parameters]
    B --> G[Provider Name]
    B --> H[Request Timestamp]

    C --> I[Response Status]
    C --> J[Response Size]
    C --> K[Processing Time]

    D --> L[Error Type]
    D --> M[Error Context]
    D --> N[Stack Trace]

    E --> O[Cache Hit/Miss]
    E --> P[API Quota Usage]
    E --> Q[Concurrent Requests]
```

**Logging Principles:**
- Never log sensitive data (API keys, user content)
- Include sufficient context for debugging
- Use structured logging for analysis
- Separate performance and error logs
- Provide actionable error messages

This comprehensive provider architecture enables flexible, reliable, and extensible linguistic analysis through a clean separation of concerns and robust error handling patterns.
