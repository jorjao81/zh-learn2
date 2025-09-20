package com.zhlearn.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WordTypeTest {

    @Test
    void shouldReturnSingleCharacterType() {
        Hanzi hanzi = new Hanzi("好");

        WordType wordType = WordType.from(hanzi);

        assertThat(wordType).isEqualTo(WordType.SINGLE_CHARACTER);
    }

    @Test
    void shouldReturnMultiCharacterType() {
        Hanzi hanzi = new Hanzi("学校");

        WordType wordType = WordType.from(hanzi);

        assertThat(wordType).isEqualTo(WordType.MULTI_CHARACTER);
    }
}

