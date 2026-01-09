package com.zhlearn.infrastructure.qwen;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
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

public class QwenAudioProvider extends AbstractTtsAudioProvider {
    private static final String NAME = "qwen-tts";
    private static final String MODEL = "qwen3-tts-flash-2025-11-27";
    private static final List<String> VOICES =
            List.of("Cherry", "Ethan", "Nofish", "Jennifer", "Elias");
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final String USER_AGENT = "zh-learn-cli/1.0 (QwenAudioProvider)";

    private QwenTtsClient client;
    private final HttpClient httpClient;
    private final QwenTtsClient injectedClient;
    private final ProviderRateLimiter rateLimiter;

    public QwenAudioProvider(
            AudioCache audioCache,
            AudioPaths audioPaths,
            ExecutorService executorService,
            HttpClient httpClient,
            QwenTtsClient client,
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
        return "Qwen3-TTS-Flash with 7 standard Mandarin voices ("
                + String.join(", ", VOICES)
                + ")";
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
        QwenTtsResult result = getClient().synthesize(voice, text);
        return download(result.audioUrl());
    }

    private QwenTtsClient getClient() {
        if (client == null) {
            if (injectedClient != null) {
                client = injectedClient;
            } else {
                client =
                        new QwenTtsClient(
                                httpClient,
                                DashScopeConfig.getApiKey(),
                                MODEL,
                                DashScopeConfig.getBaseUrl(),
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

    private Path download(URI audioUrl) throws IOException, InterruptedException {
        HttpRequest request =
                HttpRequest.newBuilder(audioUrl)
                        .timeout(TIMEOUT)
                        .header("User-Agent", USER_AGENT)
                        .GET()
                        .build();
        HttpResponse<byte[]> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Failed to download audio: HTTP " + response.statusCode());
        }
        Path tmp = Files.createTempFile(NAME + "-", ".mp3");
        Files.write(tmp, response.body());
        return tmp;
    }
}
