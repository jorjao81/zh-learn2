package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Explanation;

public interface ExplanationProvider {
    String getName();
    String getDescription();
    Explanation getExplanation(Hanzi word);
}