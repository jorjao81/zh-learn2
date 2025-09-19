package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.StructuralDecomposition;

import java.util.Optional;

/**
 * Multi-purpose Qwen provider that can serve as Example, Explanation, or StructuralDecomposition provider.
 * Uses GenericChatModelProvider internally with DashScopeConfig for Qwen models.
 */
public class ConfigurableQwenProvider implements ExampleProvider, ExplanationProvider, StructuralDecompositionProvider {

    private final GenericChatModelProvider<Example> exampleProvider;
    private final GenericChatModelProvider<Explanation> explanationProvider;
    private final GenericChatModelProvider<StructuralDecomposition> decompositionProvider;
    private final String name;
    private final String description;

    public ConfigurableQwenProvider(String modelName, String name, String description) {
        // Create providers for each type using DashScopeConfig
        this.exampleProvider = new GenericChatModelProvider<>(DashScopeConfig.forExamples(name, modelName));
        this.explanationProvider = new GenericChatModelProvider<>(DashScopeConfig.forExplanation(name, modelName));
        this.decompositionProvider = new GenericChatModelProvider<>(DashScopeConfig.forStructuralDecomposition(name, modelName));

        this.name = name;
        this.description = description;
    }

    // ExampleProvider implementation
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ProviderType getType() {
        return ProviderType.AI;
    }

    @Override
    public Example getExamples(Hanzi word, Optional<String> definition) {
        return exampleProvider.process(word, definition);
    }

    // ExplanationProvider implementation
    @Override
    public Explanation getExplanation(Hanzi word) {
        return explanationProvider.process(word, Optional.empty());
    }

    // StructuralDecompositionProvider implementation
    @Override
    public StructuralDecomposition getStructuralDecomposition(Hanzi word) {
        return decompositionProvider.process(word, Optional.empty());
    }
}