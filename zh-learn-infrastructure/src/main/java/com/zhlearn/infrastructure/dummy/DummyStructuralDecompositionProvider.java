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
        var components = List.of(
            new StructuralDecomposition.Component(
                word.characters().substring(0, 1),
                "dummy component meaning",
                "dummy radical"
            )
        );
        return new StructuralDecomposition(components);
    }
}