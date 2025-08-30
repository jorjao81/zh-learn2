package com.zhlearn.infrastructure.dummy;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.provider.PinyinProvider;

public class DummyPinyinProvider implements PinyinProvider {
    
    @Override
    public String getName() {
        return "dummy";
    }
    
    @Override
    public Pinyin getPinyin(Hanzi word) {
        return new Pinyin("dummy-pinyin-" + word.characters());
    }
}