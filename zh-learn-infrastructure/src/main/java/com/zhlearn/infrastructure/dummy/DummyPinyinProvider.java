package com.zhlearn.infrastructure.dummy;

import com.zhlearn.domain.model.ChineseWord;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.provider.PinyinProvider;

public class DummyPinyinProvider implements PinyinProvider {
    
    @Override
    public String getName() {
        return "dummy";
    }
    
    @Override
    public Pinyin getPinyin(ChineseWord word) {
        return new Pinyin(
            "dummy-romanization-" + word.characters(),
            "dummy-tones-" + word.characters()
        );
    }
}