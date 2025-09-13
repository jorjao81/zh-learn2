package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;

public interface StructuralDecompositionProvider {
    String getName();
    String getDescription();
    ProviderType getType();
    StructuralDecomposition getStructuralDecomposition(Hanzi word);
}
