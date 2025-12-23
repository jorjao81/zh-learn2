package com.zhlearn.infrastructure.minimax;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.infrastructure.audio.AudioCache;
import com.zhlearn.infrastructure.audio.AudioNormalizer;
import com.zhlearn.infrastructure.audio.AudioPaths;

class MiniMaxAudioProviderTest {

    @TempDir Path tmpHome;

    @AfterEach
    void tearDown() {
        System.clearProperty("zhlearn.home");
    }

    private MiniMaxAudioProvider createProvider(MiniMaxTtsClient client) {
        AudioPaths audioPaths = new AudioPaths();
        AudioNormalizer normalizer = new AudioNormalizer();
        AudioCache audioCache = new AudioCache(audioPaths, normalizer);
        return new MiniMaxAudioProvider(
                audioCache, audioPaths, null, HttpClient.newHttpClient(), client, null);
    }

    @Test
    void returnsFourVoicesAndCachesResults() throws Exception {
        System.setProperty("zhlearn.home", tmpHome.toString());

        FakeMiniMaxClient client = new FakeMiniMaxClient();
        MiniMaxAudioProvider provider = createProvider(client);

        Hanzi word = new Hanzi("学习");
        Pinyin pinyin = new Pinyin("xuéxí");

        List<Path> pronunciations = provider.getPronunciations(word, pinyin);

        assertThat(pronunciations).hasSize(4);
        for (Path path : pronunciations) {
            assertThat(path).isAbsolute();
        }

        assertThat(pronunciations.get(0).getFileName().toString()).contains("Wise_Woman");
        assertThat(pronunciations.get(1).getFileName().toString()).contains("Deep_Voice_Man");
        assertThat(pronunciations.get(2).getFileName().toString()).contains("Young_Knight");
        assertThat(pronunciations.get(3).getFileName().toString()).contains("Calm_Woman");

        for (Path path : pronunciations) {
            assertThat(Files.exists(path)).isTrue();
        }

        assertThat(provider.getPronunciation(word, pinyin)).contains(pronunciations.get(0));

        // Second call should use cached results
        List<Path> cached = provider.getPronunciations(word, pinyin);
        assertThat(cached).containsExactlyElementsOf(pronunciations);

        // Verify client was only called 4 times (once per voice)
        assertThat(client.callCount).isEqualTo(4);
    }

    @Test
    void propagatesClientFailure() {
        System.setProperty("zhlearn.home", tmpHome.toString());

        FailingMiniMaxClient client = new FailingMiniMaxClient();
        MiniMaxAudioProvider provider = createProvider(client);

        Hanzi word = new Hanzi("语音");
        Pinyin pinyin = new Pinyin("yǔyīn");

        Throwable thrown = catchThrowable(() -> provider.getPronunciations(word, pinyin));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IOException.class);
        assertThat(thrown.getCause()).hasMessageContaining("Simulated MiniMax failure");
    }

    @Test
    void reportsMetadataAccurately() {
        MiniMaxAudioProvider provider = createProvider(new FakeMiniMaxClient());
        assertThat(provider.getName()).isEqualTo("minimax-tts");
        assertThat(provider.getType()).isEqualTo(ProviderType.AI);
        assertThat(provider.getDescription())
                .contains("MiniMax")
                .contains("Speech-2.6-HD")
                .contains("Wise_Woman")
                .contains("Deep_Voice_Man")
                .contains("Young_Knight")
                .contains("Calm_Woman")
                .doesNotContain("Lovely_Girl");
    }

    /** Fake client that returns valid MP3-like data for testing. */
    private static class FakeMiniMaxClient extends MiniMaxTtsClient {
        private int callCount = 0;

        FakeMiniMaxClient() {
            super(
                    HttpClient.newHttpClient(),
                    "test-api-key",
                    "test-group-id",
                    "https://api.test.com",
                    "speech-2.6-hd");
        }

        @Override
        public MiniMaxTtsResult synthesize(String voiceId, String text) {
            callCount++;
            // Return fake MP3 data (ID3 header + some bytes)
            byte[] fakeMp3 = new byte[] {'I', 'D', '3', 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5};
            return new MiniMaxTtsResult(
                    fakeMp3, "trace-" + voiceId.toLowerCase() + "-" + callCount);
        }
    }

    /** Client that always fails for error handling tests. */
    private static class FailingMiniMaxClient extends MiniMaxTtsClient {

        FailingMiniMaxClient() {
            super(
                    HttpClient.newHttpClient(),
                    "test-api-key",
                    "test-group-id",
                    "https://api.test.com",
                    "speech-2.6-hd");
        }

        @Override
        public MiniMaxTtsResult synthesize(String voiceId, String text) throws IOException {
            throw new IOException("Simulated MiniMax failure for voice: " + voiceId);
        }
    }
}
