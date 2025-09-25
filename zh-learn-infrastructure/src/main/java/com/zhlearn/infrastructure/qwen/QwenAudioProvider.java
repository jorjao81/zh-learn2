package com.zhlearn.infrastructure.qwen;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.infrastructure.audio.AudioCache;
import com.zhlearn.infrastructure.audio.AudioDownloadExecutor;
import com.zhlearn.infrastructure.audio.AudioPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class QwenAudioProvider implements AudioProvider {
    private static final Logger log = LoggerFactory.getLogger(QwenAudioProvider.class);

    private static final String NAME = "qwen-tts";
    private static final String MODEL = "qwen3-tts-flash";
    private static final List<String> VOICES = List.of("Cherry", "Ethan", "Nofish", "Jennifer", "Ryan", "Katerina", "Elias");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final String API_KEY_ENV = "DASHSCOPE_API_KEY";
    private static final String USER_AGENT = "zh-learn-cli/1.0 (QwenAudioProvider)";

    private final QwenTtsClient clientOverride;
    private final HttpClient httpClient;
    private final ExecutorService executorService;

    public QwenAudioProvider() {
        this(null, HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
    }

    public QwenAudioProvider(QwenTtsClient clientOverride, HttpClient httpClient) {
        this(clientOverride, httpClient, null);
    }

    public QwenAudioProvider(AudioDownloadExecutor audioExecutor) {
        this(null, HttpClient.newBuilder().connectTimeout(TIMEOUT).build(), audioExecutor.getExecutor());
    }

    public QwenAudioProvider(QwenTtsClient clientOverride, HttpClient httpClient, ExecutorService executorService) {
        this.clientOverride = clientOverride;
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.executorService = executorService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Qwen3-TTS-Flash with 7 standard Mandarin voices (" + String.join(", ", VOICES) + ")";
    }

    @Override
    public ProviderType getType() {
        return ProviderType.AI;
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
        QwenTtsClient activeClient = clientOverride;
        if (activeClient == null) {
            activeClient = new QwenTtsClient(httpClient, resolveApiKey(), MODEL);
        }
        List<Path> results = new ArrayList<>();
        for (String voice : VOICES) {
            Path cached = cachedPath(word, pinyin, voice);
            if (Files.exists(cached)) {
                results.add(cached.toAbsolutePath());
                continue;
            }
            try {
                QwenTtsResult result = activeClient.synthesize(voice, word.characters());
                Path downloaded = download(result.audioUrl());
                try {
                    Path normalized = AudioCache.ensureCachedNormalized(downloaded, NAME,
                        word.characters(), voice, cacheKey(word, pinyin, voice));
                    results.add(normalized);
                } finally {
                    Files.deleteIfExists(downloaded);
                }
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                throw new RuntimeException("Failed to synthesize Qwen TTS for voice " + voice, e);
            }
        }
        return results;
    }

    @Override
    public List<PronunciationDescription> getPronunciationsWithDescriptions(Hanzi word, Pinyin pinyin) {
        long startTime = System.currentTimeMillis();
        log.info("[Qwen] Starting TTS synthesis for '{}' with {} voices", word.characters(), VOICES.size());

        final QwenTtsClient activeClient = clientOverride != null ? clientOverride : new QwenTtsClient(httpClient, resolveApiKey(), MODEL);

        try {
            List<PronunciationDescription> results;
            if (executorService != null) {
                log.debug("[Qwen] Using parallel voice synthesis for '{}'", word.characters());
                List<CompletableFuture<PronunciationDescription>> voiceFutures = VOICES.stream()
                    .map(voice -> CompletableFuture.<PronunciationDescription>supplyAsync(() -> {
                        try {
                            return downloadVoiceDescription(activeClient, voice, word, pinyin);
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException("Failed to download voice " + voice + " for " + word.characters(), e);
                        }
                    })) // Use ForkJoinPool.commonPool() to avoid deadlock
                    .toList();

                results = voiceFutures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
            } else {
                log.debug("[Qwen] Using sequential voice synthesis for '{}'", word.characters());
                results = getPronunciationsWithDescriptionsSequential(activeClient, word, pinyin);
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("[Qwen] Completed TTS synthesis for '{}' in {}ms - {} pronunciations",
                word.characters(), duration, results.size());
            return results;

        } catch (IOException | InterruptedException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[Qwen] Failed TTS synthesis for '{}' after {}ms: {}", word.characters(), duration, e.getMessage(), e);
            throw new RuntimeException("Failed to get audio pronunciations for " + word.characters(), e);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[Qwen] Unexpected error for '{}' after {}ms: {}", word.characters(), duration, e.getMessage(), e);
            throw e;
        }
    }

    private List<PronunciationDescription> getPronunciationsWithDescriptionsSequential(QwenTtsClient activeClient, Hanzi word, Pinyin pinyin) throws IOException, InterruptedException {
        List<PronunciationDescription> results = new ArrayList<>();
        for (String voice : VOICES) {
            PronunciationDescription desc = downloadVoiceDescription(activeClient, voice, word, pinyin);
            results.add(desc);
        }
        return results;
    }

    private PronunciationDescription downloadVoiceDescription(QwenTtsClient activeClient, String voice, Hanzi word, Pinyin pinyin) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        log.debug("[Qwen] Processing voice '{}' for '{}'", voice, word.characters());

        Path cached = cachedPath(word, pinyin, voice);
        if (Files.exists(cached)) {
            long duration = System.currentTimeMillis() - startTime;
            log.debug("[Qwen] Using cached audio for '{}' voice '{}' ({}ms)", word.characters(), voice, duration);
            String description = formatQwenDescription(voice);
            return new PronunciationDescription(cached.toAbsolutePath(), description);
        }

        log.debug("[Qwen] Synthesizing voice '{}' for '{}'", voice, word.characters());
        QwenTtsResult result = activeClient.synthesize(voice, word.characters());

        log.debug("[Qwen] Downloading audio for '{}' voice '{}'", word.characters(), voice);
        Path downloaded = download(result.audioUrl());

        try {
            log.debug("[Qwen] Normalizing audio for '{}' voice '{}'", word.characters(), voice);
            Path normalized = AudioCache.ensureCachedNormalized(downloaded, NAME,
                word.characters(), voice, cacheKey(word, pinyin, voice));

            long duration = System.currentTimeMillis() - startTime;
            log.debug("[Qwen] Completed voice '{}' for '{}' in {}ms", voice, word.characters(), duration);

            String description = formatQwenDescription(voice);
            return new PronunciationDescription(normalized, description);
        } finally {
            try {
                Files.deleteIfExists(downloaded);
            } catch (IOException e) {
                log.warn("[Qwen] Failed to delete temp file for '{}' voice '{}': {}", word.characters(), voice, e.getMessage());
                throw new RuntimeException("Failed to delete temporary file", e);
            }
        }
    }

    private static String formatQwenDescription(String voice) {
        return voice + " 🤖";
    }

    private Path download(URI audioUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(audioUrl)
            .timeout(TIMEOUT)
            .header("User-Agent", USER_AGENT)
            .GET()
            .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Failed to download audio: HTTP " + response.statusCode());
        }
        Path tmp = Files.createTempFile(NAME + "-", ".mp3");
        Files.write(tmp, response.body());
        return tmp;
    }

    private String resolveApiKey() {
        String key = System.getenv(API_KEY_ENV);
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("DASHSCOPE_API_KEY is required for Qwen TTS provider");
        }
        return key;
    }

    private static String cacheKey(Hanzi word, Pinyin pinyin, String voice) {
        return voice + "|" + word.characters() + "|" + pinyin.pinyin();
    }

    private static Path cachedPath(Hanzi word, Pinyin pinyin, String voice) {
        String sourceId = cacheKey(word, pinyin, voice);
        String base = AudioPaths.sanitize(NAME) + "_" +
            AudioPaths.sanitize(word.characters()) + "_" +
            AudioPaths.sanitize(voice);
        String hash = shortHash(sourceId.getBytes(StandardCharsets.UTF_8));
        String fileName = base + "_" + hash + ".mp3";
        return AudioPaths.audioDir().resolve(NAME).resolve(fileName);
    }

    private static String shortHash(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(bytes);
            return HexFormat.of().withUpperCase().formatHex(digest).substring(0, 10);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 digest unavailable", e);
        }
    }
}
