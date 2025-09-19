package com.zhlearn.application.service;

import com.zhlearn.domain.model.*;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.domain.provider.ExampleProvider;
import com.zhlearn.domain.provider.ExplanationProvider;
import com.zhlearn.domain.provider.StructuralDecompositionProvider;
import com.zhlearn.domain.model.ProviderInfo;
import com.zhlearn.domain.service.WordAnalysisService;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelWordAnalysisService implements WordAnalysisService {

    private final WordAnalysisServiceImpl delegate;
    private final ExecutorService executorService;
    private final Map<String, StructuralDecompositionProvider> decompositionProviders;
    private final Map<String, ExampleProvider> exampleProviders;
    private final Map<String, ExplanationProvider> explanationProviders;
    private final Map<String, AudioProvider> audioProviders;

    public ParallelWordAnalysisService(WordAnalysisServiceImpl delegate,
                                       Map<String, StructuralDecompositionProvider> decompositionProviders,
                                       Map<String, ExampleProvider> exampleProviders,
                                       Map<String, ExplanationProvider> explanationProviders,
                                       Map<String, AudioProvider> audioProviders,
                                       int threadPoolSize) {
        this.delegate = delegate;
        this.decompositionProviders = decompositionProviders;
        this.exampleProviders = exampleProviders;
        this.explanationProviders = explanationProviders;
        this.audioProviders = audioProviders;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    public ParallelWordAnalysisService(WordAnalysisServiceImpl delegate,
                                       Map<String, StructuralDecompositionProvider> decompositionProviders,
                                       Map<String, ExampleProvider> exampleProviders,
                                       Map<String, ExplanationProvider> explanationProviders,
                                       Map<String, AudioProvider> audioProviders) {
        this(delegate, decompositionProviders, exampleProviders, explanationProviders, audioProviders, 10);
    }

    @Override
    public Pinyin getPinyin(Hanzi word, String providerName) {
        return delegate.getPinyin(word, providerName);
    }

    @Override
    public Definition getDefinition(Hanzi word, String providerName) {
        return delegate.getDefinition(word, providerName);
    }

    @Override
    public StructuralDecomposition getStructuralDecomposition(Hanzi word, String providerName) {
        return delegate.getStructuralDecomposition(word, providerName);
    }

    @Override
    public Example getExamples(Hanzi word, String providerName) {
        return delegate.getExamples(word, providerName);
    }

    @Override
    public Example getExamples(Hanzi word, String providerName, String definition) {
        return delegate.getExamples(word, providerName, definition);
    }

    @Override
    public Explanation getExplanation(Hanzi word, String providerName) {
        return delegate.getExplanation(word, providerName);
    }

    @Override
    public Optional<Path> getPronunciation(Hanzi word, Pinyin pinyin, String providerName) {
        return delegate.getPronunciation(word, pinyin, providerName);
    }

    @Override
    public WordAnalysis getCompleteAnalysis(Hanzi word, String providerName) {
        return delegate.getCompleteAnalysis(word, providerName);
    }

    @Override
    public WordAnalysis getCompleteAnalysis(Hanzi word, ProviderConfiguration config) {
        Definition definition = getDefinition(word, config.getDefinitionProvider());
        Pinyin pinyin = getPinyin(word, config.getPinyinProvider());

        boolean isDecompositionAI = isAIProvider(config.getDecompositionProvider());
        boolean isExampleAI = isAIProvider(config.getExampleProvider());
        boolean isExplanationAI = isAIProvider(config.getExplanationProvider());
        boolean isAudioAI = isAIProvider(config.getAudioProvider());

        if (!isDecompositionAI && !isExampleAI && !isExplanationAI && !isAudioAI) {
            return delegate.getCompleteAnalysis(word, config);
        }

        CompletableFuture<StructuralDecomposition> decompositionFuture;
        CompletableFuture<Example> examplesFuture;
        CompletableFuture<Explanation> explanationFuture;
        CompletableFuture<Optional<Path>> pronunciationFuture;

        if (isDecompositionAI) {
            decompositionFuture = CompletableFuture.supplyAsync(() ->
                getStructuralDecomposition(word, config.getDecompositionProvider()), executorService);
        } else {
            decompositionFuture = CompletableFuture.completedFuture(
                getStructuralDecomposition(word, config.getDecompositionProvider()));
        }

        if (isExampleAI) {
            examplesFuture = CompletableFuture.supplyAsync(() ->
                getExamples(word, config.getExampleProvider(), definition.meaning()), executorService);
        } else {
            examplesFuture = CompletableFuture.completedFuture(
                getExamples(word, config.getExampleProvider(), definition.meaning()));
        }

        if (isExplanationAI) {
            explanationFuture = CompletableFuture.supplyAsync(() ->
                getExplanation(word, config.getExplanationProvider()), executorService);
        } else {
            explanationFuture = CompletableFuture.completedFuture(
                getExplanation(word, config.getExplanationProvider()));
        }

        if (isAudioAI) {
            pronunciationFuture = CompletableFuture.supplyAsync(() ->
                getPronunciation(word, pinyin, config.getAudioProvider()), executorService);
        } else {
            pronunciationFuture = CompletableFuture.completedFuture(
                getPronunciation(word, pinyin, config.getAudioProvider()));
        }

        try {
            CompletableFuture.allOf(decompositionFuture, examplesFuture, explanationFuture, pronunciationFuture).join();
            return new WordAnalysis(
                word,
                pinyin,
                definition,
                decompositionFuture.get(),
                examplesFuture.get(),
                explanationFuture.get(),
                pronunciationFuture.get()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Parallel word analysis interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Error in parallel word analysis: " + e.getCause().getMessage(), e.getCause());
        }
    }

    private boolean isAIProvider(String providerName) {
        if (providerName == null) {
            return false;
        }
        if (exampleProviders.containsKey(providerName)) {
            return exampleProviders.get(providerName).getType() == ProviderInfo.ProviderType.AI;
        }
        if (explanationProviders.containsKey(providerName)) {
            return explanationProviders.get(providerName).getType() == ProviderInfo.ProviderType.AI;
        }
        if (decompositionProviders.containsKey(providerName)) {
            return decompositionProviders.get(providerName).getType() == ProviderInfo.ProviderType.AI;
        }
        if (audioProviders.containsKey(providerName)) {
            return audioProviders.get(providerName).getType() == ProviderInfo.ProviderType.AI;
        }
        return providerName.contains("deepseek") || providerName.contains("gpt") || providerName.contains("qwen") || providerName.contains("glm");
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
