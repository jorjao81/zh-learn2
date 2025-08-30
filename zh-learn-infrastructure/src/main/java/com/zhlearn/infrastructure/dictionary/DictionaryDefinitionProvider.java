package com.zhlearn.infrastructure.dictionary;

import com.zhlearn.domain.dictionary.Dictionary;
import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.provider.DefinitionProvider;

public class DictionaryDefinitionProvider implements DefinitionProvider {
    private final Dictionary dictionary;

    public DictionaryDefinitionProvider(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public String getName() {
        return "dictionary-definition-" + dictionary.getName();
    }

    @Override
    public Definition getDefinition(Hanzi word) {
        return dictionary.lookup(word.characters())
            .map(analysis -> analysis.definition())
            .orElse(null);
    }
}