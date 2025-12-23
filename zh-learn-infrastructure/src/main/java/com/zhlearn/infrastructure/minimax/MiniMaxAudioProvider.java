package com.zhlearn.infrastructure.minimax;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

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
 * Audio provider using MiniMax's TTS API (speech-2.6-hd model). MiniMax is ranked #1 globally for
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

    // Selected voices for diverse Mandarin Chinese coverage
    private static final List<String> VOICES =
            List.of("Wise_Woman", "Deep_Voice_Man", "Young_Knight", "Calm_Woman");

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
        return "MiniMax Speech-2.6-HD with 4 Mandarin voices (" + String.join(", ", VOICES) + ")";
    }

    @Override
    public ProviderType getType() {
        return ProviderType.AI;
    }

    @Override
    protected List<String> getVoices() {
        return VOICES;
    }

    @Override
    protected Path synthesizeVoice(String voice, String text)
            throws IOException, InterruptedException, UnrecoverableProviderException {
        MiniMaxTtsResult result = getClient().synthesize(voice, text);
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
        return voice + " 🤖";
    }

    @Override
    protected String cacheKey(Hanzi word, Pinyin pinyin, String voice) {
        return voice + "|" + word.characters() + "|" + pinyin.pinyin();
    }
}
