package com.zhlearn.infrastructure.dummy;

import com.zhlearn.domain.model.ChineseWord;
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
        ChineseWord word = new ChineseWord("汉语");
        
        Pinyin pinyin = provider.getPinyin(word);
        
        assertThat(pinyin.romanization()).isEqualTo("dummy-romanization-汉语");
        assertThat(pinyin.toneMarks()).isEqualTo("dummy-tones-汉语");
    }
    
    @Test
    void shouldHandleDifferentWords() {
        ChineseWord word1 = new ChineseWord("学习");
        ChineseWord word2 = new ChineseWord("中文");
        
        Pinyin pinyin1 = provider.getPinyin(word1);
        Pinyin pinyin2 = provider.getPinyin(word2);
        
        assertThat(pinyin1.romanization()).isEqualTo("dummy-romanization-学习");
        assertThat(pinyin2.romanization()).isEqualTo("dummy-romanization-中文");
        assertThat(pinyin1.toneMarks()).isEqualTo("dummy-tones-学习");
        assertThat(pinyin2.toneMarks()).isEqualTo("dummy-tones-中文");
    }
}