package com.zhlearn.infrastructure.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Hanzi;

class ConfigurableExampleProviderTest {

    @Test
    void shouldUseSingleCharProcessorForSingleCharacterWords() {
        Example single = createExample("single");
        Example multi = createExample("multi");
        AtomicReference<String> invocations = new AtomicReference<>();

        ConfigurableExampleProvider provider =
                new ConfigurableExampleProvider(
                        (hanzi, definition) -> {
                            invocations.set("single");
                            return single;
                        },
                        (hanzi, definition) -> {
                            invocations.set("multi");
                            return multi;
                        },
                        "test",
                        "Test provider");

        Example result = provider.getExamples(new Hanzi("学"), Optional.empty());

        assertThat(result).isSameAs(single);
        assertThat(invocations.get()).isEqualTo("single");
    }

    @Test
    void shouldUseMultiCharProcessorForMultiCharacterWords() {
        Example single = createExample("single");
        Example multi = createExample("multi");
        AtomicReference<String> invocations = new AtomicReference<>();

        ConfigurableExampleProvider provider =
                new ConfigurableExampleProvider(
                        (hanzi, definition) -> {
                            invocations.set("single");
                            return single;
                        },
                        (hanzi, definition) -> {
                            invocations.set("multi");
                            return multi;
                        },
                        "test",
                        "Test provider");

        Example result = provider.getExamples(new Hanzi("学校"), Optional.empty());

        assertThat(result).isSameAs(multi);
        assertThat(invocations.get()).isEqualTo("multi");
    }

    @Test
    void shouldExposeConfigsWhenConstructedWithProviderConfigs() {
        ProviderConfig<Example> singleConfig =
                new ProviderConfig<>(
                        "api",
                        "base",
                        "model",
                        0.1,
                        100,
                        "/single-char/examples/prompt-template.md",
                        "/single-char/examples/examples/",
                        response -> createExample(response),
                        "name",
                        "error");
        ProviderConfig<Example> multiConfig =
                new ProviderConfig<>(
                        "api2",
                        "base2",
                        "model2",
                        0.1,
                        100,
                        "/multi-char/examples/prompt-template.md",
                        "/multi-char/examples/examples/",
                        response -> createExample(response),
                        "name2",
                        "error2");

        ConfigurableExampleProvider provider =
                new ConfigurableExampleProvider(singleConfig, multiConfig, "test", "Test provider");

        assertThat(provider.singleCharConfig()).containsSame(singleConfig);
        assertThat(provider.multiCharConfig()).containsSame(multiConfig);
    }

    private Example createExample(String marker) {
        Example.Usage usage =
                new Example.Usage(
                        marker + "句子", marker + "pinyin", "translation", "context", "breakdown");
        return new Example(List.of(usage), List.of());
    }
}
