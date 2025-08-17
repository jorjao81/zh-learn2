package com.zhlearn.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PinyinTest {
    
    @Test
    void shouldCreateValidPinyin() {
        Pinyin pinyin = new Pinyin("hanyu", "hànyǔ");
        
        assertThat(pinyin.romanization()).isEqualTo("hanyu");
        assertThat(pinyin.toneMarks()).isEqualTo("hànyǔ");
    }
    
    @Test
    void shouldThrowExceptionForNullRomanization() {
        assertThatThrownBy(() -> new Pinyin(null, "hànyǔ"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Pinyin romanization cannot be null or empty");
    }
    
    @Test
    void shouldThrowExceptionForEmptyRomanization() {
        assertThatThrownBy(() -> new Pinyin("", "hànyǔ"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Pinyin romanization cannot be null or empty");
    }
    
    @Test
    void shouldThrowExceptionForNullToneMarks() {
        assertThatThrownBy(() -> new Pinyin("hanyu", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Pinyin tone marks cannot be null or empty");
    }
    
    @Test
    void shouldThrowExceptionForEmptyToneMarks() {
        assertThatThrownBy(() -> new Pinyin("hanyu", ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Pinyin tone marks cannot be null or empty");
    }
}