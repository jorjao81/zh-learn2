package com.zhlearn.domain.service;

import com.zhlearn.domain.model.*;
import com.zhlearn.domain.provider.*;

public interface WordAnalysisService {
    Pinyin getPinyin(ChineseWord word, String providerName);
    Definition getDefinition(ChineseWord word, String providerName);
    StructuralDecomposition getStructuralDecomposition(ChineseWord word, String providerName);
    Example getExamples(ChineseWord word, String providerName);
    Explanation getExplanation(ChineseWord word, String providerName);
    
    WordAnalysis getCompleteAnalysis(ChineseWord word, String providerName);

    void addPinyinProvider(String name, PinyinProvider provider);
    void addDefinitionProvider(String name, DefinitionProvider provider);
    void addStructuralDecompositionProvider(String name, StructuralDecompositionProvider provider);
    void addExplanationProvider(String name, ExplanationProvider provider);

}