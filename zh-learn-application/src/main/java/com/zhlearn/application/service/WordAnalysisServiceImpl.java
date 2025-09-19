package com.zhlearn.application.service;

import com.zhlearn.domain.model.*;
import com.zhlearn.domain.provider.*;
import com.zhlearn.domain.service.WordAnalysisService;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class WordAnalysisServiceImpl implements WordAnalysisService {

    private final Map<String, PinyinProvider> pinyinProviders;
    private final Map<String, DefinitionProvider> definitionProviders;
    private final Map<String, StructuralDecompositionProvider> decompositionProviders;
    private final Map<String, ExampleProvider> exampleProviders;
    private final Map<String, ExplanationProvider> explanationProviders;
    private final Map<String, AudioProvider> audioProviders;

    public WordAnalysisServiceImpl(
        Map<String, PinyinProvider> pinyinProviders,
        Map<String, DefinitionProvider> definitionProviders,
        Map<String, StructuralDecompositionProvider> decompositionProviders,
        Map<String, ExampleProvider> exampleProviders,
        Map<String, ExplanationProvider> explanationProviders,
        Map<String, AudioProvider> audioProviders
    ) {
        this.pinyinProviders = pinyinProviders;
        this.definitionProviders = definitionProviders;
        this.decompositionProviders = decompositionProviders;
        this.exampleProviders = exampleProviders;
        this.explanationProviders = explanationProviders;
        this.audioProviders = audioProviders;
    }

    @Override
    public Pinyin getPinyin(Hanzi word, String providerName) {
        return optionalProvider(pinyinProviders, providerName)
            .orElseThrow(() -> new IllegalArgumentException("Pinyin provider not found: " + providerName))
            .getPinyin(word);
    }

    @Override
    public Definition getDefinition(Hanzi word, String providerName) {
        return optionalProvider(definitionProviders, providerName)
            .orElseThrow(() -> new IllegalArgumentException("Definition provider not found: " + providerName))
            .getDefinition(word);
    }

    @Override
    public StructuralDecomposition getStructuralDecomposition(Hanzi word, String providerName) {
        return optionalProvider(decompositionProviders, providerName)
            .orElseThrow(() -> new IllegalArgumentException("Structural decomposition provider not found: " + providerName))
            .getStructuralDecomposition(word);
    }

    @Override
    public Example getExamples(Hanzi word, String providerName) {
        return optionalProvider(exampleProviders, providerName)
            .orElseThrow(() -> new IllegalArgumentException("Example provider not found: " + providerName))
            .getExamples(word, Optional.empty());
    }

    @Override
    public Example getExamples(Hanzi word, String providerName, String definition) {
        return optionalProvider(exampleProviders, providerName)
            .orElseThrow(() -> new IllegalArgumentException("Example provider not found: " + providerName))
            .getExamples(word, Optional.of(definition));
    }

    @Override
    public Explanation getExplanation(Hanzi word, String providerName) {
        return optionalProvider(explanationProviders, providerName)
            .orElseThrow(() -> new IllegalArgumentException("Explanation provider not found: " + providerName))
            .getExplanation(word);
    }

    @Override
    public Optional<Path> getPronunciation(Hanzi word, Pinyin pinyin, String providerName) {
        if (providerName == null || providerName.isBlank()) {
            return Optional.empty();
        }
        return optionalProvider(audioProviders, providerName)
            .map(p -> p.getPronunciation(word, pinyin))
            .orElse(Optional.empty());
    }

    @Override
    public WordAnalysis getCompleteAnalysis(Hanzi word, String providerName) {
        Definition definition = getDefinition(word, providerName);
        Pinyin pinyin = getPinyin(word, providerName);
        return new WordAnalysis(
            word,
            pinyin,
            definition,
            getStructuralDecomposition(word, providerName),
            getExamples(word, providerName, definition.meaning()),
            getExplanation(word, providerName),
            getPronunciation(word, pinyin, providerName)
        );
    }

    @Override
    public WordAnalysis getCompleteAnalysis(Hanzi word, ProviderConfiguration config) {
        Definition definition = getDefinition(word, config.getDefinitionProvider());
        Pinyin pinyin = getPinyin(word, config.getPinyinProvider());
        return new WordAnalysis(
            word,
            pinyin,
            definition,
            getStructuralDecomposition(word, config.getDecompositionProvider()),
            getExamples(word, config.getExampleProvider(), definition.meaning()),
            getExplanation(word, config.getExplanationProvider()),
            getPronunciation(word, pinyin, config.getAudioProvider())
        );
    }

    public Map<String, PinyinProvider> getPinyinProviders() {
        return pinyinProviders;
    }

    public Map<String, DefinitionProvider> getDefinitionProviders() {
        return definitionProviders;
    }

    public Map<String, StructuralDecompositionProvider> getDecompositionProviders() {
        return decompositionProviders;
    }

    public Map<String, ExampleProvider> getExampleProviders() {
        return exampleProviders;
    }

    public Map<String, ExplanationProvider> getExplanationProviders() {
        return explanationProviders;
    }

    public Map<String, AudioProvider> getAudioProviders() {
        return audioProviders;
    }

    private <T> Optional<T> optionalProvider(Map<String, T> providers, String name) {
        if (name == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(providers.get(name));
    }
}
