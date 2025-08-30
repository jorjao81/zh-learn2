package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;

public interface PinyinProvider {
    String getName();
    Pinyin getPinyin(Hanzi word);
}