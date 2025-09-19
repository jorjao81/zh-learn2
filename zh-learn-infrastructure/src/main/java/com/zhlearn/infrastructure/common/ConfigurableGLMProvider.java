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
 * Multi-purpose GLM provider that can serve as Example, Explanation, or StructuralDecomposition provider.
 * Uses ZhipuChatModelProvider internally for GLM models.
 */
public class ConfigurableGLMProvider implements ExampleProvider, ExplanationProvider, StructuralDecompositionProvider {

    private final ZhipuChatModelProvider<Example> exampleProvider;
    private final ZhipuChatModelProvider<Explanation> explanationProvider;
    private final ZhipuChatModelProvider<StructuralDecomposition> decompositionProvider;
    private final String name;
    private final String description;

    public ConfigurableGLMProvider(String modelName, String name, String description) {
        this.name = name;
        this.description = description;

        // Create configs for each type using the same model but different templates
        var exampleConfig = createGLMConfig(modelName, Example.class);
        var explanationConfig = createGLMConfig(modelName, Explanation.class);
        var decompositionConfig = createGLMConfig(modelName, StructuralDecomposition.class);

        this.exampleProvider = new ZhipuChatModelProvider<>(exampleConfig);
        this.explanationProvider = new ZhipuChatModelProvider<>(explanationConfig);
        this.decompositionProvider = new ZhipuChatModelProvider<>(decompositionConfig);
    }

    private <T> ProviderConfig<T> createGLMConfig(String modelName, Class<T> type) {
        return new ProviderConfig<>(
            SimpleProviderConfig.readEnv("ZHIPU_API_KEY"),
            "https://open.bigmodel.cn/api/paas/v4",
            modelName,
            0.3,
            8000,
            getTemplateResourcePath(type),
            getExamplesResourcePath(type),
            getResponseMapper(type),
            name,
            "Failed to get response from " + name + " API"
        );
    }

    private <T> String getTemplateResourcePath(Class<T> type) {
        if (type == Example.class) {
            return "/examples/prompt-template.md";
        } else if (type == Explanation.class) {
            return "/explanation/prompt-template.md";
        } else if (type == StructuralDecomposition.class) {
            return "/structural-decomposition/prompt-template.md";
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    private <T> String getExamplesResourcePath(Class<T> type) {
        if (type == Example.class) {
            return "/examples/examples/";
        } else if (type == Explanation.class) {
            return "/explanation/examples/";
        } else if (type == StructuralDecomposition.class) {
            return "/structural-decomposition/examples/";
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    @SuppressWarnings("unchecked")
    private <T> java.util.function.Function<String, T> getResponseMapper(Class<T> type) {
        if (type == Example.class) {
            return (java.util.function.Function<String, T>) new ExampleResponseMapper();
        } else if (type == Explanation.class) {
            return (java.util.function.Function<String, T>) (java.util.function.Function<String, Explanation>) Explanation::new;
        } else if (type == StructuralDecomposition.class) {
            return (java.util.function.Function<String, T>) (java.util.function.Function<String, StructuralDecomposition>) StructuralDecomposition::new;
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

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

    @Override
    public Explanation getExplanation(Hanzi word) {
        return explanationProvider.process(word);
    }

    @Override
    public StructuralDecomposition getStructuralDecomposition(Hanzi word) {
        return decompositionProvider.process(word);
    }
}
