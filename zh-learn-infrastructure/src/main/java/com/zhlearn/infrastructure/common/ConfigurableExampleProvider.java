package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ExampleProvider;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ConfigurableExampleProvider implements ExampleProvider {


    private final BiFunction<Hanzi, Optional<String>, Example> processor;
    private final String name;
    private final String description;

    public ConfigurableExampleProvider(ProviderConfig<Example> config, String name, String description) {
        this(new GenericChatModelProvider<>(config), name, description);
    }

    public ConfigurableExampleProvider(GenericChatModelProvider<Example> provider, String name, String description) {
        this(provider::process, name, description);
    }

    public ConfigurableExampleProvider(BiFunction<Hanzi, Optional<String>, Example> processor, String name, String description) {
        this.processor = processor;
        this.name = name;
        this.description = description;
    }

    public static String templatePath() {
        return ExampleProviderConfig.templatePath();
    }

    public static String examplesDirectory() {
        return ExampleProviderConfig.examplesDirectory();
    }

    public static Function<String, Example> responseMapper() {
        return ExampleProviderConfig.responseMapper();
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
        return processor.apply(word, definition);
    }
}
