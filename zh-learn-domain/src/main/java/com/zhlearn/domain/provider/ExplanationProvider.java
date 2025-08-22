package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.ChineseWord;
import com.zhlearn.domain.model.Explanation;

public interface ExplanationProvider {
    String getName();
    Explanation getExplanation(ChineseWord word);
}