package com.zhlearn.infrastructure.audio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioCache {
    private final Logger log = LoggerFactory.getLogger(AudioCache.class);
    private final AudioPaths audioPaths;
    private final AudioNormalizer audioNormalizer;

    public AudioCache(AudioPaths audioPaths, AudioNormalizer audioNormalizer) {
        this.audioPaths = audioPaths;
        this.audioNormalizer = audioNormalizer;
    }

    /**
     * Ensure a normalized MP3 exists in the cache for this source audio. Returns the target path.
     * If a content-identical file already exists, normalization is skipped.
     *
     * @param src source audio file (mp3 or other; if null and sourceId used, method will no-op)
     * @param provider provider label (e.g., "forvo")
     * @param hanzi the Chinese characters
     * @param pinyin pinyin with tone marks
     * @param sourceId optional stable id (e.g., remote URL) to name files deterministically
     */
    public Path ensureCachedNormalized(
            Path src, String provider, String hanzi, String pinyin, String sourceId)
            throws IOException, InterruptedException {
        String name = buildName(provider, hanzi, pinyin, src, sourceId);
        Path target = audioPaths.audioDir().resolve(provider).resolve(name);
        Files.createDirectories(target.getParent());

        if (Files.exists(target)) return target.toAbsolutePath();
        if (src == null || !Files.exists(src)) {
            throw new IOException(
                    "Source audio missing for cache: " + src + " (target: " + target + ")");
        }
        audioNormalizer.normalizeToMp3(src, target);
        return target.toAbsolutePath();
    }

    private String buildName(
            String provider, String hanzi, String pinyin, Path src, String sourceId)
            throws IOException {
        String base =
                audioPaths.sanitize(provider)
                        + "_"
                        + audioPaths.sanitize(hanzi)
                        + "_"
                        + audioPaths.sanitize(pinyin);
        String hash = "";
        if (sourceId != null && !sourceId.isBlank()) {
            hash = shortHash(sourceId.getBytes(StandardCharsets.UTF_8));
        } else if (src != null && Files.exists(src)) {
            hash = shortHash(Files.readAllBytes(src));
        }
        if (!hash.isEmpty()) base = base + "_" + hash;
        return base + ".mp3";
    }

    private String shortHash(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] d = md.digest(bytes);
            String hex = HexFormat.of().withUpperCase().formatHex(d);
            return hex.substring(0, 10);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 digest not available", e);
        }
    }
}
