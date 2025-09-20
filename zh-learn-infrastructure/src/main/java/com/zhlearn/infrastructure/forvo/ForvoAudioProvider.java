package com.zhlearn.infrastructure.forvo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.infrastructure.audio.AudioCache;
import com.zhlearn.infrastructure.audio.AudioPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Forvo-based audio provider (manual-only).
 **
 * Configuration:
 *  - Env var FORVO_API_KEY (preferred)
 *  - System property forvo.api.key (fallback)
 */
public class ForvoAudioProvider implements AudioProvider {
    private static final Logger log = LoggerFactory.getLogger(ForvoAudioProvider.class);

    private static final String NAME = "forvo";
    private static final String DESCRIPTION = "Fetch pronunciations from Forvo (manual selection only)";

    private final HttpClient http;
    private final ObjectMapper mapper;

    public ForvoAudioProvider() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), new ObjectMapper());
    }

    public ForvoAudioProvider(HttpClient http, ObjectMapper mapper) {
        this.http = http;
        this.mapper = mapper;
    }

    @Override
    public String getName() { return NAME; }

    @Override
    public String getDescription() { return DESCRIPTION; }

    @Override
    public ProviderType getType() { return ProviderType.DICTIONARY; }

    @Override
    public Optional<Path> getPronunciation(Hanzi word, Pinyin pinyin) {
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Forvo API key not configured. Set FORVO_API_KEY env var or -Dforvo.api.key");
            return Optional.empty();
        }
        // Query by Hanzi; Forvo handles the language filter
        try {
            String encoded = URLEncoder.encode(word.characters(), StandardCharsets.UTF_8);
            // Prefer top rated pronunciations in Mandarin Chinese; JSON response
            String url = "https://apifree.forvo.com/key/" + apiKey +
                    "/format/json/action/word-pronunciations/word/" + encoded +
                    "/language/zh/porder/rate-desc/perpage/10";

            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.warn("Forvo request failed: HTTP {}", resp.statusCode());
                return Optional.empty();
            }
            String body = resp.body();
            JsonNode root = mapper.readTree(body);
            JsonNode items = root.get("items");
            if (items == null || !items.isArray() || items.size() == 0) {
                log.info("Forvo: no pronunciations for '{}'", word.characters());
                return Optional.empty();
            }
            // Pick first item (already rate-desc). Only manual path for now.
            JsonNode first = items.get(0);
            String mp3 = text(first, "pathmp3");
            String username = username(first);
            if (mp3 == null || mp3.isBlank()) {
                // fallback to ogg if present? we need mp3 only by spec
                log.info("Forvo: top pronunciation missing mp3 for '{}'", word.characters());
                return Optional.empty();
            }
            Path cached = AudioPaths.audioDir()
                .resolve(getName())
                .resolve(fileName(word.characters(), username, mp3));
            if (Files.exists(cached)) {
                return Optional.of(cached.toAbsolutePath());
            }
            // Download to a temp file
            Path out = downloadMp3(mp3, word.characters());
            if (out == null) return Optional.empty();
            Path normalized = AudioCache.ensureCachedNormalized(out, getName(), word.characters(), username, mp3);
            return Optional.of(normalized.toAbsolutePath());
        } catch (IOException | InterruptedException e) {
            log.warn("Forvo error for '{}': {}", word.characters(), e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<Path> getPronunciations(Hanzi word, Pinyin pinyin) {
        List<Path> results = new ArrayList<>();
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Forvo API key not configured. Set FORVO_API_KEY env var or -Dforvo.api.key");
            return results;
        }
        try {
            String encoded = URLEncoder.encode(word.characters(), StandardCharsets.UTF_8);
            String url = "https://apifree.forvo.com/key/" + apiKey +
                "/format/json/action/word-pronunciations/word/" + encoded +
                "/language/zh/porder/rate-desc/perpage/20";

            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.warn("Forvo request failed: HTTP {}", resp.statusCode());
                return results;
            }
            JsonNode root = mapper.readTree(resp.body());
            JsonNode items = root.get("items");
            if (items == null || !items.isArray() || items.size() == 0) {
                log.info("Forvo: no pronunciations for '{}'", word.characters());
                return results;
            }
            int limit = Math.min(items.size(), 8); // cap downloads to avoid over-fetching
            for (int i = 0; i < limit; i++) {
                JsonNode n = items.get(i);
                String mp3 = text(n, "pathmp3");
                String username = username(n);
                if (mp3 == null || mp3.isBlank()) continue; // skip non-mp3 entries
                try {
                    // First check cache by deterministic file name from source URL
                    Path cached = AudioPaths.audioDir()
                        .resolve(getName())
                        .resolve(fileName(word.characters(), username, mp3));
                    if (Files.exists(cached)) {
                        results.add(cached.toAbsolutePath());
                        continue;
                    }
                    Path tmp = downloadMp3(mp3, word.characters());
                    if (tmp != null) {
                        Path norm = AudioCache.ensureCachedNormalized(tmp, getName(), word.characters(), username, mp3);
                        results.add(norm.toAbsolutePath());
                    }
                } catch (IOException | InterruptedException e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    throw new RuntimeException("Forvo download failed (" + i + ")", e);
                }
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Forvo error for '" + word.characters() + "'", e);
        }
        return results;
    }

    @Override
    public List<PronunciationDescription> getPronunciationsWithDescriptions(Hanzi word, Pinyin pinyin) {
        List<PronunciationDescription> results = new ArrayList<>();
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Forvo API key not configured. Set FORVO_API_KEY env var or -Dforvo.api.key");
            return results;
        }
        try {
            String encoded = URLEncoder.encode(word.characters(), StandardCharsets.UTF_8);
            String url = "https://apifree.forvo.com/key/" + apiKey +
                "/format/json/action/word-pronunciations/word/" + encoded +
                "/language/zh/porder/rate-desc/perpage/20";

            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.warn("Forvo request failed: HTTP {}", resp.statusCode());
                return results;
            }
            JsonNode root = mapper.readTree(resp.body());
            JsonNode items = root.get("items");
            if (items == null || !items.isArray() || items.size() == 0) {
                log.info("Forvo: no pronunciations for '{}'", word.characters());
                return results;
            }
            int limit = Math.min(items.size(), 8); // cap downloads to avoid over-fetching
            for (int i = 0; i < limit; i++) {
                JsonNode n = items.get(i);
                String mp3 = text(n, "pathmp3");
                String username = username(n);
                if (mp3 == null || mp3.isBlank()) continue; // skip non-mp3 entries
                try {
                    // First check cache by deterministic file name from source URL
                    Path cached = AudioPaths.audioDir()
                        .resolve(getName())
                        .resolve(fileName(word.characters(), username, mp3));
                    if (Files.exists(cached)) {
                        String description = formatForvoDescription(username);
                        results.add(new PronunciationDescription(cached.toAbsolutePath(), description));
                        continue;
                    }
                    Path tmp = downloadMp3(mp3, word.characters());
                    if (tmp != null) {
                        Path norm = AudioCache.ensureCachedNormalized(tmp, getName(), word.characters(), username, mp3);
                        String description = formatForvoDescription(username);
                        results.add(new PronunciationDescription(norm.toAbsolutePath(), description));
                    }
                } catch (IOException | InterruptedException e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    throw new RuntimeException("Forvo download failed (" + i + ")", e);
                }
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Forvo error for '" + word.characters() + "'", e);
        }
        return results;
    }

    private static String formatForvoDescription(String username) {
        // Add user and Chinese flag emoji (placeholder - could be enhanced with country detection)
        return username + " 👤🇨🇳";
    }

    private static String shortHash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            String hex = HexFormat.of().withUpperCase().formatHex(d);
            return hex.substring(0, 10);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 digest not available", e);
        }
    }

    private static String username(JsonNode node) {
        String value = text(node, "username");
        if (value == null || value.isBlank()) {
            value = text(node, "user");
        }
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value;
    }

    private static String fileName(String word, String username, String sourceId) {
        String user = (username == null || username.isBlank()) ? "unknown" : username;
        String safeWord = AudioPaths.sanitize(word);
        String safeUser = AudioPaths.sanitize(user);
        return NAME + "_" + safeWord + "_" + safeUser + "_" + shortHash(sourceId) + ".mp3";
    }

    private Path downloadMp3(String url, String word) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() != 200) {
            log.warn("Forvo mp3 download failed: HTTP {}", resp.statusCode());
            return null;
        }
        String safe = sanitize(word);
        Path out = Files.createTempFile("zhlearn-forvo-" + safe + "-", ".mp3");
        Files.write(out, resp.body());
        out.toFile().deleteOnExit();
        return out;
    }

    private static String sanitize(String s) {
        return s.replaceAll("[^\u4e00-\u9fffA-Za-z0-9_-]", "");
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v != null && !v.isNull() ? v.asText() : null;
    }

    private static String getApiKey() {
        String k = System.getenv("FORVO_API_KEY");
        if (k == null || k.isBlank()) k = System.getProperty("forvo.api.key");
        return k;
    }
}
