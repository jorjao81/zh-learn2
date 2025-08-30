package com.zhlearn.domain.service;

import com.zhlearn.domain.model.*;
import com.zhlearn.domain.provider.*;
import com.zhlearn.domain.model.ProviderConfiguration;

public interface WordAnalysisService {
    Pinyin getPinyin(Hanzi word, String providerName);
    Definition getDefinition(Hanzi word, String providerName);
    StructuralDecomposition getStructuralDecomposition(Hanzi word, String providerName);
    Example getExamples(Hanzi word, String providerName);
    Example getExamples(Hanzi word, String providerName, String definition);
    Explanation getExplanation(Hanzi word, String providerName);
    
    WordAnalysis getCompleteAnalysis(Hanzi word, String providerName);
    WordAnalysis getCompleteAnalysis(Hanzi word, ProviderConfiguration config);

    void addPinyinProvider(String name, PinyinProvider provider);
    void addDefinitionProvider(String name, DefinitionProvider provider);
    void addStructuralDecompositionProvider(String name, StructuralDecompositionProvider provider);
    void addExplanationProvider(String name, ExplanationProvider provider);

}