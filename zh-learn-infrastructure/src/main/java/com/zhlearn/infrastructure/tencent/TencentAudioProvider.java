package com.zhlearn.infrastructure.tencent;

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
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class TencentAudioProvider implements AudioProvider {
    private static final Logger log = LoggerFactory.getLogger(TencentAudioProvider.class);

    private static final String NAME = "tencent-tts";
    private static final String SECRET_ID_ENV = "TENCENT_SECRET_ID";
    private static final String SECRET_KEY_ENV = "TENCENT_API_KEY";
    private static final String REGION_ENV = "TENCENT_REGION";
    private static final String DEFAULT_REGION = "ap-singapore";

    // Voice mapping as specified by user
    private static final Map<Integer, String> VOICES = new LinkedHashMap<>();
    static {
        VOICES.put(101052, "zhiwei");
        VOICES.put(101002, "zhiling");
    }

    private final TencentTtsClient clientOverride;
    private final ExecutorService executorService;

    public TencentAudioProvider() {
        this(null, null);
    }

    public TencentAudioProvider(TencentTtsClient clientOverride) {
        this(clientOverride, null);
    }

    public TencentAudioProvider(AudioDownloadExecutor audioExecutor) {
        this(null, audioExecutor.getExecutor());
    }

    public TencentAudioProvider(TencentTtsClient clientOverride, ExecutorService executorService) {
        this.clientOverride = clientOverride;
        this.executorService = executorService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Tencent text-to-speech (voices: " + String.join(", ", VOICES.values()) + ")";
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
        TencentTtsClient activeClient = clientOverride;
        if (activeClient == null) {
            activeClient = new TencentTtsClient(resolveSecretId(), resolveSecretKey(), resolveRegion());
        }

        List<Path> results = new ArrayList<>();
        for (Map.Entry<Integer, String> voiceEntry : VOICES.entrySet()) {
            int voiceType = voiceEntry.getKey();
            String voiceName = voiceEntry.getValue();

            Path cached = cachedPath(word, pinyin, voiceName);
            if (Files.exists(cached)) {
                results.add(cached.toAbsolutePath());
                continue;
            }

            try {
                TencentTtsResult result = activeClient.synthesize(voiceType, word.characters());
                Path audioFile = decodeAudioData(result.audioData());
                try {
                    Path normalized = AudioCache.ensureCachedNormalized(audioFile, NAME,
                        word.characters(), voiceName, cacheKey(word, pinyin, voiceName));
                    results.add(normalized);
                } finally {
                    Files.deleteIfExists(audioFile);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to process audio data for voice " + voiceName, e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Audio normalization was interrupted for voice " + voiceName, e);
            } catch (RuntimeException e) {
                throw new RuntimeException("Failed to synthesize Tencent TTS for voice " + voiceName, e);
            }
        }
        return results;
    }

    @Override
    public List<PronunciationDescription> getPronunciationsWithDescriptions(Hanzi word, Pinyin pinyin) {
        long startTime = System.currentTimeMillis();
        log.info("[Tencent] Starting TTS synthesis for '{}' with {} voices", word.characters(), VOICES.size());

        final TencentTtsClient activeClient = clientOverride != null ? clientOverride : new TencentTtsClient(resolveSecretId(), resolveSecretKey(), resolveRegion());

        try {
            List<PronunciationDescription> results;
            if (executorService != null) {
                log.debug("[Tencent] Using parallel voice synthesis for '{}'", word.characters());
                List<CompletableFuture<PronunciationDescription>> voiceFutures = VOICES.entrySet().stream()
                    .map(entry -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return downloadVoiceDescription(activeClient, entry.getKey(), entry.getValue(), word, pinyin);
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException("Failed to download voice for " + word.characters(), e);
                        }
                    }, executorService))
                    .toList();

                results = voiceFutures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
            } else {
                log.debug("[Tencent] Using sequential voice synthesis for '{}'", word.characters());
                results = getPronunciationsWithDescriptionsSequential(activeClient, word, pinyin);
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("[Tencent] Completed TTS synthesis for '{}' in {}ms - {} pronunciations",
                word.characters(), duration, results.size());
            return results;

        } catch (IOException | InterruptedException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[Tencent] Failed TTS synthesis for '{}' after {}ms: {}", word.characters(), duration, e.getMessage(), e);
            throw new RuntimeException("Failed to get audio pronunciations for " + word.characters(), e);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[Tencent] Unexpected error for '{}' after {}ms: {}", word.characters(), duration, e.getMessage(), e);
            throw e;
        }
    }

    private List<PronunciationDescription> getPronunciationsWithDescriptionsSequential(TencentTtsClient activeClient, Hanzi word, Pinyin pinyin) throws IOException, InterruptedException {
        List<PronunciationDescription> results = new ArrayList<>();
        for (Map.Entry<Integer, String> voiceEntry : VOICES.entrySet()) {
            PronunciationDescription desc = downloadVoiceDescription(activeClient, voiceEntry.getKey(), voiceEntry.getValue(), word, pinyin);
            results.add(desc);
        }
        return results;
    }

    private PronunciationDescription downloadVoiceDescription(TencentTtsClient activeClient, int voiceType, String voiceName, Hanzi word, Pinyin pinyin) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        log.debug("[Tencent] Processing voice '{}' for '{}'", voiceName, word.characters());

        Path cached = cachedPath(word, pinyin, voiceName);
        if (Files.exists(cached)) {
            long duration = System.currentTimeMillis() - startTime;
            log.debug("[Tencent] Using cached audio for '{}' voice '{}' ({}ms)", word.characters(), voiceName, duration);
            String description = formatTencentDescription(voiceName);
            return new PronunciationDescription(cached.toAbsolutePath(), description);
        }

        log.debug("[Tencent] Synthesizing voice '{}' for '{}'", voiceName, word.characters());
        TencentTtsResult result = activeClient.synthesize(voiceType, word.characters());

        log.debug("[Tencent] Decoding audio data for '{}' voice '{}'", word.characters(), voiceName);
        Path audioFile = decodeAudioData(result.audioData());

        try {
            log.debug("[Tencent] Normalizing audio for '{}' voice '{}'", word.characters(), voiceName);
            Path normalized = AudioCache.ensureCachedNormalized(audioFile, NAME,
                word.characters(), voiceName, cacheKey(word, pinyin, voiceName));

            long duration = System.currentTimeMillis() - startTime;
            log.debug("[Tencent] Completed voice '{}' for '{}' in {}ms", voiceName, word.characters(), duration);

            String description = formatTencentDescription(voiceName);
            return new PronunciationDescription(normalized, description);
        } finally {
            try {
                Files.deleteIfExists(audioFile);
            } catch (IOException e) {
                log.warn("[Tencent] Failed to delete temp file for '{}' voice '{}': {}", word.characters(), voiceName, e.getMessage());
                throw new RuntimeException("Failed to delete temporary file", e);
            }
        }
    }

    private static String formatTencentDescription(String voice) {
        return voice + " 🤖";
    }

    private Path decodeAudioData(String base64Audio) throws IOException {
        byte[] audioBytes = Base64.getDecoder().decode(base64Audio);
        Path tmp = Files.createTempFile(NAME + "-", ".mp3");
        Files.write(tmp, audioBytes);
        return tmp;
    }

    private String resolveSecretId() {
        String secretId = System.getenv(SECRET_ID_ENV);
        if (secretId == null || secretId.isBlank()) {
            throw new IllegalStateException("TENCENT_SECRET_ID is required for Tencent TTS provider");
        }
        return secretId;
    }

    private String resolveSecretKey() {
        String secretKey = System.getenv(SECRET_KEY_ENV);
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("TENCENT_API_KEY is required for Tencent TTS provider");
        }
        return secretKey;
    }

    private String resolveRegion() {
        String region = System.getenv(REGION_ENV);
        if (region == null || region.isBlank()) {
            return DEFAULT_REGION;
        }
        return region;
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