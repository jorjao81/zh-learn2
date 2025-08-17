package com.zhlearn.application.service;

import com.zhlearn.domain.model.*;
import com.zhlearn.domain.provider.*;
import com.zhlearn.domain.service.WordAnalysisService;

public class WordAnalysisServiceImpl implements WordAnalysisService {
    
    private final ProviderRegistry providerRegistry;
    
    public WordAnalysisServiceImpl(ProviderRegistry registry) {
        this.providerRegistry = registry;
    }

    @Override
    public Pinyin getPinyin(ChineseWord word, String providerName) {
        return providerRegistry.getPinyinProvider(providerName)
            .orElseThrow(() -> new IllegalArgumentException("Pinyin provider not found: " + providerName))
            .getPinyin(word);
    }
    
    @Override
    public Definition getDefinition(ChineseWord word, String providerName) {
        return providerRegistry.getDefinitionProvider(providerName)
            .orElseThrow(() -> new IllegalArgumentException("Definition provider not found: " + providerName))
            .getDefinition(word);
    }
    
    @Override
    public StructuralDecomposition getStructuralDecomposition(ChineseWord word, String providerName) {
        return providerRegistry.getStructuralDecompositionProvider(providerName)
            .orElseThrow(() -> new IllegalArgumentException("Structural decomposition provider not found: " + providerName))
            .getStructuralDecomposition(word);
    }
    
    @Override
    public Example getExamples(ChineseWord word, String providerName) {
        return providerRegistry.getExampleProvider(providerName)
            .orElseThrow(() -> new IllegalArgumentException("Example provider not found: " + providerName))
            .getExamples(word);
    }
    
    @Override
    public Explanation getExplanation(ChineseWord word, String providerName) {
        return providerRegistry.getExplanationProvider(providerName)
            .orElseThrow(() -> new IllegalArgumentException("Explanation provider not found: " + providerName))
            .getExplanation(word);
    }
    
    @Override
    public WordAnalysis getCompleteAnalysis(ChineseWord word, String providerName) {
        return new WordAnalysis(
            word,
            getPinyin(word, providerName),
            getDefinition(word, providerName),
            getStructuralDecomposition(word, providerName),
            getExamples(word, providerName),
            getExplanation(word, providerName),
            providerName
        );
    }

    @Override
    public void addPinyinProvider(String name, PinyinProvider provider) {
        providerRegistry.registerPinyinProvider(provider);
    }

    @Override
    public void addDefinitionProvider(String name, DefinitionProvider provider) {
        providerRegistry.registerDefinitionProvider(provider);
    }

    @Override
    public void addStructuralDecompositionProvider(String name, StructuralDecompositionProvider provider) {

    }

    @Override
    public void addExplanationProvider(String name, ExplanationProvider provider) {

    }
}