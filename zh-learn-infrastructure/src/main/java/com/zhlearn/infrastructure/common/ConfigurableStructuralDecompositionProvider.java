package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.model.WordType;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;

import java.util.Objects;
import java.util.function.Function;

public class ConfigurableStructuralDecompositionProvider implements StructuralDecompositionProvider {


    private final Function<Hanzi, StructuralDecomposition> singleCharProcessor;
    private final Function<Hanzi, StructuralDecomposition> multiCharProcessor;
    private final String name;
    private final String description;

    public ConfigurableStructuralDecompositionProvider(
            ProviderConfig<StructuralDecomposition> singleCharConfig,
            ProviderConfig<StructuralDecomposition> multiCharConfig,
            String name,
            String description) {
        this(
            new GenericChatModelProvider<>(singleCharConfig)::process,
            new GenericChatModelProvider<>(multiCharConfig)::process,
            name,
            description
        );
    }

    public ConfigurableStructuralDecompositionProvider(
            GenericChatModelProvider<StructuralDecomposition> singleCharProvider,
            GenericChatModelProvider<StructuralDecomposition> multiCharProvider,
            String name,
            String description) {
        this(singleCharProvider::process, multiCharProvider::process, name, description);
    }

    public ConfigurableStructuralDecompositionProvider(
            Function<Hanzi, StructuralDecomposition> singleCharProcessor,
            Function<Hanzi, StructuralDecomposition> multiCharProcessor,
            String name,
            String description) {
        this.singleCharProcessor = Objects.requireNonNull(singleCharProcessor, "singleCharProcessor");
        this.multiCharProcessor = Objects.requireNonNull(multiCharProcessor, "multiCharProcessor");
        this.name = name;
        this.description = description;
    }

    public static String templatePath() {
        return SingleCharStructuralDecompositionProviderConfig.templatePath();
    }

    public static String examplesDirectory() {
        return SingleCharStructuralDecompositionProviderConfig.examplesDirectory();
    }

    public static Function<String, StructuralDecomposition> responseMapper() {
        return SingleCharStructuralDecompositionProviderConfig.responseMapper();
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
        WordType wordType = WordType.from(word);
        return switch (wordType) {
            case SINGLE_CHARACTER -> singleCharProcessor.apply(word);
            case MULTI_CHARACTER -> multiCharProcessor.apply(word);
        };
    }
}
