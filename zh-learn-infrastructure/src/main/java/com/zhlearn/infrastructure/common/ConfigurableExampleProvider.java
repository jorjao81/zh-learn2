package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.model.WordType;
import com.zhlearn.domain.provider.ExampleProvider;

import java.util.Optional;
import java.util.function.BiFunction;

public class ConfigurableExampleProvider implements ExampleProvider {

    private final BiFunction<Hanzi, Optional<String>, Example> singleCharProcessor;
    private final BiFunction<Hanzi, Optional<String>, Example> multiCharProcessor;
    private final Optional<ProviderConfig<Example>> singleCharConfig;
    private final Optional<ProviderConfig<Example>> multiCharConfig;
    private final String name;
    private final String description;

    public ConfigurableExampleProvider(ProviderConfig<Example> config, String name, String description) {
        this(new GenericChatModelProvider<>(config), new GenericChatModelProvider<>(config),
            Optional.of(config), Optional.of(config), name, description);
    }

    public ConfigurableExampleProvider(GenericChatModelProvider<Example> provider, String name, String description) {
        this(provider::process, provider::process, Optional.empty(), Optional.empty(), name, description);
    }

    public ConfigurableExampleProvider(ProviderConfig<Example> singleCharConfig,
                                       ProviderConfig<Example> multiCharConfig,
                                       String name,
                                       String description) {
        this(new GenericChatModelProvider<>(singleCharConfig)::process,
            new GenericChatModelProvider<>(multiCharConfig)::process,
            Optional.of(singleCharConfig), Optional.of(multiCharConfig), name, description);
    }

    public ConfigurableExampleProvider(GenericChatModelProvider<Example> singleCharProvider,
                                       GenericChatModelProvider<Example> multiCharProvider,
                                       String name,
                                       String description) {
        this(singleCharProvider::process, multiCharProvider::process,
            Optional.empty(), Optional.empty(), name, description);
    }

    public ConfigurableExampleProvider(BiFunction<Hanzi, Optional<String>, Example> processor, String name, String description) {
        this(processor, processor, Optional.empty(), Optional.empty(), name, description);
    }

    public ConfigurableExampleProvider(BiFunction<Hanzi, Optional<String>, Example> singleCharProcessor,
                                       BiFunction<Hanzi, Optional<String>, Example> multiCharProcessor,
                                       String name,
                                       String description) {
        this(singleCharProcessor, multiCharProcessor, Optional.empty(), Optional.empty(), name, description);
    }

    public ConfigurableExampleProvider(BiFunction<Hanzi, Optional<String>, Example> singleCharProcessor,
                                       ProviderConfig<Example> singleCharConfig,
                                       BiFunction<Hanzi, Optional<String>, Example> multiCharProcessor,
                                       ProviderConfig<Example> multiCharConfig,
                                       String name,
                                       String description) {
        this(singleCharProcessor, multiCharProcessor,
            Optional.of(singleCharConfig), Optional.of(multiCharConfig), name, description);
    }

    ConfigurableExampleProvider(BiFunction<Hanzi, Optional<String>, Example> singleCharProcessor,
                                BiFunction<Hanzi, Optional<String>, Example> multiCharProcessor,
                                Optional<ProviderConfig<Example>> singleCharConfig,
                                Optional<ProviderConfig<Example>> multiCharConfig,
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
    public Example getExamples(Hanzi word, Optional<String> definition) {
        WordType type = WordType.from(word);
        return selectProcessor(type).apply(word, definition);
    }

    Optional<ProviderConfig<Example>> singleCharConfig() {
        return singleCharConfig;
    }

    Optional<ProviderConfig<Example>> multiCharConfig() {
        return multiCharConfig;
    }

    private BiFunction<Hanzi, Optional<String>, Example> selectProcessor(WordType type) {
        return switch (type) {
            case SINGLE_CHARACTER -> singleCharProcessor;
            case MULTI_CHARACTER -> multiCharProcessor;
        };
    }
}
