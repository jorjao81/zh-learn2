package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Hanzi;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurableExampleProviderTest {

    @Test
    void shouldUseSingleCharacterProcessor() {
        AtomicBoolean singleUsed = new AtomicBoolean(false);
        AtomicBoolean multiUsed = new AtomicBoolean(false);
        Example singleExample = new Example(
            List.of(new Example.Usage("好", "hǎo", "good", "context", "breakdown")),
            List.of()
        );
        Example multiExample = new Example(
            List.of(new Example.Usage("学校", "xuéxiào", "school", "context", "breakdown")),
            List.of()
        );

        ConfigurableExampleProvider provider = new ConfigurableExampleProvider(
            (hanzi, definition) -> {
                singleUsed.set(true);
                return singleExample;
            },
            (hanzi, definition) -> {
                multiUsed.set(true);
                return multiExample;
            },
            "test",
            "description"
        );

        Example result = provider.getExamples(new Hanzi("好"), Optional.empty());

        assertThat(result).isEqualTo(singleExample);
        assertThat(singleUsed).isTrue();
        assertThat(multiUsed).isFalse();
    }

    @Test
    void shouldUseMultiCharacterProcessor() {
        AtomicBoolean singleUsed = new AtomicBoolean(false);
        AtomicBoolean multiUsed = new AtomicBoolean(false);
        Example singleExample = new Example(
            List.of(new Example.Usage("好", "hǎo", "good", "context", "breakdown")),
            List.of()
        );
        Example multiExample = new Example(
            List.of(new Example.Usage("学校", "xuéxiào", "school", "context", "breakdown")),
            List.of()
        );

        ConfigurableExampleProvider provider = new ConfigurableExampleProvider(
            (hanzi, definition) -> {
                singleUsed.set(true);
                return singleExample;
            },
            (hanzi, definition) -> {
                multiUsed.set(true);
                return multiExample;
            },
            "test",
            "description"
        );

        Example result = provider.getExamples(new Hanzi("学校"), Optional.of("school"));

        assertThat(result).isEqualTo(multiExample);
        assertThat(singleUsed).isFalse();
        assertThat(multiUsed).isTrue();
    }
}

