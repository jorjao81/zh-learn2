package com.zhlearn.infrastructure.common;

import java.util.Optional;
import java.util.function.Function;

import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.model.WordType;
import com.zhlearn.domain.provider.ExplanationProvider;

public class ConfigurableExplanationProvider implements ExplanationProvider {

    private final Function<Hanzi, Explanation> singleCharProcessor;
    private final Function<Hanzi, Explanation> multiCharProcessor;
    private final Optional<ProviderConfig<Explanation>> singleCharConfig;
    private final Optional<ProviderConfig<Explanation>> multiCharConfig;
    private final String name;
    private final String description;

    public ConfigurableExplanationProvider(
            ProviderConfig<Explanation> singleCharConfig,
            ProviderConfig<Explanation> multiCharConfig,
            String name,
            String description) {
        this(
                new GenericChatModelProvider<>(singleCharConfig)::process,
                new GenericChatModelProvider<>(multiCharConfig)::process,
                Optional.of(singleCharConfig),
                Optional.of(multiCharConfig),
                name,
                description);
    }

    public ConfigurableExplanationProvider(
            Function<Hanzi, Explanation> singleCharProcessor,
            Function<Hanzi, Explanation> multiCharProcessor,
            String name,
            String description) {
        this(
                singleCharProcessor,
                multiCharProcessor,
                Optional.empty(),
                Optional.empty(),
                name,
                description);
    }

    public ConfigurableExplanationProvider(
            Function<Hanzi, Explanation> singleCharProcessor,
            ProviderConfig<Explanation> singleCharConfig,
            Function<Hanzi, Explanation> multiCharProcessor,
            ProviderConfig<Explanation> multiCharConfig,
            String name,
            String description) {
        this(
                singleCharProcessor,
                multiCharProcessor,
                Optional.of(singleCharConfig),
                Optional.of(multiCharConfig),
                name,
                description);
    }

    ConfigurableExplanationProvider(
            Function<Hanzi, Explanation> singleCharProcessor,
            Function<Hanzi, Explanation> multiCharProcessor,
            Optional<ProviderConfig<Explanation>> singleCharConfig,
            Optional<ProviderConfig<Explanation>> multiCharConfig,
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
    public Explanation getExplanation(Hanzi word) {
        WordType type = WordType.from(word);
        return selectProcessor(type).apply(word);
    }

    Optional<ProviderConfig<Explanation>> singleCharConfig() {
        return singleCharConfig;
    }

    Optional<ProviderConfig<Explanation>> multiCharConfig() {
        return multiCharConfig;
    }

    private Function<Hanzi, Explanation> selectProcessor(WordType type) {
        return switch (type) {
            case SINGLE_CHARACTER -> singleCharProcessor;
            case MULTI_CHARACTER -> multiCharProcessor;
        };
    }
}
