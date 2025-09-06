package com.zhlearn.application.service;

import com.zhlearn.domain.model.*;
import com.zhlearn.domain.provider.*;
import com.zhlearn.domain.service.WordAnalysisService;

import java.util.Optional;

public class WordAnalysisServiceImpl implements WordAnalysisService {
    
    private final ProviderRegistry providerRegistry;
    
    public WordAnalysisServiceImpl(ProviderRegistry registry) {
        this.providerRegistry = registry;
    }

    @Override
    public Pinyin getPinyin(Hanzi word, String providerName) {
        return providerRegistry.getPinyinProvider(providerName)
            .orElseThrow(() -> new IllegalArgumentException("Pinyin provider not found: " + providerName))
            .getPinyin(word);
    }
    
    @Override
    public Definition getDefinition(Hanzi word, String providerName) {
        return providerRegistry.getDefinitionProvider(providerName)
            .orElseThrow(() -> new IllegalArgumentException("Definition provider not found: " + providerName))
            .getDefinition(word);
    }
    
    @Override
    public StructuralDecomposition getStructuralDecomposition(Hanzi word, String providerName) {
        return providerRegistry.getStructuralDecompositionProvider(providerName)
            .orElseThrow(() -> new IllegalArgumentException("Structural decomposition provider not found: " + providerName))
            .getStructuralDecomposition(word);
    }
    
    @Override
    public Example getExamples(Hanzi word, String providerName) {
        return providerRegistry.getExampleProvider(providerName)
            .orElseThrow(() -> new IllegalArgumentException("Example provider not found: " + providerName))
            .getExamples(word, Optional.empty());
    }
    
    @Override
    public Example getExamples(Hanzi word, String providerName, String definition) {
        return providerRegistry.getExampleProvider(providerName)
            .orElseThrow(() -> new IllegalArgumentException("Example provider not found: " + providerName))
            .getExamples(word, Optional.of(definition));
    }
    
    @Override
    public Explanation getExplanation(Hanzi word, String providerName) {
        return providerRegistry.getExplanationProvider(providerName)
            .orElseThrow(() -> new IllegalArgumentException("Explanation provider not found: " + providerName))
            .getExplanation(word);
    }

    @Override
    public java.util.Optional<String> getPronunciation(Hanzi word, Pinyin pinyin, String providerName) {
        return providerRegistry.getAudioProvider(providerName)
            .orElseThrow(() -> new IllegalArgumentException("Audio provider not found: " + providerName))
            .getPronunciation(word, pinyin);
    }
    
    @Override
    public WordAnalysis getCompleteAnalysis(Hanzi word, String providerName) {
        Definition definition = getDefinition(word, providerName);
        return new WordAnalysis(
            word,
            getPinyin(word, providerName),
            definition,
            getStructuralDecomposition(word, providerName),
            getExamples(word, providerName, definition.meaning()),
            getExplanation(word, providerName),
            providerName,
            providerName, // pinyinProvider
            providerName, // definitionProvider
            providerName, // decompositionProvider
            providerName, // exampleProvider
            providerName  // explanationProvider
        );
    }
    
    @Override
    public WordAnalysis getCompleteAnalysis(Hanzi word, ProviderConfiguration config) {
        Definition definition = getDefinition(word, config.getDefinitionProvider());
        return new WordAnalysis(
            word,
            getPinyin(word, config.getPinyinProvider()),
            definition,
            getStructuralDecomposition(word, config.getDecompositionProvider()),
            getExamples(word, config.getExampleProvider(), definition.meaning()),
            getExplanation(word, config.getExplanationProvider()),
            config.getDefaultProvider(),
            config.getPinyinProvider(),
            config.getDefinitionProvider(),
            config.getDecompositionProvider(),
            config.getExampleProvider(),
            config.getExplanationProvider()
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
        providerRegistry.registerStructuralDecompositionProvider(provider);
    }

    @Override
    public void addExplanationProvider(String name, ExplanationProvider provider) {
        providerRegistry.registerExplanationProvider(provider);
    }

    @Override
    public void addAudioProvider(String name, AudioProvider provider) {
        providerRegistry.registerAudioProvider(provider);
    }
}
