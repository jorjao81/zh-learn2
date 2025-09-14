package com.zhlearn.infrastructure.forvo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ForvoAudioProviderTest {

    @Test
    void returnsMultiplePronunciationsFromJson() throws Exception {
        HttpClient http = mock(HttpClient.class);

        // Mock JSON listing three items
        String json = "{\n" +
            "  \"items\": [\n" +
            "    { \"pathmp3\": \"https://audio.example/a.mp3\" },\n" +
            "    { \"pathmp3\": \"https://audio.example/b.mp3\" },\n" +
            "    { \"pathmp3\": \"https://audio.example/c.mp3\" }\n" +
            "  ]\n" +
            "}";

        @SuppressWarnings("unchecked")
        HttpResponse<String> resp1 = (HttpResponse<String>) mock(HttpResponse.class);
        when(resp1.statusCode()).thenReturn(200);
        when(resp1.body()).thenReturn(json);

        @SuppressWarnings("unchecked")
        HttpResponse<byte[]> resp2 = (HttpResponse<byte[]>) mock(HttpResponse.class);
        when(resp2.statusCode()).thenReturn(200);
        when(resp2.body()).thenReturn(new byte[]{1});

        @SuppressWarnings("unchecked")
        HttpResponse<byte[]> resp3 = (HttpResponse<byte[]>) mock(HttpResponse.class);
        when(resp3.statusCode()).thenReturn(200);
        when(resp3.body()).thenReturn(new byte[]{2});

        @SuppressWarnings("unchecked")
        HttpResponse<byte[]> resp4 = (HttpResponse<byte[]>) mock(HttpResponse.class);
        when(resp4.statusCode()).thenReturn(200);
        when(resp4.body()).thenReturn(new byte[]{3});

        // First call returns JSON; next three return mp3 bytes
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn((HttpResponse) resp1)
            .thenReturn((HttpResponse) resp2)
            .thenReturn((HttpResponse) resp3)
            .thenReturn((HttpResponse) resp4);

        System.setProperty("forvo.api.key", "test-key");
        ForvoAudioProvider provider = new ForvoAudioProvider(http, new ObjectMapper());

        List<String> list = provider.getPronunciations(new Hanzi("学习"), new Pinyin("xuéxí"));
        assertThat(list).hasSize(3);
        assertThat(list.get(0)).startsWith("[sound:");
    }
}
