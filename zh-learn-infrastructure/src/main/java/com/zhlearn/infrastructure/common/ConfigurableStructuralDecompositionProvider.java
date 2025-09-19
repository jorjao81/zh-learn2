package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;

public class ConfigurableStructuralDecompositionProvider implements StructuralDecompositionProvider {

    private final GenericChatModelProvider<StructuralDecomposition> provider;
    private final String name;
    private final String description;

    public ConfigurableStructuralDecompositionProvider(ProviderConfig<StructuralDecomposition> config, String name, String description) {
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
    public StructuralDecomposition getStructuralDecomposition(Hanzi word) {
        return provider.process(word);
    }
}