package com.zhlearn.infrastructure.dummy;

import com.zhlearn.domain.model.ChineseWord;
import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.provider.DefinitionProvider;

public class DummyDefinitionProvider implements DefinitionProvider {
    
    @Override
    public String getName() {
        return "dummy";
    }
    
    @Override
    public Definition getDefinition(ChineseWord word) {
        return new Definition(
            "Dummy meaning for " + word.characters(),
            "noun"
        );
    }
}