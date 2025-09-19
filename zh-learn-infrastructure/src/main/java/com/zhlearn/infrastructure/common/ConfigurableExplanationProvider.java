package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ExplanationProvider;

public class ConfigurableExplanationProvider implements ExplanationProvider {

    private final GenericChatModelProvider<Explanation> provider;
    private final String name;
    private final String description;

    public ConfigurableExplanationProvider(ProviderConfig<Explanation> config, String name, String description) {
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
    public Explanation getExplanation(Hanzi word) {
        return provider.process(word);
    }
}