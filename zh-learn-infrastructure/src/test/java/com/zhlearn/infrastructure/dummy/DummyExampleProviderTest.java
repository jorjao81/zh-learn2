package com.zhlearn.infrastructure.dummy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Hanzi;

class DummyExampleProviderTest {

    private final DummyExampleProvider provider = new DummyExampleProvider();

    @Test
    void singleCharacter_includesBreakdown() {
        Hanzi singleChar = new Hanzi("学");
        Example result = provider.getExamples(singleChar, Optional.empty());

        assertThat(result.usages()).hasSize(2);
        assertThat(result.usages().get(0).breakdown()).isNotNull();
        assertThat(result.usages().get(0).breakdown()).contains("学 + 计");
        assertThat(result.usages().get(1).breakdown()).isNotNull();
        assertThat(result.usages().get(1).breakdown()).contains("学 + 价");
    }

    @Test
    void multiCharacter_excludesBreakdown() {
        Hanzi multiChar = new Hanzi("学习");
        Example result = provider.getExamples(multiChar, Optional.empty());

        assertThat(result.usages()).hasSize(2);
        assertThat(result.usages().get(0).breakdown()).isNull();
        assertThat(result.usages().get(1).breakdown()).isNull();
    }
}
