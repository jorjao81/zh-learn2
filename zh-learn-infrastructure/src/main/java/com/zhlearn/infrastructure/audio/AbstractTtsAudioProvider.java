package com.zhlearn.infrastructure.audio;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.provider.AudioProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Abstract base class for TTS (text-to-speech) audio providers.
 * Consolidates common logic for caching, parallel processing, and audio normalization.
 * Subclasses implement provider-specific synthesis logic.
 */
public abstract class AbstractTtsAudioProvider implements AudioProvider {
    private static final Logger log = LoggerFactory.getLogger(AbstractTtsAudioProvider.class);

    protected final AudioCache audioCache;
    protected final AudioPaths audioPaths;
    protected final ExecutorService executorService;

    protected AbstractTtsAudioProvider(AudioCache audioCache, AudioPaths audioPaths, ExecutorService executorService) {
        this.audioCache = Objects.requireNonNull(audioCache, "audioCache");
        this.audioPaths = Objects.requireNonNull(audioPaths, "audioPaths");
        this.executorService = executorService;
    }

    /**
     * Return the list of voice names this provider supports.
     */
    protected abstract List<String> getVoices();

    /**
     * Synthesize audio for a specific voice and text.
     * Returns a Path to a temporary audio file that will be normalized and cleaned up automatically.
     *
     * @param voice the voice name
     * @param text the text to synthesize
     * @return Path to temporary audio file
     * @throws IOException if synthesis fails
     * @throws InterruptedException if interrupted
     */
    protected abstract Path synthesizeVoice(String voice, String text) throws IOException, InterruptedException;

    /**
     * Format a human-readable description for a voice.
     */
    protected abstract String formatDescription(String voice);

    /**
     * Generate a cache key for this word/pinyin/voice combination.
     */
    protected abstract String cacheKey(Hanzi word, Pinyin pinyin, String voice);

    /**
     * Check if an exception should be logged as a warning and skipped (e.g., content moderation).
     * Default: false (all exceptions are propagated).
     */
    protected boolean isSkippableException(Exception e) {
        return false;
    }

    @Override
    public Optional<Path> getPronunciation(Hanzi word, Pinyin pinyin) {
        List<Path> pronunciations = getPronunciations(word, pinyin);
        if (pronunciations.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(pronunciations.get(0));
    }

    @Override
    public List<Path> getPronunciations(Hanzi word, Pinyin pinyin) {
        List<Path> results = new ArrayList<>();
        for (String voice : getVoices()) {
            Path cached = cachedPath(word, pinyin, voice);
            if (Files.exists(cached)) {
                results.add(cached.toAbsolutePath());
                continue;
            }
            try {
                Path tempFile = synthesizeVoice(voice, word.characters());
                try {
                    Path normalized = audioCache.ensureCachedNormalized(tempFile, getName(),
                        word.characters(), voice, cacheKey(word, pinyin, voice));
                    results.add(normalized);
                } finally {
                    Files.deleteIfExists(tempFile);
                }
            } catch (Exception e) {
                if (isSkippableException(e)) {
                    log.warn("[{}] Skipping voice '{}' for '{}': {}",
                        getName(), voice, word.characters(), e.getMessage());
                    continue;
                }
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                throw new RuntimeException("Failed to synthesize " + getName() + " for voice " + voice, e);
            }
        }
        return results;
    }

    @Override
    public List<PronunciationDescription> getPronunciationsWithDescriptions(Hanzi word, Pinyin pinyin) {
        long startTime = System.currentTimeMillis();
        log.info("[{}] Starting TTS synthesis for '{}' with {} voices", getName(), word.characters(), getVoices().size());

        try {
            List<PronunciationDescription> results;
            if (executorService != null) {
                log.debug("[{}] Using parallel voice synthesis for '{}'", getName(), word.characters());
                results = synthesizeParallel(word, pinyin);
            } else {
                log.debug("[{}] Using sequential voice synthesis for '{}'", getName(), word.characters());
                results = synthesizeSequential(word, pinyin);
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("[{}] Completed TTS synthesis for '{}' in {}ms - {} pronunciations",
                getName(), word.characters(), duration, results.size());
            return results;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[{}] Failed TTS synthesis for '{}' after {}ms: {}", getName(), word.characters(), duration, e.getMessage(), e);
            throw new RuntimeException("Failed to get audio pronunciations for " + word.characters(), e);
        }
    }

    private List<PronunciationDescription> synthesizeParallel(Hanzi word, Pinyin pinyin) {
        List<CompletableFuture<PronunciationDescription>> voiceFutures = getVoices().stream()
            .map(voice -> CompletableFuture.supplyAsync(() -> {
                try {
                    return downloadVoiceDescription(voice, word, pinyin);
                } catch (Exception e) {
                    if (isSkippableException(e)) {
                        log.warn("[{}] Skipping voice '{}' for '{}': {}",
                            getName(), voice, word.characters(), e.getMessage());
                        return null;
                    }
                    throw new RuntimeException("Failed to download voice " + voice + " for " + word.characters(), e);
                }
            })) // Use ForkJoinPool.commonPool() to avoid deadlock
            .toList();

        return voiceFutures.stream()
            .map(CompletableFuture::join)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private List<PronunciationDescription> synthesizeSequential(Hanzi word, Pinyin pinyin) {
        List<PronunciationDescription> results = new ArrayList<>();
        for (String voice : getVoices()) {
            try {
                PronunciationDescription desc = downloadVoiceDescription(voice, word, pinyin);
                results.add(desc);
            } catch (Exception e) {
                if (isSkippableException(e)) {
                    log.warn("[{}] Skipping voice '{}' for '{}': {}",
                        getName(), voice, word.characters(), e.getMessage());
                    continue;
                }
                throw new RuntimeException("Failed to synthesize voice " + voice, e);
            }
        }
        return results;
    }

    private PronunciationDescription downloadVoiceDescription(String voice, Hanzi word, Pinyin pinyin) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        log.debug("[{}] Processing voice '{}' for '{}'", getName(), voice, word.characters());

        Path cached = cachedPath(word, pinyin, voice);
        if (Files.exists(cached)) {
            long duration = System.currentTimeMillis() - startTime;
            log.debug("[{}] Using cached audio for '{}' voice '{}' ({}ms)", getName(), word.characters(), voice, duration);
            String description = formatDescription(voice);
            return new PronunciationDescription(cached.toAbsolutePath(), description);
        }

        log.debug("[{}] Synthesizing voice '{}' for '{}'", getName(), voice, word.characters());
        Path tempFile = synthesizeVoice(voice, word.characters());

        try {
            log.debug("[{}] Normalizing audio for '{}' voice '{}'", getName(), word.characters(), voice);
            Path normalized = audioCache.ensureCachedNormalized(tempFile, getName(),
                word.characters(), voice, cacheKey(word, pinyin, voice));

            long duration = System.currentTimeMillis() - startTime;
            log.debug("[{}] Completed voice '{}' for '{}' in {}ms", getName(), voice, word.characters(), duration);

            String description = formatDescription(voice);
            return new PronunciationDescription(normalized, description);
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                log.warn("[{}] Failed to delete temp file for '{}' voice '{}': {}", getName(), word.characters(), voice, e.getMessage());
                throw new RuntimeException("Failed to delete temporary file", e);
            }
        }
    }

    protected Path cachedPath(Hanzi word, Pinyin pinyin, String voice) {
        String sourceId = cacheKey(word, pinyin, voice);
        String base = audioPaths.sanitize(getName()) + "_" +
            audioPaths.sanitize(word.characters()) + "_" +
            audioPaths.sanitize(voice);
        String hash = shortHash(sourceId.getBytes(StandardCharsets.UTF_8));
        String fileName = base + "_" + hash + ".mp3";
        return audioPaths.audioDir().resolve(getName()).resolve(fileName);
    }

    protected static String shortHash(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(bytes);
            return HexFormat.of().withUpperCase().formatHex(digest).substring(0, 10);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 digest unavailable", e);
        }
    }
}
