package com.zhlearn.infrastructure.dictionary;

import com.zhlearn.domain.dictionary.Dictionary;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ExplanationProvider;

public class DictionaryExplanationProvider implements ExplanationProvider {
    private final Dictionary dictionary;

    public DictionaryExplanationProvider(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public String getName() {
        return "dictionary-explanation-" + dictionary.getName();
    }

    @Override
    public String getDescription() {
        return "Dictionary-based explanation provider using "
                + dictionary.getName()
                + " dictionary data";
    }

    @Override
    public ProviderType getType() {
        return ProviderType.DICTIONARY;
    }

    @Override
    public Explanation getExplanation(Hanzi word) {
        return dictionary
                .lookup(word.characters())
                .map(analysis -> analysis.explanation())
                .orElse(null);
    }
}
