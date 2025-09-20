package com.zhlearn.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HanziTest {

    @Test
    void shouldIdentifySingleCharacterWord() {
        Hanzi hanzi = new Hanzi("好");

        assertThat(hanzi.isSingleCharacter()).isTrue();
        assertThat(hanzi.isMultiCharacter()).isFalse();
    }

    @Test
    void shouldIdentifyMultiCharacterWord() {
        Hanzi hanzi = new Hanzi("学校");

        assertThat(hanzi.isSingleCharacter()).isFalse();
        assertThat(hanzi.isMultiCharacter()).isTrue();
    }

    @Test
    void shouldTreatSupplementaryCharacterAsSingleCharacter() {
        Hanzi hanzi = new Hanzi("𠮷");

        assertThat(hanzi.isSingleCharacter()).isTrue();
        assertThat(hanzi.isMultiCharacter()).isFalse();
    }
}

