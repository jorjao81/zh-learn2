package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;

import java.util.function.Function;

public class ConfigurableStructuralDecompositionProvider implements StructuralDecompositionProvider {


    private final Function<Hanzi, StructuralDecomposition> processor;
    private final String name;
    private final String description;

    public ConfigurableStructuralDecompositionProvider(ProviderConfig<StructuralDecomposition> config, String name, String description) {
        this(new GenericChatModelProvider<>(config), name, description);
    }

    public ConfigurableStructuralDecompositionProvider(GenericChatModelProvider<StructuralDecomposition> provider, String name, String description) {
        this(provider::process, name, description);
    }

    public ConfigurableStructuralDecompositionProvider(Function<Hanzi, StructuralDecomposition> processor, String name, String description) {
        this.processor = processor;
        this.name = name;
        this.description = description;
    }

    public static String templatePath() {
        return StructuralDecompositionProviderConfig.templatePath();
    }

    public static String examplesDirectory() {
        return StructuralDecompositionProviderConfig.examplesDirectory();
    }

    public static Function<String, StructuralDecomposition> responseMapper() {
        return StructuralDecompositionProviderConfig.responseMapper();
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
        return processor.apply(word);
    }
}
