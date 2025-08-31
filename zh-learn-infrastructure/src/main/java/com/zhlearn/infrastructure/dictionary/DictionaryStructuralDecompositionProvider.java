package com.zhlearn.infrastructure.dictionary;

import com.zhlearn.domain.dictionary.Dictionary;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.StructuralDecomposition;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;

public class DictionaryStructuralDecompositionProvider implements StructuralDecompositionProvider {
    private final Dictionary dictionary;

    public DictionaryStructuralDecompositionProvider(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public String getName() {
        return "dictionary-decomposition-" + dictionary.getName();
    }
    
    @Override
    public String getDescription() {
        return "Dictionary-based structural decomposition provider using " + dictionary.getName() + " dictionary data";
    }

    @Override
    public StructuralDecomposition getStructuralDecomposition(Hanzi word) {
        return dictionary.lookup(word.characters())
            .map(analysis -> analysis.structuralDecomposition())
            .orElse(null);
    }
}