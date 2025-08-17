package com.zhlearn.domain.provider;

import com.zhlearn.domain.model.ChineseWord;
import com.zhlearn.domain.model.StructuralDecomposition;

public interface StructuralDecompositionProvider {
    String getName();
    StructuralDecomposition getStructuralDecomposition(ChineseWord word);
}