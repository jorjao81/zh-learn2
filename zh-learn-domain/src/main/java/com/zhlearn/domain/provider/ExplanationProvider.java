package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;

public interface ExplanationProvider {
    String getName();
    String getDescription();
    ProviderType getType();
    Explanation getExplanation(Hanzi word);
}
