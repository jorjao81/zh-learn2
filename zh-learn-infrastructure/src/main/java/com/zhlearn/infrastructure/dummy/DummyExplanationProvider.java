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
            "Dummy etymology for " + word.characters() + ": This word has ancient origins.",
            "Dummy usage explanation for " + word.characters() + ": This word is commonly used in daily conversation.",
            List.of("相似词1", "相似词2", "相似词3"),
            "Dummy cultural context for " + word.characters() + ": This word has cultural significance in Chinese society."
        );
    }
}