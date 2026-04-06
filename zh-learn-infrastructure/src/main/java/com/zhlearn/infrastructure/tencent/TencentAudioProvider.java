package com.zhlearn.infrastructure.tencent;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.infrastructure.audio.AbstractTtsAudioProvider;
import com.zhlearn.infrastructure.audio.AudioCache;
import com.zhlearn.infrastructure.audio.AudioPaths;
import com.zhlearn.infrastructure.ratelimit.ProviderRateLimiter;

public class TencentAudioProvider extends AbstractTtsAudioProvider {
    private static final String NAME = "tencent-tts";

    // Premium voices at 16kHz — best available on international endpoint (ap-singapore)
    private static final Map<Integer, String> VOICES = new LinkedHashMap<>();

    static {
        VOICES.put(101052, "zhiwei");
        VOICES.put(101002, "zhiling");
        VOICES.put(101004, "zhiyun");
        VOICES.put(101010, "zhihua");
    }

    private TencentTtsClient client;
    private final HttpClient httpClient;
    private final TencentTtsClient injectedClient;
    private final Map<String, Integer> voiceNameToType;
    private final ProviderRateLimiter rateLimiter;

    public TencentAudioProvider(
            AudioCache audioCache,
            AudioPaths audioPaths,
            ExecutorService executorService,
            HttpClient httpClient,
            TencentTtsClient client,
            ProviderRateLimiter rateLimiter) {
        super(audioCache, audioPaths, executorService);
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.injectedClient = client;
        this.rateLimiter = rateLimiter;
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
        return "Tencent Premium text-to-speech (voices: "
                + String.join(", ", VOICES.values())
                + ")";
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
        try {
            TencentTtsResult result = getClient().synthesize(voiceType, text);
            return decodeAudioData(result.audioData());
        } catch (TencentTtsClientException e) {
            throw new RuntimeException("Tencent TTS synthesis failed for voice " + voice, e);
        }
    }

    private TencentTtsClient getClient() {
        if (client == null) {
            if (injectedClient != null) {
                client = injectedClient;
            } else {
                client =
                        new TencentTtsClient(
                                httpClient,
                                TencentConfig.getSecretId(),
                                TencentConfig.getSecretKey(),
                                TencentConfig.getRegion(),
                                TencentConfig.getEndpoint(),
                                null, // use default endpoint URL
                                rateLimiter);
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
}
