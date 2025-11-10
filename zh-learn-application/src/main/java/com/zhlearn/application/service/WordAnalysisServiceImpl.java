package com.zhlearn.application.service;

import java.nio.file.Path;
import java.util.Optional;

import com.zhlearn.domain.model.*;
import com.zhlearn.domain.provider.*;
import com.zhlearn.domain.service.WordAnalysisService;

public class WordAnalysisServiceImpl implements WordAnalysisService {

    private final ExampleProvider exampleProvider;
    private final ExplanationProvider explanationProvider;
    private final StructuralDecompositionProvider decompositionProvider;
    private final PinyinProvider pinyinProvider;
    private final DefinitionProvider definitionProvider;
    private final DefinitionFormatterProvider definitionFormatterProvider;
    private final DefinitionGeneratorProvider definitionGeneratorProvider;
    private final AudioProvider audioProvider;

    public WordAnalysisServiceImpl(
            ExampleProvider exampleProvider,
            ExplanationProvider explanationProvider,
            StructuralDecompositionProvider decompositionProvider,
            PinyinProvider pinyinProvider,
            DefinitionProvider definitionProvider,
            DefinitionFormatterProvider definitionFormatterProvider,
            DefinitionGeneratorProvider definitionGeneratorProvider,
            AudioProvider audioProvider) {
        this.exampleProvider = exampleProvider;
        this.explanationProvider = explanationProvider;
        this.decompositionProvider = decompositionProvider;
        this.pinyinProvider = pinyinProvider;
        this.definitionProvider = definitionProvider;
        this.definitionFormatterProvider = definitionFormatterProvider;
        this.definitionGeneratorProvider = definitionGeneratorProvider;
        this.audioProvider = audioProvider;
    }

    @Override
    public Pinyin getPinyin(Hanzi word, String providerName) {
        return pinyinProvider.getPinyin(word);
    }

    @Override
    public Definition getDefinition(Hanzi word, String providerName) {
        Definition rawDefinition = definitionProvider.getDefinition(word);

        // If no raw definition and generator is available, generate one
        if (rawDefinition == null && definitionGeneratorProvider != null) {
            rawDefinition = definitionGeneratorProvider.generateDefinition(word);
        }

        // Format definition if formatter is available
        if (definitionFormatterProvider != null) {
            Optional<String> rawText =
                    rawDefinition != null ? Optional.of(rawDefinition.meaning()) : Optional.empty();
            Definition formatted = definitionFormatterProvider.formatDefinition(word, rawText);
            if (formatted != null) {
                return formatted;
            }
        }

        return rawDefinition;
    }

    @Override
    public StructuralDecomposition getStructuralDecomposition(Hanzi word, String providerName) {
        return decompositionProvider.getStructuralDecomposition(word);
    }

    @Override
    public Example getExamples(Hanzi word, String providerName) {
        return exampleProvider.getExamples(word, Optional.empty());
    }

    @Override
    public Example getExamples(Hanzi word, String providerName, String definition) {
        return exampleProvider.getExamples(word, Optional.of(definition));
    }

    @Override
    public Explanation getExplanation(Hanzi word, String providerName) {
        return explanationProvider.getExplanation(word);
    }

    @Override
    public Optional<Path> getPronunciation(Hanzi word, Pinyin pinyin, String providerName) {
        if (providerName == null || providerName.isBlank()) {
            return Optional.empty();
        }
        return audioProvider.getPronunciation(word, pinyin);
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
                getPronunciation(word, pinyin, providerName));
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
                getPronunciation(word, pinyin, config.getAudioProvider()));
    }
}
