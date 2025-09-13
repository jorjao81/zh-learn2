package com.zhlearn.infrastructure.dummy;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;

public class DummyStructuralDecompositionProvider implements StructuralDecompositionProvider {
    
    @Override
    public String getName() {
        return "dummy";
    }
    
    @Override
    public String getDescription() {
        return "Test provider that returns dummy structural decomposition for development and testing";
    }
    
    @Override
    public ProviderType getType() { return ProviderType.DUMMY; }
    
    @Override
    public StructuralDecomposition getStructuralDecomposition(Hanzi word) {
        return new StructuralDecomposition(
            "Dummy structural decomposition for " + word.characters() + ": Component breakdown with radicals and meanings."
        );
    }
}
