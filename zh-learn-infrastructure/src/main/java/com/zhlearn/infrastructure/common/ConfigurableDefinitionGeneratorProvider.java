package com.zhlearn.infrastructure.common;

import java.util.function.Function;

import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.model.WordType;
import com.zhlearn.domain.provider.DefinitionGeneratorProvider;

public class ConfigurableDefinitionGeneratorProvider implements DefinitionGeneratorProvider {

    private final Function<Hanzi, Definition> singleCharProcessor;
    private final Function<Hanzi, Definition> multiCharProcessor;
    private final String name;
    private final String description;

    public ConfigurableDefinitionGeneratorProvider(
            ProviderConfig<Definition> singleCharConfig,
            ProviderConfig<Definition> multiCharConfig,
            String name,
            String description) {
        this(
                new GenericChatModelProvider<>(singleCharConfig)::process,
                new GenericChatModelProvider<>(multiCharConfig)::process,
                name,
                description);
    }

    public ConfigurableDefinitionGeneratorProvider(
            Function<Hanzi, Definition> singleCharProcessor,
            Function<Hanzi, Definition> multiCharProcessor,
            String name,
            String description) {
        this.singleCharProcessor = singleCharProcessor;
        this.multiCharProcessor = multiCharProcessor;
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
    public Definition generateDefinition(Hanzi word) {
        WordType type = WordType.from(word);
        return selectProcessor(type).apply(word);
    }

    private Function<Hanzi, Definition> selectProcessor(WordType type) {
        return switch (type) {
            case SINGLE_CHARACTER -> singleCharProcessor;
            case MULTI_CHARACTER -> multiCharProcessor;
        };
    }
}
