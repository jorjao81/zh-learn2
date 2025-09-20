package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.model.WordType;
import com.zhlearn.domain.provider.ExplanationProvider;

import java.util.Objects;
import java.util.function.Function;

public class ConfigurableExplanationProvider implements ExplanationProvider {

    private final Function<Hanzi, Explanation> singleCharProcessor;
    private final Function<Hanzi, Explanation> multiCharProcessor;
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
            name,
            description
        );
    }

    public ConfigurableExplanationProvider(
            GenericChatModelProvider<Explanation> singleCharProvider,
            GenericChatModelProvider<Explanation> multiCharProvider,
            String name,
            String description) {
        this(singleCharProvider::process, multiCharProvider::process, name, description);
    }

    public ConfigurableExplanationProvider(
            Function<Hanzi, Explanation> singleCharProcessor,
            Function<Hanzi, Explanation> multiCharProcessor,
            String name,
            String description) {
        this.singleCharProcessor = Objects.requireNonNull(singleCharProcessor, "singleCharProcessor");
        this.multiCharProcessor = Objects.requireNonNull(multiCharProcessor, "multiCharProcessor");
        this.name = name;
        this.description = description;
    }

    public static String templatePath() {
        return SingleCharExplanationProviderConfig.templatePath();
    }

    public static String examplesDirectory() {
        return SingleCharExplanationProviderConfig.examplesDirectory();
    }

    public static Function<String, Explanation> responseMapper() {
        return SingleCharExplanationProviderConfig.responseMapper();
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
        WordType wordType = WordType.from(word);
        return switch (wordType) {
            case SINGLE_CHARACTER -> singleCharProcessor.apply(word);
            case MULTI_CHARACTER -> multiCharProcessor.apply(word);
        };
    }
}
