package com.zhlearn.infrastructure.qwen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.helidon.faulttolerance.Retry;

class QwenTtsClientTest {

    @Test
    void usesSingaporeDashScopeEndpoint() throws Exception {
        Field endpointField = QwenTtsClient.class.getDeclaredField("ENDPOINT");
        endpointField.setAccessible(true);
        URI endpoint = (URI) endpointField.get(null);

        assertThat(endpoint.getHost()).isEqualTo("dashscope-intl.aliyuncs.com");
        assertThat(endpoint.getPath())
                .isEqualTo("/api/v1/services/aigc/multimodal-generation/generation");
        assertThat(endpoint.getScheme()).isEqualTo("https");
    }

    @Test
    void retriesOnRateLimitThenSucceeds() throws Exception {
        HttpClient http = mock(HttpClient.class);
        HttpResponse<String> rateLimited = mockResponse(429, "{\"code\":\"429\"}");
        HttpResponse<String> success =
                mockResponse(
                        200,
                        "{\"output\":{\"audio\":{\"url\":\"https://example.com/audio.mp3\"}},\"request_id\":\"req-123\"}");

        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(rateLimited)
                .thenReturn(rateLimited)
                .thenReturn(success);

        Retry retry =
                Retry.create(
                        builder ->
                                builder.calls(3)
                                        .delay(Duration.ofMillis(5))
                                        .delayFactor(1.0)
                                        .overallTimeout(Duration.ofSeconds(1))
                                        .applyOn(Set.of(QwenTtsClient.RateLimitException.class)));

        QwenTtsClient client =
                new QwenTtsClient(http, "key", "qwen3-tts-flash", new ObjectMapper(), retry, null);

        QwenTtsResult result = client.synthesize("Cherry", "学习");

        assertThat(result.audioUrl()).isEqualTo(URI.create("https://example.com/audio.mp3"));
        verify(http, times(3)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void failsAfterExhaustingRateLimitRetries() throws Exception {
        HttpClient http = mock(HttpClient.class);
        HttpResponse<String> rateLimited = mockResponse(429, "{\"code\":\"429\"}");

        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(rateLimited)
                .thenReturn(rateLimited)
                .thenReturn(rateLimited);

        Retry retry =
                Retry.create(
                        builder ->
                                builder.calls(3)
                                        .delay(Duration.ofMillis(5))
                                        .delayFactor(1.0)
                                        .overallTimeout(Duration.ofSeconds(1))
                                        .applyOn(Set.of(QwenTtsClient.RateLimitException.class)));

        QwenTtsClient client =
                new QwenTtsClient(http, "key", "qwen3-tts-flash", new ObjectMapper(), retry, null);

        assertThatThrownBy(() -> client.synthesize("Cherry", "学习"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("rate limit exhausted");

        verify(http, times(3)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    private HttpResponse<String> mockResponse(int status, String body) {
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = (HttpResponse<String>) mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        return response;
    }
}
