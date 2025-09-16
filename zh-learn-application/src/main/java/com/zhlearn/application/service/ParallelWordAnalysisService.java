package com.zhlearn.application.service;

import com.zhlearn.domain.model.*;
import com.zhlearn.domain.service.WordAnalysisService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Parallel implementation of WordAnalysisService that executes AI provider calls concurrently
 * for better performance. Non-AI providers (like pleco-export, pinyin4j) are called synchronously.
 */
public class ParallelWordAnalysisService implements WordAnalysisService {

    private final WordAnalysisServiceImpl delegate;
    private final ExecutorService executorService;

    public ParallelWordAnalysisService(ProviderRegistry registry, int threadPoolSize) {
        this.delegate = new WordAnalysisServiceImpl(registry);
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    public ParallelWordAnalysisService(ProviderRegistry registry) {
        this(registry, 10); // Default to 10 threads
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
    public java.util.Optional<String> getPronunciation(Hanzi word, Pinyin pinyin, String providerName) {
        return delegate.getPronunciation(word, pinyin, providerName);
    }

    @Override
    public WordAnalysis getCompleteAnalysis(Hanzi word, String providerName) {
        return delegate.getCompleteAnalysis(word, providerName);
    }

    @Override
    public WordAnalysis getCompleteAnalysis(Hanzi word, ProviderConfiguration config) {
        // Call fast, non-AI providers synchronously first
        Definition definition = getDefinition(word, config.getDefinitionProvider());
        Pinyin pinyin = getPinyin(word, config.getPinyinProvider());

        // Determine which providers are AI-based and should be called in parallel
        boolean isDecompositionAI = isAIProvider(config.getDecompositionProvider());
        boolean isExampleAI = isAIProvider(config.getExampleProvider());
        boolean isExplanationAI = isAIProvider(config.getExplanationProvider());
        boolean isAudioAI = isAIProvider(config.getAudioProvider());

        // If no AI providers, call everything synchronously
        if (!isDecompositionAI && !isExampleAI && !isExplanationAI && !isAudioAI) {
            return delegate.getCompleteAnalysis(word, config);
        }

        // Call AI providers in parallel
        CompletableFuture<StructuralDecomposition> decompositionFuture;
        CompletableFuture<Example> examplesFuture;
        CompletableFuture<Explanation> explanationFuture;
        CompletableFuture<java.util.Optional<String>> pronunciationFuture;

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

        // Wait for all AI providers to complete
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
        } catch (java.util.concurrent.ExecutionException e) {
            throw new RuntimeException("Error in parallel word analysis: " + e.getCause().getMessage(), e.getCause());
        }
    }

    /**
     * Determine if a provider is AI-based and should be called in parallel.
     * AI providers typically have longer response times and benefit from parallelism.
     */
    private boolean isAIProvider(String providerName) {
        return providerName.contains("deepseek") || 
               providerName.contains("gpt") || 
               providerName.contains("openai");
    }

    @Override
    public void addPinyinProvider(String name, com.zhlearn.domain.provider.PinyinProvider provider) {
        delegate.addPinyinProvider(name, provider);
    }

    @Override
    public void addDefinitionProvider(String name, com.zhlearn.domain.provider.DefinitionProvider provider) {
        delegate.addDefinitionProvider(name, provider);
    }

    @Override
    public void addStructuralDecompositionProvider(String name, com.zhlearn.domain.provider.StructuralDecompositionProvider provider) {
        delegate.addStructuralDecompositionProvider(name, provider);
    }

    @Override
    public void addExplanationProvider(String name, com.zhlearn.domain.provider.ExplanationProvider provider) {
        delegate.addExplanationProvider(name, provider);
    }

    @Override
    public void addAudioProvider(String name, com.zhlearn.domain.provider.AudioProvider provider) {
        delegate.addAudioProvider(name, provider);
    }

    /**
     * Shutdown the executor service when done.
     * Should be called when the service is no longer needed.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
