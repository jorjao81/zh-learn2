package com.zhlearn.domain.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HanziTest {

    @Test
    void shouldCreateValidChineseWord() {
        Hanzi word = new Hanzi("汉语");

        assertThat(word.characters()).isEqualTo("汉语");
    }

    @Test
    void shouldThrowExceptionForNullCharacters() {
        assertThatThrownBy(() -> new Hanzi(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Chinese word characters cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionForEmptyCharacters() {
        assertThatThrownBy(() -> new Hanzi(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Chinese word characters cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionForBlankCharacters() {
        assertThatThrownBy(() -> new Hanzi("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Chinese word characters cannot be null or empty");
    }

    @Test
    void shouldDetectSingleCharacter() {
        Hanzi single = new Hanzi("学");

        assertThat(single.isSingleCharacter()).isTrue();
        assertThat(single.isMultiCharacter()).isFalse();
    }

    @Test
    void shouldDetectSurrogatePairAsSingleCharacter() {
        Hanzi single = new Hanzi("\uD840\uDC00");

        assertThat(single.isSingleCharacter()).isTrue();
        assertThat(single.isMultiCharacter()).isFalse();
    }

    @Test
    void shouldDetectMultiCharacterWord() {
        Hanzi multi = new Hanzi("学校");

        assertThat(multi.isSingleCharacter()).isFalse();
        assertThat(multi.isMultiCharacter()).isTrue();
    }
}
