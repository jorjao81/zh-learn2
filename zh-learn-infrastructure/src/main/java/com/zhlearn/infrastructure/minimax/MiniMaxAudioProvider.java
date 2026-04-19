package com.zhlearn.infrastructure.minimax;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhlearn.domain.exception.UnrecoverableProviderException;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.infrastructure.audio.AbstractTtsAudioProvider;
import com.zhlearn.infrastructure.audio.AudioCache;
import com.zhlearn.infrastructure.audio.AudioPaths;
import com.zhlearn.infrastructure.ratelimit.ProviderRateLimiter;

/**
 * Audio provider using MiniMax's TTS API (speech-2.8-hd model). MiniMax is ranked #1 globally for
 * TTS quality, with particularly excellent Chinese/Mandarin support.
 *
 * <p>Requires environment variables:
 *
 * <ul>
 *   <li>{@code MINIMAX_API_KEY} - API key from MiniMax platform
 *   <li>{@code MINIMAX_GROUP_ID} - Group ID from MiniMax account
 * </ul>
 */
public class MiniMaxAudioProvider extends AbstractTtsAudioProvider {
    private static final String NAME = "minimax-tts";

    private record VoiceVariant(String key, String voiceId, String emotion, double speed) {
        /** Build a variant whose key equals the voice_id (preserves cache for baseline voices). */
        static VoiceVariant baseline(String voiceId) {
            return new VoiceVariant(voiceId, voiceId, "neutral", 1.0);
        }

        /** Build a variant with a distinct key encoding emotion + speed. */
        static VoiceVariant tuned(String voiceId, String emotion, double speed) {
            return new VoiceVariant(voiceId + "|" + emotion + "|" + speed, voiceId, emotion, speed);
        }
    }

    // All voice_ids and speed/emotion combinations below were verified live against
    // speech-2.8-hd (HTTP 200 with audio payload) before being committed.
    private static final List<VoiceVariant> VARIANTS =
            List.of(
                    VoiceVariant.baseline("Chinese (Mandarin)_Male_Announcer"),
                    VoiceVariant.baseline("Chinese (Mandarin)_News_Anchor"),
                    VoiceVariant.baseline("Chinese (Mandarin)_Gentle_Senior"),
                    VoiceVariant.tuned("Chinese (Mandarin)_Sincere_Adult", "calm", 1.0),
                    VoiceVariant.baseline("Chinese (Mandarin)_Reliable_Executive"));

    private static final Map<String, VoiceVariant> BY_KEY =
            VARIANTS.stream()
                    .collect(
                            Collectors.toMap(
                                    VoiceVariant::key, v -> v, (a, b) -> a, LinkedHashMap::new));

    private static final List<String> VOICE_KEYS =
            VARIANTS.stream().map(VoiceVariant::key).toList();

    private static final List<String> BASE_VOICE_IDS =
            VARIANTS.stream()
                    .map(VoiceVariant::voiceId)
                    .distinct()
                    .collect(Collectors.toUnmodifiableList());

    private MiniMaxTtsClient client;
    private final HttpClient httpClient;
    private final MiniMaxTtsClient injectedClient;
    private final ProviderRateLimiter rateLimiter;

    public MiniMaxAudioProvider(
            AudioCache audioCache,
            AudioPaths audioPaths,
            ExecutorService executorService,
            HttpClient httpClient,
            MiniMaxTtsClient client,
            ProviderRateLimiter rateLimiter) {
        super(audioCache, audioPaths, executorService);
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.injectedClient = client;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "MiniMax Speech-2.8-HD with "
                + VARIANTS.size()
                + " voice variants across "
                + BASE_VOICE_IDS.size()
                + " base voices ("
                + String.join(", ", BASE_VOICE_IDS)
                + ")";
    }

    @Override
    public ProviderType getType() {
        return ProviderType.AI;
    }

    @Override
    protected List<String> getVoices() {
        return VOICE_KEYS;
    }

    @Override
    protected Path synthesizeVoice(String voice, String text)
            throws IOException, InterruptedException, UnrecoverableProviderException {
        VoiceVariant variant = BY_KEY.get(voice);
        if (variant == null) {
            throw new IllegalStateException("Unknown MiniMax voice variant: " + voice);
        }
        MiniMaxTtsResult result =
                getClient().synthesize(variant.voiceId(), text, variant.emotion(), variant.speed());
        Path tmp = Files.createTempFile(NAME + "-", ".mp3");
        Files.write(tmp, result.audioData());
        return tmp;
    }

    private MiniMaxTtsClient getClient() {
        if (client == null) {
            if (injectedClient != null) {
                client = injectedClient;
            } else {
                client =
                        new MiniMaxTtsClient(
                                httpClient,
                                MiniMaxConfig.getApiKey(),
                                MiniMaxConfig.getGroupId(),
                                MiniMaxConfig.getBaseUrl(),
                                MiniMaxConfig.getModel(),
                                new ObjectMapper(),
                                null, // use default retry
                                rateLimiter);
            }
        }
        return client;
    }

    @Override
    protected String formatDescription(String voice) {
        VoiceVariant variant = BY_KEY.get(voice);
        if (variant == null) {
            return voice + " 🤖";
        }
        if (variant.key().equals(variant.voiceId())) {
            return variant.voiceId() + " 🤖";
        }
        return variant.voiceId() + " · " + variant.emotion() + " @" + variant.speed() + "× 🤖";
    }

    @Override
    protected String cacheKey(Hanzi word, Pinyin pinyin, String voice) {
        return voice + "|" + word.characters() + "|" + pinyin.pinyin();
    }
}
