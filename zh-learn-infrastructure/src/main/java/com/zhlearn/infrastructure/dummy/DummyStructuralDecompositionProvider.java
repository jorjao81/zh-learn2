package com.zhlearn.infrastructure.dummy;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;

public class DummyStructuralDecompositionProvider implements StructuralDecompositionProvider {
    
    @Override
    public String getName() {
        return "dummy";
    }
    
    @Override
    public StructuralDecomposition getStructuralDecomposition(Hanzi word) {
        return new StructuralDecomposition(
            "Dummy structural decomposition for " + word.characters() + ": Component breakdown with radicals and meanings."
        );
    }
}