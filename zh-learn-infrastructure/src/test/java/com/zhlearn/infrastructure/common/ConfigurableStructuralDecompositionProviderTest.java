package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurableStructuralDecompositionProviderTest {

    @Test
    void shouldUseSingleCharacterProcessor() {
        AtomicBoolean singleUsed = new AtomicBoolean(false);
        AtomicBoolean multiUsed = new AtomicBoolean(false);
        StructuralDecomposition singleDecomposition = new StructuralDecomposition("single");
        StructuralDecomposition multiDecomposition = new StructuralDecomposition("multi");

        ConfigurableStructuralDecompositionProvider provider = new ConfigurableStructuralDecompositionProvider(
            hanzi -> {
                singleUsed.set(true);
                return singleDecomposition;
            },
            hanzi -> {
                multiUsed.set(true);
                return multiDecomposition;
            },
            "test",
            "description"
        );

        StructuralDecomposition result = provider.getStructuralDecomposition(new Hanzi("好"));

        assertThat(result).isEqualTo(singleDecomposition);
        assertThat(singleUsed).isTrue();
        assertThat(multiUsed).isFalse();
    }

    @Test
    void shouldUseMultiCharacterProcessor() {
        AtomicBoolean singleUsed = new AtomicBoolean(false);
        AtomicBoolean multiUsed = new AtomicBoolean(false);
        StructuralDecomposition singleDecomposition = new StructuralDecomposition("single");
        StructuralDecomposition multiDecomposition = new StructuralDecomposition("multi");

        ConfigurableStructuralDecompositionProvider provider = new ConfigurableStructuralDecompositionProvider(
            hanzi -> {
                singleUsed.set(true);
                return singleDecomposition;
            },
            hanzi -> {
                multiUsed.set(true);
                return multiDecomposition;
            },
            "test",
            "description"
        );

        StructuralDecomposition result = provider.getStructuralDecomposition(new Hanzi("学校"));

        assertThat(result).isEqualTo(multiDecomposition);
        assertThat(singleUsed).isFalse();
        assertThat(multiUsed).isTrue();
    }
}

