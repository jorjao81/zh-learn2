package com.zhlearn.infrastructure.qwen;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QwenAudioProviderTest {

    @TempDir
    Path tmpHome;

    @AfterEach
    void tearDown() {
        System.clearProperty("zhlearn.home");
    }

    @Test
    void returnsSevenVoicesAndCachesResults() throws Exception {
        System.setProperty("zhlearn.home", tmpHome.toString());

        FakeQwenClient client = new FakeQwenClient();
        HttpClient http = mock(HttpClient.class);

        HttpResponse<byte[]> cherryResp = mockBinaryResponse(new byte[]{1, 2, 3});
        HttpResponse<byte[]> ethanResp = mockBinaryResponse(new byte[]{4, 5});
        HttpResponse<byte[]> nofishResp = mockBinaryResponse(new byte[]{6});
        HttpResponse<byte[]> jenniferResp = mockBinaryResponse(new byte[]{7, 8});
        HttpResponse<byte[]> ryanResp = mockBinaryResponse(new byte[]{9});
        HttpResponse<byte[]> katerinaResp = mockBinaryResponse(new byte[]{10, 11});
        HttpResponse<byte[]> eliasResp = mockBinaryResponse(new byte[]{12});

        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn((HttpResponse) cherryResp)
            .thenReturn((HttpResponse) ethanResp)
            .thenReturn((HttpResponse) nofishResp)
            .thenReturn((HttpResponse) jenniferResp)
            .thenReturn((HttpResponse) ryanResp)
            .thenReturn((HttpResponse) katerinaResp)
            .thenReturn((HttpResponse) eliasResp);

        QwenAudioProvider provider = new QwenAudioProvider(client, http);

        Hanzi word = new Hanzi("学习");
        Pinyin pinyin = new Pinyin("xuéxí");

        List<Path> pronunciations = provider.getPronunciations(word, pinyin);

        assertThat(pronunciations).hasSize(7);
        for (Path path : pronunciations) {
            assertThat(path).isAbsolute();
        }

        assertThat(pronunciations.get(0).getFileName().toString()).contains("Cherry").doesNotContain("xuéxí");
        assertThat(pronunciations.get(1).getFileName().toString()).contains("Ethan");
        assertThat(pronunciations.get(2).getFileName().toString()).contains("Nofish");
        assertThat(pronunciations.get(3).getFileName().toString()).contains("Jennifer");
        assertThat(pronunciations.get(4).getFileName().toString()).contains("Ryan");
        assertThat(pronunciations.get(5).getFileName().toString()).contains("Katerina");
        assertThat(pronunciations.get(6).getFileName().toString()).contains("Elias");

        for (Path path : pronunciations) {
            assertThat(Files.exists(path)).isTrue();
        }

        assertThat(provider.getPronunciation(word, pinyin)).contains(pronunciations.get(0));

        List<Path> cached = provider.getPronunciations(word, pinyin);
        assertThat(cached).containsExactlyElementsOf(pronunciations);

        verify(http, times(7)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        verifyNoMoreInteractions(http);
    }

    @Test
    void propagatesHttpFailure() throws Exception {
        System.setProperty("zhlearn.home", tmpHome.toString());

        FakeQwenClient client = new FakeQwenClient();
        HttpClient http = mock(HttpClient.class);

        HttpResponse<byte[]> failure = mock(HttpResponse.class);
        when(failure.statusCode()).thenReturn(500);
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn((HttpResponse) failure);

        QwenAudioProvider provider = new QwenAudioProvider(client, http);

        Hanzi word = new Hanzi("语音");
        Pinyin pinyin = new Pinyin("yǔyīn");

        Throwable thrown = catchThrowable(() -> provider.getPronunciations(word, pinyin));
        assertThat(thrown)
            .isInstanceOf(RuntimeException.class)
            .hasCauseInstanceOf(IOException.class);
        assertThat(thrown.getCause()).hasMessageContaining("Failed to download audio");
    }

    @Test
    void reportsMetadataAccurately() {
        QwenAudioProvider provider = new QwenAudioProvider(new FakeQwenClient(), HttpClient.newHttpClient());
        assertThat(provider.getName()).isEqualTo("qwen-tts");
        assertThat(provider.getType()).isEqualTo(ProviderType.AI);
        assertThat(provider.getDescription()).contains("Cherry").contains("Ethan").contains("Nofish").contains("Jennifer").contains("Ryan").contains("Katerina").contains("Elias");
    }

    private HttpResponse<byte[]> mockBinaryResponse(byte[] data) {
        @SuppressWarnings("unchecked")
        HttpResponse<byte[]> response = (HttpResponse<byte[]>) mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(data);
        return response;
    }

    private static class FakeQwenClient extends QwenTtsClient {
        private int callCount = 0;

        FakeQwenClient() {
            super(HttpClient.newHttpClient(), "test-key", "qwen3-tts-flash");
        }

        @Override
        public QwenTtsResult synthesize(String voice, String text) {
            callCount++;
            return new QwenTtsResult(
                URI.create("https://example.com/" + voice.toLowerCase() + callCount + ".mp3"),
                "req-" + voice.toLowerCase()
            );
        }
    }
}
