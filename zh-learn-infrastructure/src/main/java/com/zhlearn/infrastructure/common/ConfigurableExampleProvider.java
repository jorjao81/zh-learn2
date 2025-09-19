package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ExampleProvider;

import java.util.Optional;

public class ConfigurableExampleProvider implements ExampleProvider {

    private final GenericChatModelProvider<Example> provider;
    private final String name;
    private final String description;

    public ConfigurableExampleProvider(ProviderConfig<Example> config, String name, String description) {
        this.provider = new GenericChatModelProvider<>(config);
        this.name = name;
        this.description = description;
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
        return provider.process(word, definition);
    }
}