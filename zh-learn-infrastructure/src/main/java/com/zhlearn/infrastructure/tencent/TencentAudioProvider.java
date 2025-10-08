package com.zhlearn.infrastructure.tencent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.infrastructure.audio.AbstractTtsAudioProvider;
import com.zhlearn.infrastructure.audio.AudioCache;
import com.zhlearn.infrastructure.audio.AudioPaths;

public class TencentAudioProvider extends AbstractTtsAudioProvider {
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

    private TencentTtsClient client;
    private final TencentTtsClient injectedClient;
    private final Map<String, Integer> voiceNameToType;

    public TencentAudioProvider(
            AudioCache audioCache,
            AudioPaths audioPaths,
            ExecutorService executorService,
            TencentTtsClient client) {
        super(audioCache, audioPaths, executorService);
        this.injectedClient = client;
        this.voiceNameToType = buildVoiceNameToTypeMap();
    }

    private Map<String, Integer> buildVoiceNameToTypeMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Map.Entry<Integer, String> entry : VOICES.entrySet()) {
            map.put(entry.getValue(), entry.getKey());
        }
        return map;
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
    protected List<String> getVoices() {
        return List.copyOf(VOICES.values());
    }

    @Override
    protected Path synthesizeVoice(String voice, String text)
            throws IOException, InterruptedException {
        Integer voiceType = voiceNameToType.get(voice);
        if (voiceType == null) {
            throw new IllegalArgumentException("Unknown voice: " + voice);
        }
        TencentTtsResult result = getClient().synthesize(voiceType, text);
        return decodeAudioData(result.audioData());
    }

    private TencentTtsClient getClient() {
        if (client == null) {
            if (injectedClient != null) {
                client = injectedClient;
            } else {
                client =
                        new TencentTtsClient(
                                resolveSecretId(), resolveSecretKey(), resolveRegion());
            }
        }
        return client;
    }

    @Override
    protected String formatDescription(String voice) {
        return voice + " 🤖";
    }

    @Override
    protected String cacheKey(Hanzi word, Pinyin pinyin, String voice) {
        return voice + "|" + word.characters() + "|" + pinyin.pinyin();
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
            throw new IllegalStateException(
                    "TENCENT_SECRET_ID is required for Tencent TTS provider");
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
}
