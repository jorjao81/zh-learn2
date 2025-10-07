package com.zhlearn.infrastructure.pinyin4j;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;

class Pinyin4jProviderTest {

    private final Pinyin4jProvider provider = new Pinyin4jProvider();

    @Test
    void shouldReturnProviderName() {
        assertThat(provider.getName()).isEqualTo("pinyin4j");
    }

    @Test
    void shouldConvertSingleChineseCharacter() {
        Hanzi word = new Hanzi("好");
        Pinyin pinyin = provider.getPinyin(word);
        assertThat(pinyin.pinyin()).isEqualTo("hăo");
    }

    @Test
    void shouldConvertMultipleChineseCharacters() {
        Hanzi word = new Hanzi("你好");
        Pinyin pinyin = provider.getPinyin(word);
        assertThat(pinyin.pinyin()).isEqualTo("nĭhăo");
    }

    @Test
    void shouldConvertCommonWords() {
        Hanzi word1 = new Hanzi("学习");
        Hanzi word2 = new Hanzi("中文");
        Hanzi word3 = new Hanzi("汉语");

        Pinyin pinyin1 = provider.getPinyin(word1);
        Pinyin pinyin2 = provider.getPinyin(word2);
        Pinyin pinyin3 = provider.getPinyin(word3);

        assertThat(pinyin1.pinyin()).isEqualTo("xuéxí");
        assertThat(pinyin2.pinyin()).isEqualTo("zhōngwén");
        assertThat(pinyin3.pinyin()).isEqualTo("hànyŭ");
    }

    @Test
    void shouldReturnLowercasePinyin() {
        Hanzi word = new Hanzi("北京");
        Pinyin pinyin = provider.getPinyin(word);
        assertThat(pinyin.pinyin()).isEqualTo("bĕijīng");
    }

    @Test
    void shouldHandleMixedContentWithSpacing() {
        Hanzi word1 = new Hanzi("Hello中文");
        Pinyin pinyin1 = provider.getPinyin(word1);
        assertThat(pinyin1.pinyin()).isEqualTo("Hello zhōngwén");

        Hanzi word2 = new Hanzi("第1章");
        Pinyin pinyin2 = provider.getPinyin(word2);
        assertThat(pinyin2.pinyin()).isEqualTo("dì 1 zhāng");
    }

    @Test
    void shouldHandleComplexCharacters() {
        Hanzi word = new Hanzi("繁体字");
        Pinyin pinyin = provider.getPinyin(word);
        assertThat(pinyin.pinyin()).isEqualTo("fántĭzì");
    }

    @Test
    void shouldHandleNeutralTone() {
        Hanzi word = new Hanzi("的");
        Pinyin pinyin = provider.getPinyin(word);
        assertThat(pinyin.pinyin()).isEqualTo("de");
    }

    @Test
    void shouldNotAddSpacesBetweenChineseCharacters() {
        Hanzi word = new Hanzi("学习");
        Pinyin pinyin = provider.getPinyin(word);
        assertThat(pinyin.pinyin()).isEqualTo("xuéxí");
    }
}
