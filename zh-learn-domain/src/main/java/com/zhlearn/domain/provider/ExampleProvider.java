package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.ChineseWord;
import com.zhlearn.domain.model.Example;

public interface ExampleProvider {
    String getName();
    Example getExamples(ChineseWord word);
}