package com.zhlearn.infrastructure.dummy;

import com.zhlearn.domain.model.ChineseWord;
import com.zhlearn.domain.model.Explanation;
import com.zhlearn.domain.provider.ExplanationProvider;

import java.util.List;

public class DummyExplanationProvider implements ExplanationProvider {
    
    @Override
    public String getName() {
        return "dummy";
    }
    
    @Override
    public Explanation getExplanation(ChineseWord word) {
        return new Explanation(
            "Dummy explanation for " + word.characters() + ": This word has ancient origins and is commonly used in daily conversation. It has cultural significance in Chinese society."
        );
    }
}