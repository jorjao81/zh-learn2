package com.zhlearn.infrastructure.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Hanzi;

class GoogleImageSearchProviderTest {

    @Test
    void shouldParseImagesFromSuccessfulResponse() throws Exception {
        String responseBody =
                """
                        {
                          \"items\": [
                            {
                              \"link\": \"https://example.com/image.jpg\",
                              \"mime\": \"image/jpeg\",
                              \"title\": \"Example Image\",
                              \"image\": {
                                \"thumbnailLink\": \"https://example.com/thumb.jpg\",
                                \"width\": 800,
                                \"height\": 600
                              }
                            }
                          ]
                        }
                        """;

        HttpClient client = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(client.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(responseBody);

        GoogleImageSearchProvider provider =
                new GoogleImageSearchProvider(
                        client,
                        new ObjectMapper(),
                        "fake-key",
                        "search-engine",
                        "https://search.example.com");

        assertThat(provider.searchImages(new Hanzi("学"), new Definition("study"), 3))
                .hasSize(1)
                .first()
                .extracting(image -> image.sourceUrl().toString())
                .isEqualTo("https://example.com/image.jpg");
    }

    @Test
    void shouldFailWhenNoImagesReturned() throws Exception {
        HttpClient client = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(client.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"items\": []}");

        GoogleImageSearchProvider provider =
                new GoogleImageSearchProvider(
                        client,
                        new ObjectMapper(),
                        "fake-key",
                        "search-engine",
                        "https://search.example.com");

        assertThatThrownBy(() -> provider.searchImages(new Hanzi("学"), new Definition("study"), 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no images found");
    }

    @Test
    void shouldFailOnNon200Status() throws Exception {
        HttpClient client = mock(HttpClient.class);
        @SuppressWarnings("unchecked")
        HttpResponse<String> response = mock(HttpResponse.class);
        when(client.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        when(response.statusCode()).thenReturn(500);
        when(response.body()).thenReturn("server error");

        GoogleImageSearchProvider provider =
                new GoogleImageSearchProvider(
                        client,
                        new ObjectMapper(),
                        "fake-key",
                        "search-engine",
                        "https://search.example.com");

        assertThatThrownBy(() -> provider.searchImages(new Hanzi("学"), new Definition("study"), 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("HTTP 500");
    }
}
