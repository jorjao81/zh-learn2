package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.model.WordType;
import com.zhlearn.domain.provider.ExampleProvider;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ConfigurableExampleProvider implements ExampleProvider {


    private final BiFunction<Hanzi, Optional<String>, Example> singleCharProcessor;
    private final BiFunction<Hanzi, Optional<String>, Example> multiCharProcessor;
    private final String name;
    private final String description;

    public ConfigurableExampleProvider(
            ProviderConfig<Example> singleCharConfig,
            ProviderConfig<Example> multiCharConfig,
            String name,
            String description) {
        this(
            new GenericChatModelProvider<>(singleCharConfig)::process,
            new GenericChatModelProvider<>(multiCharConfig)::process,
            name,
            description
        );
    }

    public ConfigurableExampleProvider(
            GenericChatModelProvider<Example> singleCharProvider,
            GenericChatModelProvider<Example> multiCharProvider,
            String name,
            String description) {
        this(singleCharProvider::process, multiCharProvider::process, name, description);
    }

    public ConfigurableExampleProvider(
            BiFunction<Hanzi, Optional<String>, Example> singleCharProcessor,
            BiFunction<Hanzi, Optional<String>, Example> multiCharProcessor,
            String name,
            String description) {
        this.singleCharProcessor = Objects.requireNonNull(singleCharProcessor, "singleCharProcessor");
        this.multiCharProcessor = Objects.requireNonNull(multiCharProcessor, "multiCharProcessor");
        this.name = name;
        this.description = description;
    }

    public static String templatePath() {
        return SingleCharExampleProviderConfig.templatePath();
    }

    public static String examplesDirectory() {
        return SingleCharExampleProviderConfig.examplesDirectory();
    }

    public static Function<String, Example> responseMapper() {
        return SingleCharExampleProviderConfig.responseMapper();
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
        WordType wordType = WordType.from(word);
        return switch (wordType) {
            case SINGLE_CHARACTER -> singleCharProcessor.apply(word, definition);
            case MULTI_CHARACTER -> multiCharProcessor.apply(word, definition);
        };
    }
}
