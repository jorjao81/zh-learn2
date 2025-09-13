package com.zhlearn.infrastructure.dictionary;

import com.zhlearn.domain.dictionary.Dictionary;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.PinyinProvider;

public class DictionaryPinyinProvider implements PinyinProvider {
    private final Dictionary dictionary;

    public DictionaryPinyinProvider(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public String getName() {
        // Use the dictionary's name directly (e.g., "pleco-export")
        return dictionary.getName();
    }
    
    @Override
    public String getDescription() {
        return "Dictionary-based pinyin provider using " + dictionary.getName() + " dictionary data";
    }
    
    @Override
    public ProviderType getType() { return ProviderType.DICTIONARY; }

    @Override
    public Pinyin getPinyin(Hanzi word) {
        return dictionary.lookup(word.characters())
            .map(analysis -> analysis.pinyin())
            .orElse(null);
    }
}
