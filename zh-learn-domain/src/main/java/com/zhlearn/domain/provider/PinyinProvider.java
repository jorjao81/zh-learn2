package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;

public interface PinyinProvider {
    String getName();

    String getDescription();

    ProviderType getType();

    Pinyin getPinyin(Hanzi word);
}
