package com.zhlearn.infrastructure.forvo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;

class ForvoAudioProviderTest {

    @TempDir Path tmpHome;

    @AfterEach
    void tearDown() {
        System.clearProperty("zhlearn.home");
        System.clearProperty("forvo.api.key");
    }

    @Test
    void returnsMultiplePronunciationsFromJson() throws Exception {
        HttpClient http = mock(HttpClient.class);

        // Mock JSON listing three items
        String json =
                "{\n"
                        + "  \"items\": [\n"
                        + "    { \"pathmp3\": \"https://audio.example/a.mp3\", \"username\": \"UserOne\" },\n"
                        + "    { \"pathmp3\": \"https://audio.example/b.mp3\", \"username\": \"UserTwo\" },\n"
                        + "    { \"pathmp3\": \"https://audio.example/c.mp3\", \"username\": \"UserThree\" }\n"
                        + "  ]\n"
                        + "}";

        @SuppressWarnings("unchecked")
        HttpResponse<String> resp1 = (HttpResponse<String>) mock(HttpResponse.class);
        when(resp1.statusCode()).thenReturn(200);
        when(resp1.body()).thenReturn(json);

        @SuppressWarnings("unchecked")
        HttpResponse<byte[]> resp2 = (HttpResponse<byte[]>) mock(HttpResponse.class);
        when(resp2.statusCode()).thenReturn(200);
        when(resp2.body()).thenReturn(new byte[] {1});

        @SuppressWarnings("unchecked")
        HttpResponse<byte[]> resp3 = (HttpResponse<byte[]>) mock(HttpResponse.class);
        when(resp3.statusCode()).thenReturn(200);
        when(resp3.body()).thenReturn(new byte[] {2});

        @SuppressWarnings("unchecked")
        HttpResponse<byte[]> resp4 = (HttpResponse<byte[]>) mock(HttpResponse.class);
        when(resp4.statusCode()).thenReturn(200);
        when(resp4.body()).thenReturn(new byte[] {3});

        // First call returns JSON; next three return mp3 bytes
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((HttpResponse) resp1)
                .thenReturn((HttpResponse) resp2)
                .thenReturn((HttpResponse) resp3)
                .thenReturn((HttpResponse) resp4);

        System.setProperty("zhlearn.home", tmpHome.toString());
        System.setProperty("forvo.api.key", "test-key");
        ForvoAudioProvider provider = new ForvoAudioProvider(http, new ObjectMapper());

        List<Path> list = provider.getPronunciations(new Hanzi("学习"), new Pinyin("xuéxí"));
        assertThat(list).hasSize(3);

        Path firstPath = list.get(0);
        Path secondPath = list.get(1);
        Path thirdPath = list.get(2);

        assertThat(firstPath.getFileName().toString()).startsWith("forvo_学习_UserOne_");
        assertThat(secondPath.getFileName().toString()).startsWith("forvo_学习_UserTwo_");
        assertThat(thirdPath.getFileName().toString()).startsWith("forvo_学习_UserThree_");

        assertThat(Files.exists(firstPath)).isTrue();
        assertThat(Files.exists(secondPath)).isTrue();
        assertThat(Files.exists(thirdPath)).isTrue();
    }
}
