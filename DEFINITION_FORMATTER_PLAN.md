# Definition Formatter Provider Implementation Plan

## Overview

This plan outlines the implementation of a new AI provider type: **DefinitionFormatterProvider**. This provider follows the same pattern as `ExampleProvider`, `ExplanationProvider`, etc., and can be backed by any AI service (deepseek-chat, glm-4.5, etc.) to format or generate HTML-formatted definitions for Chinese words.

## Background

### Current State
- **AI Provider Pattern**: Providers like `ExampleProvider`, `ExplanationProvider` follow a consistent pattern with `ConfigurableXXXProvider` implementations backed by any AI service
- **AIProviderFactory**: Creates providers with separate single-char and multi-char configurations
- **Resource Structure**: Prompt templates stored in `zh-learn-infrastructure/src/main/resources/{single-char,multi-char}/{provider-type}/prompt-template.md`
- **Parse-pleco command**: Uses various providers through the factory pattern
- **Existing DefinitionProvider**: For *looking up* definitions from data sources (different from formatting)

### Requirements
1. **New AI provider type**: `DefinitionFormatterProvider` that formats/generates definitions using AI
2. **Input**: Chinese word + Optional raw definition (from Pleco or other sources)
3. **AI-powered processing**: Both single-char and multi-char words processed through AI with different prompts
4. **Parse-pleco integration**: Pass Pleco definition when available for AI formatting/enhancement
5. **Provider selection**: Support all AI backends (deepseek-chat, glm-4-flash, glm-4.5, etc.)
6. **Prompt usage**:
   - Single-char: `single-char/definition/prompt-template.md` - minimal formatting of existing definition
   - Multi-char: `multi-char/definition/prompt-template.md` - complex formatting or generation

## Design Architecture

### 1. Domain Layer (`zh-learn-domain`)

#### New Interface: `DefinitionFormatterProvider`
```java
package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import java.util.Optional;

public interface DefinitionFormatterProvider {
    String getName();
    String getDescription();
    ProviderType getType();

    /**
     * Format or generate a definition for the given Chinese word using AI.
     *
     * @param word The Chinese word to format a definition for
     * @param rawDefinition Optional raw definition (typically from Pleco export).
     *                     If present, AI will format it. If empty, AI may generate new definition.
     * @return AI-formatted HTML definition, or null if cannot process
     */
    Definition formatDefinition(Hanzi word, Optional<String> rawDefinition);
}
```

**Key Points**:
- Follows same interface pattern as `ExampleProvider`
- Takes `Optional<String>` raw definition (like `ExampleProvider` takes optional definition)
- Always returns AI-processed content (even for single characters)

### 2. Infrastructure Layer (`zh-learn-infrastructure`)

#### New Implementation: `ConfigurableDefinitionFormatterProvider`
```java
package com.zhlearn.infrastructure.common;

public class ConfigurableDefinitionFormatterProvider implements DefinitionFormatterProvider {
    private final BiFunction<Hanzi, Optional<String>, Definition> singleCharProcessor;
    private final BiFunction<Hanzi, Optional<String>, Definition> multiCharProcessor;
    // ... similar structure to ConfigurableExampleProvider

    @Override
    public Definition formatDefinition(Hanzi word, Optional<String> rawDefinition) {
        WordType type = WordType.from(word);
        return selectProcessor(type).apply(word, rawDefinition);
    }
}
```

This follows the exact same pattern as `ConfigurableExampleProvider`:
- **Single-char processor**: Uses AI with simple formatting prompt
- **Multi-char processor**: Uses AI with complex formatting/generation prompt
- **Constructor variants**: Support different AI backends and configurations

#### New Configuration Classes

**Single-Char Configuration**:
```java
package com.zhlearn.infrastructure.common;

public class SingleCharDefinitionFormatterProviderConfig {
    public static String templatePath() {
        return "single-char/definition/prompt-template.md";
    }

    public static String examplesDirectory() {
        return "single-char/definition/examples";
    }

    public static Function<String, Definition> responseMapper() {
        return response -> new Definition(response.trim());
    }
}
```

**Multi-Char Configuration**:
```java
package com.zhlearn.infrastructure.common;

public class MultiCharDefinitionFormatterProviderConfig {
    public static String templatePath() {
        return "multi-char/definition/prompt-template.md";
    }

    public static String examplesDirectory() {
        return "multi-char/definition/examples";
    }

    public static Function<String, Definition> responseMapper() {
        return response -> new Definition(response.trim());
    }
}
```

#### Updates to AIProviderFactory
Add new method following the exact same pattern as `createExampleProvider`:

```java
public static DefinitionFormatterProvider createDefinitionFormatterProvider(String providerName) {
    if (providerName == null) providerName = "deepseek-chat";

    return switch (providerName) {
        case "dummy" -> new DummyDefinitionFormatterProvider();
        case "deepseek-chat" -> {
            requireAPIKey("DEEPSEEK_API_KEY", providerName);
            ProviderConfig<Definition> singleConfig = createProviderConfig(
                DeepSeekConfig.getApiKey(),
                DeepSeekConfig.getBaseUrl(),
                "deepseek-chat",
                SingleCharDefinitionFormatterProviderConfig.templatePath(),
                SingleCharDefinitionFormatterProviderConfig.examplesDirectory(),
                SingleCharDefinitionFormatterProviderConfig.responseMapper(),
                providerName,
                "Failed to format definition from DeepSeek (deepseek-chat)"
            );
            ProviderConfig<Definition> multiConfig = createProviderConfig(
                DeepSeekConfig.getApiKey(),
                DeepSeekConfig.getBaseUrl(),
                "deepseek-chat",
                MultiCharDefinitionFormatterProviderConfig.templatePath(),
                MultiCharDefinitionFormatterProviderConfig.examplesDirectory(),
                MultiCharDefinitionFormatterProviderConfig.responseMapper(),
                providerName,
                "Failed to format definition from DeepSeek (deepseek-chat)"
            );
            yield new ConfigurableDefinitionFormatterProvider(singleConfig, multiConfig, providerName, "DeepSeek AI-powered definition formatter");
        }
        case "glm-4-flash" -> {
            requireAPIKey("ZHIPU_API_KEY", providerName);
            // Similar pattern with ZhipuChatModelProvider delegate...
        }
        case "glm-4.5" -> {
            // Similar pattern...
        }
        case "qwen-max", "qwen-plus", "qwen-turbo" -> {
            // Similar pattern...
        }
        // ... all other AI providers
        default -> throw new IllegalArgumentException("Unknown definition formatter provider: " + providerName);
    };
}
```

#### Dummy Implementation
```java
package com.zhlearn.infrastructure.dummy;

public class DummyDefinitionFormatterProvider implements DefinitionFormatterProvider {
    @Override
    public Definition formatDefinition(Hanzi word, Optional<String> rawDefinition) {
        String formatted = rawDefinition.orElse("dummy definition for " + word.characters());
        return new Definition("<span class=\"part-of-speech\">dummy</span> " + formatted);
    }
    // ... other methods
}
```

### 3. Application Layer (`zh-learn-application`)

#### Service Integration
Update `WordAnalysisServiceImpl` to optionally use definition formatting:

```java
public class WordAnalysisServiceImpl implements WordAnalysisService {
    private final DefinitionFormatterProvider definitionFormatterProvider; // Optional

    public WordAnalysis getCompleteAnalysis(Hanzi word, ProviderConfiguration config) {
        // Get raw definition from DefinitionProvider
        Definition rawDefinition = definitionProvider.getDefinition(word);

        // Format definition if formatter is available
        Definition finalDefinition = rawDefinition;
        if (definitionFormatterProvider != null) {
            Optional<String> rawText = rawDefinition != null
                ? Optional.of(rawDefinition.meaning())
                : Optional.empty();
            Definition formatted = definitionFormatterProvider.formatDefinition(word, rawText);
            if (formatted != null) {
                finalDefinition = formatted;
            }
        }

        // Use finalDefinition in WordAnalysis...
    }
}
```

#### Configuration Updates
Add `DefinitionFormatterProvider` to `ProviderConfiguration`:

```java
public record ProviderConfiguration(
    String exampleProvider,
    String pinyinProvider,
    String definitionProvider,
    String definitionFormatterProvider, // NEW
    String decompositionProvider,
    String explanationProvider,
    String audioProvider
) {}
```

### 4. CLI Layer (`zh-learn-cli`)

#### Updates to MainCommand
Add definition formatter provider support following existing pattern:

```java
public class MainCommand {
    // Add to createProviders() method:
    public DefinitionFormatterProvider createDefinitionFormatterProvider(String providerName) {
        return AIProviderFactory.createDefinitionFormatterProvider(providerName);
    }
}
```

#### Updates to ParsePlecoCommand
Add new command line option:

```java
@Option(names = {"--definition-formatter-provider"},
        description = "Set specific provider for definition formatting (default: deepseek-chat). Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo",
        defaultValue = "deepseek-chat")
private String definitionFormatterProvider;
```

Update provider configuration and service creation:

```java
// In run() method:
DefinitionFormatterProvider defFormatterProv = parent.createDefinitionFormatterProvider(definitionFormatterProvider);

WordAnalysisServiceImpl baseService = new WordAnalysisServiceImpl(
    exampleProv, explanationProv, decompositionProv, pinyinProv, definitionProv, defFormatterProv, audioProv
);

ProviderConfiguration config = new ProviderConfiguration(
    exampleProvider,
    pinyinProvider,
    definitionProvider,
    definitionFormatterProvider, // NEW
    decompositionProvider,
    explanationProvider,
    audioProvider
);
```

#### Updates to WordCommand
Add the same definition formatter provider option to the regular `word` command for consistency.

### 5. Module Dependencies and Exports

#### `zh-learn-domain/module-info.java`
```java
module zh.learn.domain {
    exports com.zhlearn.domain.provider; // Already exports DefinitionFormatterProvider
}
```

#### `zh-learn-infrastructure/module-info.java`
No new exports needed - uses existing `com.zhlearn.infrastructure.common` and `com.zhlearn.infrastructure.dummy` packages.

## Resource Structure

### Prompt Templates
Following established pattern:

```
zh-learn-infrastructure/src/main/resources/
├── single-char/
│   └── definition/
│       ├── prompt-template.md                    # CREATED
│       └── examples/                             # Future: example inputs/outputs
└── multi-char/
    └── definition/
        ├── prompt-template.md                    # CREATED
        └── examples/                             # Future: example inputs/outputs
```

### Prompt Content

**Single-Char Prompt** (`single-char/definition/prompt-template.md`):
- Takes raw definition from Pleco/dictionary
- Adds minimal HTML formatting (part-of-speech tags, etc.)
- Expands abbreviations
- Does NOT change meaning

**Multi-Char Prompt** (`multi-char/definition/prompt-template.md`):
- Can format existing definitions OR generate new ones
- Supports complex HTML structure with lists, domains, usage notes
- More sophisticated formatting and expansion

## Implementation Strategy

### Phase 1: Core Infrastructure
1. **Create `DefinitionFormatterProvider` interface** in `zh-learn-domain`
2. **Create configuration classes** for single-char and multi-char prompts
3. **Create `ConfigurableDefinitionFormatterProvider`** following existing pattern
4. **Create `DummyDefinitionFormatterProvider`** for testing
5. **Unit tests** for core functionality

### Phase 2: AI Provider Integration
1. **Update `AIProviderFactory`** with `createDefinitionFormatterProvider` method
2. **Add support for all AI providers** (deepseek-chat, glm-4-flash, glm-4.5, qwen variants)
3. **Integration tests** with dummy and real AI providers

### Phase 3: Service Integration
1. **Update `WordAnalysisServiceImpl`** to use definition formatter
2. **Update `ProviderConfiguration`** record
3. **Integration tests** for the complete pipeline

### Phase 4: CLI Integration
1. **Update `MainCommand`** with definition formatter provider support
2. **Update `ParsePlecoCommand`** and `WordCommand` with new option
3. **End-to-end CLI tests**

### Phase 5: Testing and Documentation
1. **Comprehensive test coverage** across all layers
2. **Performance testing** with various AI providers
3. **Update documentation** (CLAUDE.md, README)

## Detailed Behavior Specification

### Single Character Processing
- **Input**: Single Chinese character + Optional Pleco definition
- **AI Processing**: Always uses AI with simple formatting prompt
- **Behavior**:
  - If raw definition exists: AI formats it with HTML tags, expands abbreviations
  - If raw definition is empty: AI may return null or generate minimal definition
  - Uses `single-char/definition/prompt-template.md`

### Multi-Character Processing
- **Input**: Multi-character Chinese word + Optional raw definition
- **AI Processing**: Always uses AI with complex formatting prompt
- **Behavior**:
  - If raw definition exists: AI formats and enhances it significantly
  - If raw definition is empty: AI generates complete new definition
  - Uses `multi-char/definition/prompt-template.md`
  - Supports complex HTML with lists, domains, usage markers

### Integration with Parse-Pleco
- **Provider selection**: Choose AI backend via `--definition-formatter-provider`
- **Default**: "deepseek-chat" (consistent with other providers)
- **Pipeline**:
  1. `DictionaryDefinitionProvider` gets raw definition from Pleco export
  2. `DefinitionFormatterProvider` formats the raw definition using selected AI service
  3. Formatted definition used in final `WordAnalysis`

### Provider Names and Backends
All existing AI providers supported:
- `dummy` - Test implementation
- `deepseek-chat` - DeepSeek Chat API
- `glm-4-flash` - Zhipu GLM-4 Flash
- `glm-4.5` - Zhipu GLM-4.5
- `qwen-max`, `qwen-plus`, `qwen-turbo` - Alibaba Qwen variants

## Error Handling

### Fail-Fast Philosophy Compliance
- **Missing API keys**: Fail immediately during provider creation
- **Invalid provider names**: Fail during factory method call
- **AI provider errors**: Let exceptions bubble up, don't swallow
- **Malformed prompts**: Fail during resource loading

### Graceful Degradation
- **Definition formatter failure**: Fall back to raw definition if available
- **No raw definition + formatter failure**: Return null Definition
- **Invalid AI responses**: Let responseMapper handle validation

## Testing Strategy

### Unit Tests
- `DefinitionFormatterProviderTest` interface compliance tests
- `ConfigurableDefinitionFormatterProviderTest` for single/multi-char logic
- `AIProviderFactoryTest` for new provider creation method
- `DummyDefinitionFormatterProviderTest` for test implementation

### Integration Tests
- End-to-end tests with dummy providers
- Parse-pleco command tests with various AI backends
- WordAnalysisService tests with definition formatting pipeline
- Resource loading tests for prompt templates

### Performance Tests
- AI provider response time measurements
- Parallel processing with definition formatting
- Memory usage with large Pleco export files

## Constitutional Compliance

### Modular Architecture ✓
- Clear module boundaries maintained
- No cross-layer dependencies
- Minimal exports from `module-info.java`
- Follows established provider pattern

### Fail-Fast Philosophy ✓
- No fallbacks unless explicitly requested
- Exceptions bubble up rather than being swallowed
- Fast failure on configuration errors
- Clear error messages

### Test-First Development ✓
- Unit tests for each new component
- Integration tests for provider interactions
- End-to-end CLI tests
- Dummy implementations for testing

### CLI-First Interface ✓
- Command-line options for provider selection
- Interactive terminal support maintained
- Consistent with existing CLI patterns
- Help text follows established format

### Provider Pattern ✓
- Follows established provider interface pattern
- Configurable and swappable implementations
- Clear separation of concerns
- Consistent with ExampleProvider, ExplanationProvider, etc.

## File Structure

```
zh-learn-domain/
└── src/main/java/com/zhlearn/domain/provider/
    └── DefinitionFormatterProvider.java                                          # NEW

zh-learn-infrastructure/
├── src/main/java/com/zhlearn/infrastructure/
│   ├── common/
│   │   ├── AIProviderFactory.java                                                # UPDATED
│   │   ├── ConfigurableDefinitionFormatterProvider.java                          # NEW
│   │   ├── SingleCharDefinitionFormatterProviderConfig.java                      # NEW
│   │   └── MultiCharDefinitionFormatterProviderConfig.java                       # NEW
│   └── dummy/
│       └── DummyDefinitionFormatterProvider.java                                 # NEW
└── src/main/resources/
    ├── single-char/definition/
    │   └── prompt-template.md                                                     # CREATED
    └── multi-char/definition/
        └── prompt-template.md                                                     # CREATED

zh-learn-application/
└── src/main/java/com/zhlearn/application/service/
    ├── WordAnalysisServiceImpl.java                                              # UPDATED
    └── ProviderConfiguration.java                                                # UPDATED (record)

zh-learn-cli/
└── src/main/java/com/zhlearn/cli/
    ├── MainCommand.java                                                           # UPDATED
    ├── ParsePlecoCommand.java                                                     # UPDATED
    └── WordCommand.java                                                           # UPDATED
```

