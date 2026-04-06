package com.zhlearn.infrastructure.tencent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

class TencentTtsClientTest {

    @Test
    void synthesizeSuccessfully() throws Exception {
        HttpClient http = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body())
                .thenReturn(
                        "{\"Response\":{\"Audio\":\"fake-audio-data\",\"SessionId\":\"test-session-123\",\"RequestId\":\"req-1\"}}");
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);

        TencentTtsClient client =
                new TencentTtsClient(
                        http,
                        "test-secret-id",
                        "test-secret-key",
                        "ap-singapore",
                        "tts.tencentcloudapi.com",
                        "https://tts.tencentcloudapi.com",
                        null);

        TencentTtsResult result = client.synthesize(101052, "学习");

        assertThat(result.audioData()).isEqualTo("fake-audio-data");
        assertThat(result.sessionId()).isEqualTo("test-session-123");
    }

    @Test
    void handlesApiError() throws Exception {
        HttpClient http = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body())
                .thenReturn(
                        "{\"Response\":{\"Error\":{\"Code\":\"InvalidParameter\",\"Message\":\"Invalid voice type\"},\"RequestId\":\"req-2\"}}");
        when(http.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);

        TencentTtsClient client =
                new TencentTtsClient(
                        http,
                        "test-secret-id",
                        "test-secret-key",
                        "ap-singapore",
                        "tts.tencentcloudapi.com",
                        "https://tts.tencentcloudapi.com",
                        null);

        assertThatThrownBy(() -> client.synthesize(999999, "学习"))
                .isInstanceOf(TencentTtsClientException.class)
                .hasMessageContaining("InvalidParameter")
                .hasMessageContaining("Invalid voice type");
    }
}
