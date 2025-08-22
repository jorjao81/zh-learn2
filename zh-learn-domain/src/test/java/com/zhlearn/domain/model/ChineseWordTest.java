package com.zhlearn.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ChineseWordTest {
    
    @Test
    void shouldCreateValidChineseWord() {
        ChineseWord word = new ChineseWord("汉语");
        
        assertThat(word.characters()).isEqualTo("汉语");
    }
    
    @Test
    void shouldThrowExceptionForNullCharacters() {
        assertThatThrownBy(() -> new ChineseWord(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Chinese word characters cannot be null or empty");
    }
    
    @Test
    void shouldThrowExceptionForEmptyCharacters() {
        assertThatThrownBy(() -> new ChineseWord(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Chinese word characters cannot be null or empty");
    }
    
    @Test
    void shouldThrowExceptionForBlankCharacters() {
        assertThatThrownBy(() -> new ChineseWord("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Chinese word characters cannot be null or empty");
    }
}