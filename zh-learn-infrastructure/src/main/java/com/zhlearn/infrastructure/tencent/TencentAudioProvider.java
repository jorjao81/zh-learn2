package com.zhlearn.infrastructure.tencent;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.infrastructure.audio.AudioCache;
import com.zhlearn.infrastructure.audio.AudioPaths;

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

public class TencentAudioProvider implements AudioProvider {
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

    public TencentAudioProvider() {
        this(null);
    }

    public TencentAudioProvider(TencentTtsClient clientOverride) {
        this.clientOverride = clientOverride;
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
        TencentTtsClient activeClient = clientOverride;
        if (activeClient == null) {
            activeClient = new TencentTtsClient(resolveSecretId(), resolveSecretKey(), resolveRegion());
        }

        List<PronunciationDescription> results = new ArrayList<>();
        for (Map.Entry<Integer, String> voiceEntry : VOICES.entrySet()) {
            int voiceType = voiceEntry.getKey();
            String voiceName = voiceEntry.getValue();

            Path cached = cachedPath(word, pinyin, voiceName);
            if (Files.exists(cached)) {
                String description = formatTencentDescription(voiceName);
                results.add(new PronunciationDescription(cached.toAbsolutePath(), description));
                continue;
            }

            try {
                TencentTtsResult result = activeClient.synthesize(voiceType, word.characters());
                Path audioFile = decodeAudioData(result.audioData());
                try {
                    Path normalized = AudioCache.ensureCachedNormalized(audioFile, NAME,
                        word.characters(), voiceName, cacheKey(word, pinyin, voiceName));
                    String description = formatTencentDescription(voiceName);
                    results.add(new PronunciationDescription(normalized, description));
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