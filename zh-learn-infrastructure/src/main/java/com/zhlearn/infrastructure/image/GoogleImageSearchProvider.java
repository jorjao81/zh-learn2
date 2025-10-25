package com.zhlearn.infrastructure.image;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Image;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ImageProvider;

/** Google Custom Search API provider for image search. */
public class GoogleImageSearchProvider implements ImageProvider {
    private static final Logger log = LoggerFactory.getLogger(GoogleImageSearchProvider.class);

    private static final String NAME = "google";
    private static final String DESCRIPTION = "Search images using Google Custom Search API";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String searchEngineId;
    private final String endpoint;

    public GoogleImageSearchProvider() {
        this(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
                new ObjectMapper(),
                GoogleImageSearchConfig.getApiKey(),
                GoogleImageSearchConfig.getSearchEngineId(),
                GoogleImageSearchConfig.getEndpoint());
    }

    public GoogleImageSearchProvider(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            String apiKey,
            String searchEngineId,
            String endpoint) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.searchEngineId = searchEngineId;
        this.endpoint = endpoint;

        // Fail-fast validation
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Google Search API key cannot be null or empty. "
                            + "Set GOOGLE_SEARCH_API_KEY environment variable.");
        }
        if (searchEngineId == null || searchEngineId.isBlank()) {
            throw new IllegalStateException(
                    "Google Search engine ID cannot be null or empty. "
                            + "Set GOOGLE_SEARCH_ENGINE_ID environment variable.");
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public ProviderType getType() {
        return ProviderType.DICTIONARY;
    }

    @Override
    public List<Image> searchImages(Hanzi word, Definition definition, int maxResults) {
        String query = word.characters() + " " + definition.meaning();
        log.info("[Google Images] Searching for: '{}'", query);

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url =
                    String.format(
                            "%s?key=%s&cx=%s&q=%s&searchType=image&num=%d",
                            endpoint, apiKey, searchEngineId, encodedQuery, maxResults);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(Duration.ofSeconds(30))
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                throw new IllegalStateException(
                        "Google Image Search quota exceeded (100 queries/day free tier). "
                                + "Monitor usage at console.cloud.google.com");
            }

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Google Image Search API error: HTTP "
                                + response.statusCode()
                                + " - "
                                + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode items = root.get("items");

            if (items == null || !items.isArray() || items.size() == 0) {
                throw new IllegalStateException(
                        "Google Image Search returned 0 results for query '"
                                + query
                                + "'. This may indicate a configuration issue.");
            }

            List<Image> results = new ArrayList<>();
            for (JsonNode item : items) {
                Image image = parseImageFromJson(item);
                results.add(image);
            }

            log.info("[Google Images] Found {} images for '{}'", results.size(), query);
            return results;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to fetch images from Google Custom Search API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Image search interrupted: " + e.getMessage(), e);
        }
    }

    private Image parseImageFromJson(JsonNode item) {
        String link = item.get("link").asText();
        JsonNode imageNode = item.get("image");

        String thumbnailLink = imageNode.get("thumbnailLink").asText();
        int width = imageNode.get("width").asInt();
        int height = imageNode.get("height").asInt();

        JsonNode titleNode = item.get("title");
        Optional<String> title =
                titleNode != null && !titleNode.isNull()
                        ? Optional.of(titleNode.asText())
                        : Optional.empty();

        String contentType = item.get("mime").asText();

        return new Image(URI.create(link), thumbnailLink, title, width, height, contentType);
    }
}
