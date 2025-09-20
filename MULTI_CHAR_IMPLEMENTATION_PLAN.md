# Detailed Implementation Plan: Multi-Character Word Support

## Architecture Analysis & Integration Points

**Current State:**
- `Hanzi` record accepts any string without character count distinction
- `WordAnalysisServiceImpl` orchestrates all providers but doesn't differentiate between single/multi-character
- `AIProviderFactory` creates providers with hardcoded single-character resource paths
- CLI commands (`WordCommand`, `ParsePlecoCommand`) directly use provider factories
- Provider interfaces are character-count agnostic

## 1. Character Detection Strategy

### 1.1 Domain Model Enhancement
```java
// Add to Hanzi record
public boolean isSingleCharacter() {
    return characters.codePointCount(0, characters.length()) == 1;
}

public boolean isMultiCharacter() {
    return !isSingleCharacter();
}
```

### 1.2 Word Type Enum
```java
public enum WordType {
    SINGLE_CHARACTER,
    MULTI_CHARACTER;

    public static WordType from(Hanzi word) {
        return word.isSingleCharacter() ? SINGLE_CHARACTER : MULTI_CHARACTER;
    }
}
```

## 2. Provider Configuration System

### 2.1 Enhanced Provider Configs
Keep existing single-character configs and add multi-character configs:
- `ExampleProviderConfig` (existing single-character, rename to `SingleCharExampleProviderConfig` )
- `MultiCharExampleProviderConfig` (new)
- `ExplanationProviderConfig` (existing single-character, rename to `SingleCharExplanationProviderConfig`)
- `MultiCharExplanationProviderConfig` (new)
- `StructuralDecompositionProviderConfig` (existing single-character, rename to `SingleCharStructuralDecompositionProviderConfig`)
- `MultiCharStructuralDecompositionProviderConfig` (new)

### 2.2 Provider Internal Routing
```java
// In ConfigurableExampleProvider
public class ConfigurableExampleProvider implements ExampleProvider {
    private final ProviderConfig<Example> singleCharConfig;
    private final ProviderConfig<Example> multiCharConfig;

    @Override
    public Example getExamples(Hanzi word, Optional<String> definition) {
        WordType type = WordType.from(word);
        ProviderConfig<Example> config = switch(type) {
            case SINGLE_CHARACTER -> singleCharConfig;
            case MULTI_CHARACTER -> multiCharConfig;
        };
        return processWithConfig(word, definition, config);
    }
}
```

## 3. Application Service Changes

### 3.1 No Changes to WordAnalysisServiceImpl
The existing `WordAnalysisServiceImpl` requires no changes! Since each provider now handles routing internally, the service layer remains completely unchanged.

### 3.2 Provider Factory Enhancement
```java
// In AIProviderFactory
public static ExampleProvider createExampleProvider(String providerName) {
    // No word type parameter needed - providers handle routing internally
    return switch (providerName) {
        case "dummy" -> new DummyExampleProvider();
        case "deepseek-chat" -> {
            requireAPIKey("DEEPSEEK_API_KEY", providerName);

            // Create both single and multi-char configs
            ProviderConfig<Example> singleConfig = createProviderConfig(
                DeepSeekConfig.getApiKey(),
                DeepSeekConfig.getBaseUrl(),
                "deepseek-chat",
                SingleCharExampleProviderConfig.templatePath(), 
                    SingleCharExampleProviderConfig.examplesDirectory(),
                    SingleCharExampleProviderConfig.responseMapper(),
                providerName,
                "Failed to get examples from DeepSeek (deepseek-chat)"
            );

            ProviderConfig<Example> multiConfig = createProviderConfig(
                DeepSeekConfig.getApiKey(),
                DeepSeekConfig.getBaseUrl(),
                "deepseek-chat",
                MultiCharExampleProviderConfig.templatePath(),
                MultiCharExampleProviderConfig.examplesDirectory(),
                MultiCharExampleProviderConfig.responseMapper(),
                providerName,
                "Failed to get examples from DeepSeek (deepseek-chat)"
            );

            yield new ConfigurableExampleProvider(singleConfig, multiConfig, providerName, "DeepSeek AI-powered example provider");
        }
        // ... other providers
    };
}
```

## 4. CLI Integration Approach

### 4.1 Zero Changes Required
- **No CLI changes at all** - providers handle routing internally
- `WordCommand` and `ParsePlecoCommand` remain completely unchanged
- `MainCommand` remains completely unchanged
- Detection happens in individual providers, completely transparent to all other layers

### 4.2 Perfect Encapsulation
Each provider is self-contained and handles both single and multi-character words internally. No other part of the system needs to know about word types.

## 5. Test Strategy (Constitutional Compliance)

### 5.1 Cucumber Acceptance Tests
```gherkin
Feature: Multi-character word analysis
  Scenario: Analyze multi-character word
    Given I have a multi-character word "学校"
    When I analyze the word using "dummy" providers
    Then the analysis should be successful
    And the response should contain sentence examples
    And the structural decomposition should show compound components
```

### 5.2 Unit Tests
- `WordTypeTest` - test detection logic in Hanzi
- `ConfigurableExampleProviderTest` - test internal routing logic
- `MultiCharProviderConfigTest` - test multi-character configuration
- `AIProviderFactoryTest` - test provider creation with both configs
- All tests must pass before any commits

## 6. Implementation Phases

### Phase 1: Domain Enhancement
1. Add character detection methods to `Hanzi`
2. Create `WordType` enum
3. Write comprehensive unit tests

### Phase 2: Provider Configuration
1. Create multi-character provider configs
2. Enhance `ConfigurableExampleProvider` to accept both single and multi-char configs
3. Update `AIProviderFactory` to create providers with both configs
4. Test provider internal routing

### Phase 3: Provider Enhancement
1. Update all configurable providers (Example, Explanation, StructuralDecomposition)
2. Each provider gets internal routing logic
3. Write unit tests for each provider's routing

### Phase 4: Integration
1. No changes needed to service layer or CLI!
2. Write end-to-end Cucumber tests

### Phase 5: Validation
1. Test with mixed Pleco exports (single + multi character)
2. Verify seamless operation
3. Performance testing

## 7. Constitutional Compliance Checkpoints

✅ **Modular Architecture**: Changes respect module boundaries
✅ **Fail-Fast Philosophy**: No fallbacks, clear failure modes
✅ **Test-First Development**: All features start with tests
✅ **CLI-First Interface**: No breaking changes to CLI
✅ **Provider Pattern**: Maintains consistent provider interfaces

## 8. Seamless Operation Guarantee

**Pleco Export Mixed Content:**
- Single characters → single-character providers automatically
- Multi-character words → multi-character providers automatically
- No user intervention required
- Same CLI commands work for both
- Performance maintained through efficient detection

This implementation ensures backwards compatibility while providing robust multi-character support through automatic detection and transparent routing.