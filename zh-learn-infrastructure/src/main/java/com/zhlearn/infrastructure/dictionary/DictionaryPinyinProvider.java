package com.zhlearn.infrastructure.dictionary;

import com.zhlearn.domain.dictionary.Dictionary;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.provider.PinyinProvider;

public class DictionaryPinyinProvider implements PinyinProvider {
    private final Dictionary dictionary;

    public DictionaryPinyinProvider(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public String getName() {
        return "dictionary-pinyin-" + dictionary.getName();
    }

    @Override
    public Pinyin getPinyin(Hanzi word) {
        return dictionary.lookup(word.characters())
            .map(analysis -> analysis.pinyin())
            .orElse(null);
    }
}