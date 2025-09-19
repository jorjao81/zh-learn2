package com.zhlearn.domain.service;

import com.zhlearn.domain.model.*;
import com.zhlearn.domain.model.ProviderConfiguration;

import java.nio.file.Path;
import java.util.Optional;

public interface WordAnalysisService {
    Pinyin getPinyin(Hanzi word, String providerName);
    Definition getDefinition(Hanzi word, String providerName);
    StructuralDecomposition getStructuralDecomposition(Hanzi word, String providerName);
    Example getExamples(Hanzi word, String providerName);
    Example getExamples(Hanzi word, String providerName, String definition);
    Explanation getExplanation(Hanzi word, String providerName);
    Optional<Path> getPronunciation(Hanzi word, Pinyin pinyin, String providerName);
    
    WordAnalysis getCompleteAnalysis(Hanzi word, String providerName);
    WordAnalysis getCompleteAnalysis(Hanzi word, ProviderConfiguration config);
}
