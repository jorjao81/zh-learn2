package com.zhlearn.application.service;

import com.zhlearn.domain.model.*;
import com.zhlearn.domain.provider.*;
import com.zhlearn.domain.service.WordAnalysisService;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Parallel implementation of WordAnalysisService that executes AI provider calls concurrently
 * for better performance. Non-AI providers (like pleco-export, pinyin4j) are called synchronously.
 */
public class ParallelWordAnalysisService implements WordAnalysisService {

    private static final int DEFAULT_THREAD_POOL_SIZE = 10;

    private final WordAnalysisService delegate;
    private final ExecutorService executorService;

    public ParallelWordAnalysisService(WordAnalysisService delegate, int threadPoolSize) {
        this.delegate = delegate;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    public ParallelWordAnalysisService(WordAnalysisService delegate) {
        this(delegate, DEFAULT_THREAD_POOL_SIZE);
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
        // Call pinyin and definition providers synchronously first as they're needed by other providers
        Definition definition = getDefinition(word, config.getDefinitionProvider());
        Pinyin pinyin = getPinyin(word, config.getPinyinProvider());

        // Run remaining providers in parallel - let fast providers complete quickly
        CompletableFuture<StructuralDecomposition> decompositionFuture = CompletableFuture.supplyAsync(() ->
            getStructuralDecomposition(word, config.getDecompositionProvider()), executorService);

        CompletableFuture<Example> examplesFuture = CompletableFuture.supplyAsync(() ->
            getExamples(word, config.getExampleProvider(), definition.meaning()), executorService);

        CompletableFuture<Explanation> explanationFuture = CompletableFuture.supplyAsync(() ->
            getExplanation(word, config.getExplanationProvider()), executorService);

        CompletableFuture<Optional<Path>> pronunciationFuture = CompletableFuture.supplyAsync(() ->
            getPronunciation(word, pinyin, config.getAudioProvider()), executorService);

        // Wait for all providers to complete
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

    /**
     * Shutdown the executor service when done.
     * Should be called when the service is no longer needed.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
