package com.zhlearn.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WordTypeTest {

    @Test
    void shouldIdentifySingleCharacterWordType() {
        Hanzi hanzi = new Hanzi("学");

        WordType type = WordType.from(hanzi);

        assertThat(type).isEqualTo(WordType.SINGLE_CHARACTER);
    }

    @Test
    void shouldIdentifyMultiCharacterWordType() {
        Hanzi hanzi = new Hanzi("学校");

        WordType type = WordType.from(hanzi);

        assertThat(type).isEqualTo(WordType.MULTI_CHARACTER);
    }
}
