package com.zhlearn.infrastructure.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

public class AudioCache {
    private static final Logger log = LoggerFactory.getLogger(AudioCache.class);

    /**
     * Ensure a normalized MP3 exists in the cache for this source audio. Returns the target path.
     * If a content-identical file already exists, normalization is skipped.
     *
     * @param src       source audio file (mp3 or other; if null and sourceId used, method will no-op)
     * @param provider  provider label (e.g., "forvo")
     * @param hanzi     the Chinese characters
     * @param pinyin    pinyin with tone marks
     * @param sourceId  optional stable id (e.g., remote URL) to name files deterministically
     */
    public static Path ensureCachedNormalized(Path src, String provider, String hanzi, String pinyin, String sourceId) throws IOException, InterruptedException {
        String name = buildName(provider, hanzi, pinyin, src, sourceId);
        Path target = AudioPaths.audioDir().resolve(provider).resolve(name);
        Files.createDirectories(target.getParent());

        if (Files.exists(target)) return target.toAbsolutePath();
        if (src == null || !Files.exists(src)) {
            throw new IOException("Source audio missing for cache: " + src + " (target: " + target + ")");
        }
        AudioNormalizer.normalizeToMp3(src, target);
        return target.toAbsolutePath();
    }

    private static String buildName(String provider, String hanzi, String pinyin, Path src, String sourceId) throws IOException {
        String base = AudioPaths.sanitize(provider) + "_" + AudioPaths.sanitize(hanzi) + "_" + AudioPaths.sanitize(pinyin);
        String hash = "";
        if (sourceId != null && !sourceId.isBlank()) {
            hash = shortHash(sourceId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } else if (src != null && Files.exists(src)) {
            hash = shortHash(Files.readAllBytes(src));
        }
        if (!hash.isEmpty()) base = base + "_" + hash;
        return base + ".mp3";
    }

    private static String shortHash(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] d = md.digest(bytes);
            String hex = HexFormat.of().withUpperCase().formatHex(d);
            return hex.substring(0, 10);
        } catch (Exception e) {
            return "";
        }
    }
}

