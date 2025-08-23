package com.zhlearn.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

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
}