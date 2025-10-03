package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.model.WordType;
import com.zhlearn.domain.provider.DefinitionFormatterProvider;

import java.util.Optional;
import java.util.function.BiFunction;

public class ConfigurableDefinitionFormatterProvider implements DefinitionFormatterProvider {

    private final BiFunction<Hanzi, Optional<String>, Definition> singleCharProcessor;
    private final BiFunction<Hanzi, Optional<String>, Definition> multiCharProcessor;
    private final Optional<ProviderConfig<Definition>> singleCharConfig;
    private final Optional<ProviderConfig<Definition>> multiCharConfig;
    private final String name;
    private final String description;

    public ConfigurableDefinitionFormatterProvider(ProviderConfig<Definition> config, String name, String description) {
        this(new GenericChatModelProvider<>(config)::process, new GenericChatModelProvider<>(config)::process,
            Optional.of(config), Optional.of(config), name, description);
    }

    public ConfigurableDefinitionFormatterProvider(GenericChatModelProvider<Definition> provider, String name, String description) {
        this(provider::process, provider::process, Optional.empty(), Optional.empty(), name, description);
    }

    public ConfigurableDefinitionFormatterProvider(ProviderConfig<Definition> singleCharConfig,
                                       ProviderConfig<Definition> multiCharConfig,
                                       String name,
                                       String description) {
        this(new GenericChatModelProvider<>(singleCharConfig)::process,
            new GenericChatModelProvider<>(multiCharConfig)::process,
            Optional.of(singleCharConfig), Optional.of(multiCharConfig), name, description);
    }

    public ConfigurableDefinitionFormatterProvider(GenericChatModelProvider<Definition> singleCharProvider,
                                       GenericChatModelProvider<Definition> multiCharProvider,
                                       String name,
                                       String description) {
        this(singleCharProvider::process, multiCharProvider::process,
            Optional.empty(), Optional.empty(), name, description);
    }

    public ConfigurableDefinitionFormatterProvider(BiFunction<Hanzi, Optional<String>, Definition> processor, String name, String description) {
        this(processor, processor, Optional.empty(), Optional.empty(), name, description);
    }

    public ConfigurableDefinitionFormatterProvider(BiFunction<Hanzi, Optional<String>, Definition> singleCharProcessor,
                                       BiFunction<Hanzi, Optional<String>, Definition> multiCharProcessor,
                                       String name,
                                       String description) {
        this(singleCharProcessor, multiCharProcessor, Optional.empty(), Optional.empty(), name, description);
    }

    public ConfigurableDefinitionFormatterProvider(BiFunction<Hanzi, Optional<String>, Definition> singleCharProcessor,
                                       ProviderConfig<Definition> singleCharConfig,
                                       BiFunction<Hanzi, Optional<String>, Definition> multiCharProcessor,
                                       ProviderConfig<Definition> multiCharConfig,
                                       String name,
                                       String description) {
        this(singleCharProcessor, multiCharProcessor,
            Optional.of(singleCharConfig), Optional.of(multiCharConfig), name, description);
    }

    ConfigurableDefinitionFormatterProvider(BiFunction<Hanzi, Optional<String>, Definition> singleCharProcessor,
                                BiFunction<Hanzi, Optional<String>, Definition> multiCharProcessor,
                                Optional<ProviderConfig<Definition>> singleCharConfig,
                                Optional<ProviderConfig<Definition>> multiCharConfig,
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
    public Definition formatDefinition(Hanzi word, Optional<String> rawDefinition) {
        WordType type = WordType.from(word);

        // Skip formatting for single-character words with etymology markers
        if (type == WordType.SINGLE_CHARACTER && rawDefinition.isPresent()) {
            String raw = rawDefinition.get();
            if (containsEtymologyMarkers(raw)) {
                return new Definition(raw);
            }
        }

        return selectProcessor(type).apply(word, rawDefinition);
    }

    Optional<ProviderConfig<Definition>> singleCharConfig() {
        return singleCharConfig;
    }

    Optional<ProviderConfig<Definition>> multiCharConfig() {
        return multiCharConfig;
    }

    private BiFunction<Hanzi, Optional<String>, Definition> selectProcessor(WordType type) {
        return switch (type) {
            case SINGLE_CHARACTER -> singleCharProcessor;
            case MULTI_CHARACTER -> multiCharProcessor;
        };
    }

    private boolean containsEtymologyMarkers(String text) {
        return text.contains("=>") ||
               text.contains("⇒") ||
               text.contains("→") ||
               text.contains("(orig.)");
    }
}