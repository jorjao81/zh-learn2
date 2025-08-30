package com.zhlearn.infrastructure.dictionary;

import com.zhlearn.domain.dictionary.Dictionary;
import com.zhlearn.domain.model.Example;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.provider.ExampleProvider;

import java.util.Optional;

public class DictionaryExampleProvider implements ExampleProvider {
    private final Dictionary dictionary;

    public DictionaryExampleProvider(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public String getName() {
        return "dictionary-example-" + dictionary.getName();
    }

    @Override
    public Example getExamples(Hanzi word, Optional<String> definition) {
        return dictionary.lookup(word.characters())
            .map(analysis -> analysis.examples())
            .orElse(null);
    }
}