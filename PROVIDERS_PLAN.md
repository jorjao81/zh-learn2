# LLM Provider Architecture Refactoring Plan

> Status: Completed on 2025-09-19. See `com.zhlearn.infrastructure.llm.LlmProviders` and `com.zhlearn.cli.MainCommand` for the config-driven provider registration now in place.

## Executive Summary

The current LLM provider architecture suffers from massive over-engineering, with **60+ nearly identical provider classes** that differ only in configuration parameters. This plan proposes reducing the codebase from 60+ classes to approximately 6-8 classes through instance-based configuration rather than subclass proliferation.

## Current Architecture Problems

### 1. Class Explosion
- **60+ provider classes** that are essentially identical
- Each LLM model requires 3+ separate classes:
  - `{Model}ExampleProvider`
  - `{Model}ExplanationProvider`
  - `{Model}StructuralDecompositionProvider`
- Examples: `DeepSeekExampleProvider`, `GPT5NanoExampleProvider`, `Qwen3MaxExampleProvider`, etc.

### 2. Massive Code Duplication
Every provider class follows this identical pattern:
```java
public class DeepSeekExampleProvider implements ExampleProvider {
    private final GenericChatModelProvider<Example> provider;

    public DeepSeekExampleProvider() {
        this.provider = new GenericChatModelProvider<>(DeepSeekConfig.forExamples());
    }

    @Override
    public String getName() { return provider.getName(); }

    @Override
    public String getDescription() { return "DeepSeek AI-powered example provider..."; }

    @Override
    public Example getExamples(Hanzi word, Optional<String> definition) {
        return provider.process(word, definition);
    }
}
```

### 3. Configuration Boilerplate
Config classes have repetitive factory methods:
- `DeepSeekConfig.forExamples()`, `DeepSeekConfig.forExplanation()`, `DeepSeekConfig.forStructuralDecomposition()`
- `OpenAIConfig.forGPT5NanoExamples()`, `OpenAIConfig.forGPT5NanoExplanation()`, etc.

### 4. Service Registration Overhead
Each class requires META-INF/services registration, leading to many files with long lists of nearly identical classes.

### 5. Maintenance Nightmare
Adding a new model requires:
1. Creating 3+ new provider classes
2. Adding config factory methods
3. Updating service registration files
4. Writing nearly identical tests

## Core Insight

**The differences between providers are purely configuration-driven, not behavior-driven.**

Only these parameters change:
- Base URL (`https://api.deepseek.com/v1` vs `https://api.openai.com/v1`)
- Model name (`deepseek-chat` vs `gpt-5-nano`)
- API key environment variables (`DEEPSEEK_API_KEY` vs `OPENAI_API_KEY`)
- Temperature/maxTokens settings
- Provider name and description text

The **behavior** is identical across all providers - they all delegate to `GenericChatModelProvider`.

## Proposed Architecture (Simplified)

The original plan was overengineered with `ProviderRegistry` + `ProviderDefinition` + `ProviderFactory`. A much simpler approach is to create providers explicitly and pass them down.

### 1. Simple Configuration Record

Replace complex factory methods with a simple record (Java 24):

```java
public record ProviderConfig(
    String name,
    String description,
    String baseUrl,
    String modelName,
    String apiKey,
    Double temperature,
    Integer maxTokens
) {
    // Records provide constructor, getters, equals, hashCode, toString automatically
    // No constructor reference needed - we'll call constructors directly!

    public com.zhlearn.infrastructure.common.ProviderConfig<T> toInternalConfig(Class<T> type) {
        // Convert to the internal ProviderConfig that GenericChatModelProvider expects
        return new com.zhlearn.infrastructure.common.ProviderConfig<>(
            apiKey, baseUrl, modelName, temperature, maxTokens,
            getTemplateResourcePath(type), getExamplesResourcePath(type),
            getResponseMapper(type), name, getErrorMessagePrefix()
        );
    }
}
```

### 2. Direct Provider Creation

Skip configurable wrapper classes entirely - just call constructors directly:

```java
public class MainCommand implements Runnable {
    private final ExampleProvider exampleProvider;
    private final ExplanationProvider explanationProvider;
    private final StructuralDecompositionProvider decompositionProvider;

    public MainCommand() {
        // Create providers directly - no wrapper classes needed!
        var deepSeekConfig = new ProviderConfig<>(
            "deepseek-chat",
            "DeepSeek AI provider",
            "https://api.deepseek.com/v1",
            "deepseek-chat",
            readEnv("DEEPSEEK_API_KEY"),
            0.3,
            8000
        );

        // Just call the constructors directly
        exampleProvider = new GenericChatModelProvider<>(deepSeekConfig.toInternalConfig(Example.class));
        explanationProvider = new GenericChatModelProvider<>(deepSeekConfig.toInternalConfig(Explanation.class));
        decompositionProvider = new GenericChatModelProvider<>(deepSeekConfig.toInternalConfig(StructuralDecomposition.class));

        // Or for ChatGLM/z.ai:
        // var chatGLMConfig = new ProviderConfig<>(
        //     "glm-4-flash",
        //     "ChatGLM (z.ai) provider",
        //     "https://api.z.ai/openai/v1",
        //     "glm-4-flash",
        //     readEnv("ZAI_API_KEY"),
        //     0.3,
        //     8000
        // );
        // exampleProvider = new ZhipuChatModelProvider<>(chatGLMConfig.toInternalConfig(Example.class));
    }
}
```

### 4. Simple Provider Access

Since services only need one provider of each type:

```java
public class MainCommand {
    public ExampleProvider getExampleProvider() { return exampleProvider; }
    public ExplanationProvider getExplanationProvider() { return explanationProvider; }
    public StructuralDecompositionProvider getDecompositionProvider() { return decompositionProvider; }
}
```

### 5. Pass Single Providers to Services

Services just take the single provider they need:

```java
public class WordAnalysisServiceImpl {
    private final ExampleProvider exampleProvider;
    private final ExplanationProvider explanationProvider;
    private final StructuralDecompositionProvider decompositionProvider;

    public WordAnalysisServiceImpl(ExampleProvider exampleProvider,
                                  ExplanationProvider explanationProvider,
                                  StructuralDecompositionProvider decompositionProvider) {
        this.exampleProvider = exampleProvider;
        this.explanationProvider = explanationProvider;
        this.decompositionProvider = decompositionProvider;
    }
}
```

### 7. No Service Registration Needed

Since providers are created explicitly in `MainCommand`, we can **eliminate META-INF/services files entirely**. No more ServiceLoader, no more service registration - just explicit, easy-to-understand provider creation.

## Handling Special Cases: ChatGLM/z.ai

The refactoring preserves the custom `ZaiOpenAiChatModel` and `ZhipuChatModelProvider` implementations while still eliminating class duplication:

### Current ChatGLM Architecture
- **Custom `ZaiOpenAiChatModel`**: Handles multiple base URL candidates, custom headers (`X-API-Key`), and special endpoint logic
- **Custom `ZhipuChatModelProvider`**: Nearly identical to `GenericChatModelProvider` but uses `ZaiOpenAiChatModel`
- **Still has class explosion**: `ChatGLMExampleProvider`, `ChatGLMExplanationProvider`, `ChatGLM45ExampleProvider`, etc.

### After Refactoring
- **Keep custom implementations**: `ZaiOpenAiChatModel` and `ZhipuChatModelProvider` remain unchanged
- **Eliminate class duplication**: Only one `ConfigurableExampleProvider` handles both OpenAI-compatible and z.ai providers
- **Engine-based routing**: The `ProviderEngine.ZAI_CUSTOM` flag routes to the appropriate provider implementation

### Key Insight
Even with custom `ChatModel` implementations, the **provider wrapper classes** are still identical boilerplate. The refactoring eliminates the wrapper duplication while preserving the necessary custom logic.

## Benefits of Simplified Approach

### 1. Massive Code Reduction
- **From 60+ classes to ~2-3 classes** (98% reduction!)
- **From 1000+ lines of boilerplate to ~100 lines**

### 2. Ultimate Simplicity
- **No ServiceLoader magic** - explicit provider creation
- **No complex registry pattern** - just single providers
- **No factory abstraction** - providers created directly
- **No wrapper classes** - call constructors directly
- **No helper methods** - records created in-place
- **Easy to understand** - you can see exactly what providers are created
- **Records eliminate boilerplate** - immutable configs with no getter/setter/equals code

### 3. Simplified Maintenance
Adding new model:
- **Before**: Create 3+ classes, update configs, update service registration
- **After**: Just change the ProviderConfig record creation in MainCommand - everything in one place

### 4. Better Testability
- **Easy to test** - just pass in mock constructor references in test configs
- **No complex mocking** - no registry/factory to mock
- **Straightforward** - test configurable providers with different configs and constructor references

### 5. Configuration Flexibility
- **Easy to configure** - just change the record parameters in MainCommand
- **No hidden behavior** - all provider creation is explicit and in one place
- **Simple debugging** - easy to see which providers are created and why
- **Everything in one place** - config + constructor reference together

### 6. Cleaner Module Structure
```
zh-learn-infrastructure/
├── common/
│   ├── ProviderConfig.java                       # Simple config record (Java 24)
│   ├── GenericChatModelProvider.java             # Keep existing
│   └── ZhipuChatModelProvider.java               # Keep existing
└── (No META-INF/services files needed!)
└── (No ProviderEngine enum needed!)
└── (No wrapper classes needed!)
└── (Records eliminate boilerplate!)
```

**From 60+ classes down to just 3 classes!**

## Migration Strategy (Simplified)

### Phase 1: Create New Architecture (Parallel)
1. Implement `ProviderConfig` record (Java 24) with `toInternalConfig()` method
2. Extract `BaseChatModelProvider` interface (if not already existing)
3. Ensure all tests pass with direct constructor calls

### Phase 2: Replace Provider Creation
1. Update `MainCommand` to create providers directly with constructor calls
2. Update services to take single providers instead of registry
3. Update tests to pass providers directly
4. Verify backwards compatibility

### Phase 3: Remove Old Architecture
1. Delete 60+ old provider classes (ConfigurableXxxProvider not needed!)
2. Delete repetitive config factory classes
3. Delete `ProviderRegistry` class
4. Remove META-INF/services files
5. Remove old tests

### Phase 4: Clean Up
1. Remove unused imports and dependencies
2. Update documentation
3. Verify all functionality works

## Risk Mitigation

### 1. Backwards Compatibility
- Keep provider names identical
- Ensure identical behavior
- Maintain same API surface

### 2. Testing Strategy
- Comprehensive integration tests
- Provider-specific behavior tests
- Performance benchmarks

### 3. Rollback Plan
- Keep old classes during migration
- Feature flags for new vs old architecture
- Gradual migration per provider type

## Implementation Estimate (Simplified)

- **Phase 1**: 1-2 days (much simpler than registry/factory approach)
- **Phase 2**: 1 day (straightforward provider list updates)
- **Phase 3**: 1 day (delete old classes)
- **Phase 4**: 0.5 days (cleanup)
- **Total**: 3.5-4.5 days (faster than original plan)

## Conclusion

This **simplified** refactoring approach transforms the provider architecture from a maintenance nightmare into a clean, understandable system. The 95% code reduction, dramatically improved maintainability, and simplified testing make this a high-value refactoring.

### Key Insights
1. **Configuration differences should not drive class proliferation** - use instance-based configuration
2. **Registry/Factory patterns are overkill** - explicit provider creation is simpler and clearer
3. **ServiceLoader magic is unnecessary** - explicit creation is easier to understand and debug
4. **Enums are unnecessary** - just choose the constructor to call
5. **Lists are overkill** - services only need one provider of each type
6. **Helper methods are unnecessary** - create records in-place for maximum clarity
7. **Wrapper classes are unnecessary** - just call constructors directly
8. **Records eliminate configuration boilerplate** - immutable, clean, auto-generated methods
9. **Even custom implementations (ChatGLM/z.ai) can be unified** - just call different constructors

By embracing **direct constructor calls with simple config records** over complex abstraction layers, we achieve the same functionality with dramatically less code and much better maintainability.
