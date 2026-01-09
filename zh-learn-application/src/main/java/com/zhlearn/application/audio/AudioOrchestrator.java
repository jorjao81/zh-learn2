package com.zhlearn.application.audio;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.provider.AudioProvider;

public class AudioOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(AudioOrchestrator.class);

    private final List<AudioProvider> audioProviders;
    private final ExecutorService executorService;

    public AudioOrchestrator(List<AudioProvider> audioProviders, ExecutorService executorService) {
        this.audioProviders = audioProviders;
        this.executorService = executorService;
    }

    public List<PronunciationCandidate> candidatesFor(Hanzi word, Pinyin pinyin) {
        log.info(
                "[Audio] Starting audio candidate generation for '{}' ({}) with {} providers",
                word.characters(),
                pinyin.pinyin(),
                audioProviders.size());

        List<CompletableFuture<List<PronunciationCandidate>>> futures =
                audioProviders.stream()
                        .map(
                                provider ->
                                        CompletableFuture.supplyAsync(
                                                () ->
                                                        getCandidatesFromProvider(
                                                                provider, word, pinyin),
                                                executorService))
                        .toList();

        List<PronunciationCandidate> allCandidates =
                futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

        log.info(
                "[Audio] Completed audio candidate generation for '{}' - total {} candidates from {} providers",
                word.characters(),
                allCandidates.size(),
                audioProviders.size());

        return allCandidates;
    }

    private List<PronunciationCandidate> getCandidatesFromProvider(
            AudioProvider provider, Hanzi word, Pinyin pinyin) {
        long startTime = System.currentTimeMillis();
        log.info("[Audio] Starting provider '{}' for '{}'", provider.getName(), word.characters());

        List<PronunciationCandidate> candidates =
                provider.getPronunciationsWithDescriptions(word, pinyin).stream()
                        .map(
                                desc ->
                                        new PronunciationCandidate(
                                                provider.getName(),
                                                validateAbsolutePath(
                                                        provider.getName(), desc.path()),
                                                desc.description()))
                        .filter(candidate -> candidate.file() != null)
                        .collect(Collectors.toList());

        long duration = System.currentTimeMillis() - startTime;
        log.info(
                "[Audio] Provider '{}' completed for '{}' in {}ms - {} candidates",
                provider.getName(),
                word.characters(),
                duration,
                candidates.size());

        return candidates;
    }

    static Path validateAbsolutePath(String providerName, Path provided) {
        if (provided == null) {
            throw new IllegalArgumentException("Audio provider returned null path");
        }

        if (!provided.isAbsolute()) {
            throw new IllegalStateException(
                    "Audio provider '"
                            + providerName
                            + "' returned non-absolute path: "
                            + provided);
        }
        return provided.toAbsolutePath();
    }
}
