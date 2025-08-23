package com.zhlearn.infrastructure.dummy;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DummyPinyinProviderTest {
    
    private final DummyPinyinProvider provider = new DummyPinyinProvider();
    
    @Test
    void shouldReturnProviderName() {
        assertThat(provider.getName()).isEqualTo("dummy");
    }
    
    @Test
    void shouldProvideDummyPinyin() {
        Hanzi word = new Hanzi("汉语");
        
        Pinyin pinyin = provider.getPinyin(word);
        
        assertThat(pinyin.pinyin()).isEqualTo("dummy-pinyin-汉语");
    }
    
    @Test
    void shouldHandleDifferentWords() {
        Hanzi word1 = new Hanzi("学习");
        Hanzi word2 = new Hanzi("中文");
        
        Pinyin pinyin1 = provider.getPinyin(word1);
        Pinyin pinyin2 = provider.getPinyin(word2);
        
        assertThat(pinyin1.pinyin()).isEqualTo("dummy-pinyin-学习");
        assertThat(pinyin2.pinyin()).isEqualTo("dummy-pinyin-中文");
    }
}