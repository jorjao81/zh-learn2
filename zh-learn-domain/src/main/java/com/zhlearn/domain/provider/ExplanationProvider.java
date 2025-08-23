package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Explanation;

public interface ExplanationProvider {
    String getName();
    Explanation getExplanation(Hanzi word);
}