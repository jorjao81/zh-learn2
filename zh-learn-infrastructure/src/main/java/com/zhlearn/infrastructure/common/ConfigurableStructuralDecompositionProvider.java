package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.model.WordType;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;

import java.util.Optional;
import java.util.function.Function;

public class ConfigurableStructuralDecompositionProvider implements StructuralDecompositionProvider {

    private final Function<Hanzi, StructuralDecomposition> singleCharProcessor;
    private final Function<Hanzi, StructuralDecomposition> multiCharProcessor;
    private final Optional<ProviderConfig<StructuralDecomposition>> singleCharConfig;
    private final Optional<ProviderConfig<StructuralDecomposition>> multiCharConfig;
    private final String name;
    private final String description;

    public ConfigurableStructuralDecompositionProvider(ProviderConfig<StructuralDecomposition> config,
                                                       String name,
                                                       String description) {
        this(new GenericChatModelProvider<>(config), new GenericChatModelProvider<>(config),
            Optional.of(config), Optional.of(config), name, description);
    }

    public ConfigurableStructuralDecompositionProvider(GenericChatModelProvider<StructuralDecomposition> provider,
                                                       String name,
                                                       String description) {
        this(provider::process, provider::process, Optional.empty(), Optional.empty(), name, description);
    }

    public ConfigurableStructuralDecompositionProvider(ProviderConfig<StructuralDecomposition> singleCharConfig,
                                                       ProviderConfig<StructuralDecomposition> multiCharConfig,
                                                       String name,
                                                       String description) {
        this(new GenericChatModelProvider<>(singleCharConfig)::process,
            new GenericChatModelProvider<>(multiCharConfig)::process,
            Optional.of(singleCharConfig), Optional.of(multiCharConfig), name, description);
    }

    public ConfigurableStructuralDecompositionProvider(GenericChatModelProvider<StructuralDecomposition> singleCharProvider,
                                                       GenericChatModelProvider<StructuralDecomposition> multiCharProvider,
                                                       String name,
                                                       String description) {
        this(singleCharProvider::process, multiCharProvider::process,
            Optional.empty(), Optional.empty(), name, description);
    }

    public ConfigurableStructuralDecompositionProvider(Function<Hanzi, StructuralDecomposition> processor,
                                                       String name,
                                                       String description) {
        this(processor, processor, Optional.empty(), Optional.empty(), name, description);
    }

    public ConfigurableStructuralDecompositionProvider(Function<Hanzi, StructuralDecomposition> singleCharProcessor,
                                                       Function<Hanzi, StructuralDecomposition> multiCharProcessor,
                                                       String name,
                                                       String description) {
        this(singleCharProcessor, multiCharProcessor, Optional.empty(), Optional.empty(), name, description);
    }

    public ConfigurableStructuralDecompositionProvider(Function<Hanzi, StructuralDecomposition> singleCharProcessor,
                                                       ProviderConfig<StructuralDecomposition> singleCharConfig,
                                                       Function<Hanzi, StructuralDecomposition> multiCharProcessor,
                                                       ProviderConfig<StructuralDecomposition> multiCharConfig,
                                                       String name,
                                                       String description) {
        this(singleCharProcessor, multiCharProcessor,
            Optional.of(singleCharConfig), Optional.of(multiCharConfig), name, description);
    }

    ConfigurableStructuralDecompositionProvider(Function<Hanzi, StructuralDecomposition> singleCharProcessor,
                                                Function<Hanzi, StructuralDecomposition> multiCharProcessor,
                                                Optional<ProviderConfig<StructuralDecomposition>> singleCharConfig,
                                                Optional<ProviderConfig<StructuralDecomposition>> multiCharConfig,
                                                String name,
                                                String description) {
        this.singleCharProcessor = singleCharProcessor;
        this.multiCharProcessor = multiCharProcessor;
        this.singleCharConfig = singleCharConfig;
        this.multiCharConfig = multiCharConfig;
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
        WordType type = WordType.from(word);
        return selectProcessor(type).apply(word);
    }

    Optional<ProviderConfig<StructuralDecomposition>> singleCharConfig() {
        return singleCharConfig;
    }

    Optional<ProviderConfig<StructuralDecomposition>> multiCharConfig() {
        return multiCharConfig;
    }

    private Function<Hanzi, StructuralDecomposition> selectProcessor(WordType type) {
        return switch (type) {
            case SINGLE_CHARACTER -> singleCharProcessor;
            case MULTI_CHARACTER -> multiCharProcessor;
        };
    }
}
