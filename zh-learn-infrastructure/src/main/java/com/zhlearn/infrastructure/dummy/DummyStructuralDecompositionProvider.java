package com.zhlearn.infrastructure.dummy;

import com.zhlearn.domain.model.ChineseWord;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;

import java.util.List;

public class DummyStructuralDecompositionProvider implements StructuralDecompositionProvider {
    
    @Override
    public String getName() {
        return "dummy";
    }
    
    @Override
    public StructuralDecomposition getStructuralDecomposition(ChineseWord word) {
        return new StructuralDecomposition(
            "Dummy structural decomposition for " + word.characters() + ": Component breakdown with radicals and meanings."
        );
    }
}