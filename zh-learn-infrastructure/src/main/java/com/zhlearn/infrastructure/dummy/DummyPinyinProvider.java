package com.zhlearn.infrastructure.dummy;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.PinyinProvider;

public class DummyPinyinProvider implements PinyinProvider {
    
    @Override
    public String getName() {
        return "dummy";
    }
    
    @Override
    public String getDescription() {
        return "Test provider that returns dummy pinyin for development and testing";
    }
    
    @Override
    public ProviderType getType() { return ProviderType.DUMMY; }
    
    @Override
    public Pinyin getPinyin(Hanzi word) {
        return new Pinyin("dummy-pinyin-" + word.characters());
    }
}
