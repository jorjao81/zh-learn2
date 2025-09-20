package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.Hanzi;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurableExplanationProviderTest {

    @Test
    void shouldUseSingleCharacterProcessor() {
        AtomicBoolean singleUsed = new AtomicBoolean(false);
        AtomicBoolean multiUsed = new AtomicBoolean(false);
        Explanation singleExplanation = new Explanation("single");
        Explanation multiExplanation = new Explanation("multi");

        ConfigurableExplanationProvider provider = new ConfigurableExplanationProvider(
            hanzi -> {
                singleUsed.set(true);
                return singleExplanation;
            },
            hanzi -> {
                multiUsed.set(true);
                return multiExplanation;
            },
            "test",
            "description"
        );

        Explanation result = provider.getExplanation(new Hanzi("好"));

        assertThat(result).isEqualTo(singleExplanation);
        assertThat(singleUsed).isTrue();
        assertThat(multiUsed).isFalse();
    }

    @Test
    void shouldUseMultiCharacterProcessor() {
        AtomicBoolean singleUsed = new AtomicBoolean(false);
        AtomicBoolean multiUsed = new AtomicBoolean(false);
        Explanation singleExplanation = new Explanation("single");
        Explanation multiExplanation = new Explanation("multi");

        ConfigurableExplanationProvider provider = new ConfigurableExplanationProvider(
            hanzi -> {
                singleUsed.set(true);
                return singleExplanation;
            },
            hanzi -> {
                multiUsed.set(true);
                return multiExplanation;
            },
            "test",
            "description"
        );

        Explanation result = provider.getExplanation(new Hanzi("学校"));

        assertThat(result).isEqualTo(multiExplanation);
        assertThat(singleUsed).isFalse();
        assertThat(multiUsed).isTrue();
    }
}

