package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;

public interface StructuralDecompositionProvider {
    String getName();
    String getDescription();
    StructuralDecomposition getStructuralDecomposition(Hanzi word);
}