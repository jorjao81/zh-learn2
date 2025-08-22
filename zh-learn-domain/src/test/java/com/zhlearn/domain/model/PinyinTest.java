package com.zhlearn.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PinyinTest {
    
    @Test
    void shouldCreateValidPinyin() {
        Pinyin pinyin = new Pinyin("hànyǔ");
        
        assertThat(pinyin.pinyin()).isEqualTo("hànyǔ");
    }
    
    @Test
    void shouldThrowExceptionForNullPinyin() {
        assertThatThrownBy(() -> new Pinyin(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Pinyin cannot be null or empty");
    }
    
    @Test
    void shouldThrowExceptionForEmptyPinyin() {
        assertThatThrownBy(() -> new Pinyin(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Pinyin cannot be null or empty");
    }
    
    @Test
    void shouldThrowExceptionForWhitespacePinyin() {
        assertThatThrownBy(() -> new Pinyin("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Pinyin cannot be null or empty");
    }
}