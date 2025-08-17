package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.ChineseWord;
import com.zhlearn.domain.model.Pinyin;

public interface PinyinProvider {
    String getName();
    Pinyin getPinyin(ChineseWord word);
}