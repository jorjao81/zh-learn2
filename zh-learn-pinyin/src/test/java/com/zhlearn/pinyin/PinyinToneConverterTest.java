package com.zhlearn.pinyin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PinyinToneConverterTest {

    @Test
    void convertsSingleSyllable() {
        assertEquals("shùn", PinyinToneConverter.convertToToneMarks("shun4"));
        assertEquals("mà", PinyinToneConverter.convertToToneMarks("ma4"));
    }

    @Test
    void convertsMultiSyllable() {
        assertEquals("zhōng guó", PinyinToneConverter.convertToToneMarks("zhong1 guo2"));
        assertEquals("hǎo péngyǒu", PinyinToneConverter.convertToToneMarks("hao3 peng2you3"));
    }

    @Test
    void appliesOuRuleAndIuUiRules() {
        assertEquals("ǒu", PinyinToneConverter.convertToToneMarks("ou3"));
        assertEquals("shuǐ", PinyinToneConverter.convertToToneMarks("shui3"));
        assertEquals("liú", PinyinToneConverter.convertToToneMarks("liu2"));
    }

    @Test
    void handlesUmlautVariants() {
        assertEquals("lüè", PinyinToneConverter.convertToToneMarks("lu:e4"));
        assertEquals("lüè", PinyinToneConverter.convertToToneMarks("lve4"));
        assertEquals("lǜ", PinyinToneConverter.convertToToneMarks("lv4"));
    }

    @Test
    void neutralToneDropsNumber() {
        assertEquals("r", PinyinToneConverter.convertToToneMarks("r5"));
    }
}
