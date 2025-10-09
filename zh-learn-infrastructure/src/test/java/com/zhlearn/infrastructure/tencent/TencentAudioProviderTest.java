package com.zhlearn.infrastructure.tencent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
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

class TencentAudioProviderTest {

    @TempDir Path tmpHome;

    @AfterEach
    void tearDown() {
        System.clearProperty("zhlearn.home");
    }

    private TencentAudioProvider createProvider(TencentTtsClient client) {
        AudioPaths audioPaths = new AudioPaths();
        AudioNormalizer normalizer = new AudioNormalizer();
        AudioCache audioCache = new AudioCache(audioPaths, normalizer);
        return new TencentAudioProvider(audioCache, audioPaths, null, client);
    }

    @Test
    void returnsTwoVoicesAndCachesResults() {
        System.setProperty("zhlearn.home", tmpHome.toString());

        FakeTencentClient client = new FakeTencentClient();
        TencentAudioProvider provider = createProvider(client);

        Hanzi word = new Hanzi("学习");
        Pinyin pinyin = new Pinyin("xuéxí");

        List<Path> pronunciations = provider.getPronunciations(word, pinyin);

        assertThat(pronunciations).hasSize(2);
        assertThat(pronunciations.get(0)).isAbsolute();
        assertThat(pronunciations.get(1)).isAbsolute();

        assertThat(pronunciations.get(0).getFileName().toString())
                .contains("zhiwei")
                .doesNotContain("xuéxí");
        assertThat(pronunciations.get(1).getFileName().toString()).contains("zhiling");

        assertThat(Files.exists(pronunciations.get(0))).isTrue();
        assertThat(Files.exists(pronunciations.get(1))).isTrue();

        assertThat(provider.getPronunciation(word, pinyin)).contains(pronunciations.get(0));

        // Test caching
        List<Path> cached = provider.getPronunciations(word, pinyin);
        assertThat(cached).containsExactlyElementsOf(pronunciations);

        // Verify client was only called twice (once per voice) due to caching
        assertThat(client.callCount).isEqualTo(2);
    }

    @Test
    void propagatesClientFailure() {
        System.setProperty("zhlearn.home", tmpHome.toString());

        FailingTencentClient client = new FailingTencentClient();
        TencentAudioProvider provider = createProvider(client);

        Hanzi word = new Hanzi("语音");
        Pinyin pinyin = new Pinyin("yǔyīn");

        Throwable thrown = catchThrowable(() -> provider.getPronunciations(word, pinyin));
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(RuntimeException.class);
        assertThat(thrown.getCause()).hasMessageContaining("Test failure for voice");
    }

    @Test
    void reportsMetadataAccurately() {
        TencentAudioProvider provider = createProvider(new FakeTencentClient());
        assertThat(provider.getName()).isEqualTo("tencent-tts");
        assertThat(provider.getType()).isEqualTo(ProviderType.AI);
        assertThat(provider.getDescription()).contains("zhiwei").contains("zhiling");
    }

    @Test
    void failsFastWhenClientFails() {
        EmptyTencentClient client = new EmptyTencentClient();
        TencentAudioProvider provider = createProvider(client);

        Hanzi word = new Hanzi("测试");
        Pinyin pinyin = new Pinyin("cèshì");

        // Should fail fast according to constitution
        Throwable thrown1 = catchThrowable(() -> provider.getPronunciation(word, pinyin));
        assertThat(thrown1)
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(RuntimeException.class);

        Throwable thrown2 = catchThrowable(() -> provider.getPronunciations(word, pinyin));
        assertThat(thrown2)
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void handlesVoiceMappingCorrectly() {
        System.setProperty("zhlearn.home", tmpHome.toString());

        FakeTencentClient client = new FakeTencentClient();
        TencentAudioProvider provider = createProvider(client);

        // Verify voice IDs are correctly mapped
        assertThat(client.receivedVoiceTypes).isEmpty();

        Hanzi word = new Hanzi("你好");
        Pinyin pinyin = new Pinyin("nǐhǎo");

        provider.getPronunciations(word, pinyin);

        // Should have called with both voice IDs: 101052 (zhiwei) and 101002 (zhiling)
        assertThat(client.receivedVoiceTypes).containsExactly(101052, 101002);
    }

    private static class FakeTencentClient extends TencentTtsClient {
        int callCount = 0;
        final List<Integer> receivedVoiceTypes = new ArrayList<>();

        FakeTencentClient() {
            super("test-secret-id", "test-secret-key", "ap-singapore");
        }

        @Override
        public TencentTtsResult synthesize(int voiceType, String text) {
            callCount++;
            receivedVoiceTypes.add(voiceType);
            // Create some fake MP3 data (minimal valid MP3 header)
            byte[] mp3Data =
                    new byte[] {(byte) 0xFF, (byte) 0xFB, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00};
            String base64Audio = Base64.getEncoder().encodeToString(mp3Data);
            return new TencentTtsResult(base64Audio, "session-" + voiceType);
        }
    }

    private static class FailingTencentClient extends TencentTtsClient {
        FailingTencentClient() {
            super("test-secret-id", "test-secret-key", "ap-singapore");
        }

        @Override
        public TencentTtsResult synthesize(int voiceType, String text) {
            throw new TencentTtsClientException("Test failure for voice " + voiceType);
        }
    }

    private static class EmptyTencentClient extends TencentTtsClient {
        EmptyTencentClient() {
            super("test-secret-id", "test-secret-key", "ap-singapore");
        }

        @Override
        public TencentTtsResult synthesize(int voiceType, String text) {
            throw new TencentTtsClientException("No voices available");
        }
    }
}
