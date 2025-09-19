package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ExplanationProvider;

import java.util.function.Function;

public class ConfigurableExplanationProvider implements ExplanationProvider {


    private final Function<Hanzi, Explanation> processor;
    private final String name;
    private final String description;

    public ConfigurableExplanationProvider(ProviderConfig<Explanation> config, String name, String description) {
        this(new GenericChatModelProvider<>(config), name, description);
    }

    public ConfigurableExplanationProvider(GenericChatModelProvider<Explanation> provider, String name, String description) {
        this(provider::process, name, description);
    }

    public ConfigurableExplanationProvider(Function<Hanzi, Explanation> processor, String name, String description) {
        this.processor = processor;
        this.name = name;
        this.description = description;
    }

    public static String templatePath() {
        return ExplanationProviderConfig.templatePath();
    }

    public static String examplesDirectory() {
        return ExplanationProviderConfig.examplesDirectory();
    }

    public static Function<String, Explanation> responseMapper() {
        return ExplanationProviderConfig.responseMapper();
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
    public Explanation getExplanation(Hanzi word) {
        return processor.apply(word);
    }
}
