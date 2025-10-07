package com.zhlearn.infrastructure.cache;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemCache {
    private static final Logger log = LoggerFactory.getLogger(FileSystemCache.class);
    private static final long DEFAULT_TTL_SECONDS = 7 * 24 * 60 * 60; // 1 week

    private final Path cacheDirectory;
    private final long ttlSeconds;

    public FileSystemCache() {
        this(resolveDefaultCacheDirectory(), DEFAULT_TTL_SECONDS);
    }

    private static Path resolveDefaultCacheDirectory() {
        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isBlank()) {
            throw new IllegalStateException(
                    "user.home system property must be set to resolve the cache directory");
        }
        return Path.of(userHome, ".zh-learn", "cache");
    }

    public FileSystemCache(Path cacheDirectory, long ttlSeconds) {
        this.cacheDirectory = cacheDirectory;
        this.ttlSeconds = ttlSeconds;
        initializeCacheDirectory();
    }

    private void initializeCacheDirectory() {
        try {
            Files.createDirectories(cacheDirectory.resolve("responses"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create cache directory: " + cacheDirectory, e);
        }
    }

    public Optional<String> get(String cacheKey) {
        Path cacheFile = getCacheFilePath(cacheKey);

        if (!Files.exists(cacheFile)) {
            return Optional.empty();
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(cacheFile))) {
            CacheEntry entry = (CacheEntry) ois.readObject();

            if (entry.isExpired(Instant.now(), ttlSeconds)) {
                log.debug("Cache entry expired for key: {}", cacheKey);
                deleteFile(cacheFile);
                return Optional.empty();
            }

            log.debug("Cache hit for key: {}", cacheKey);
            return Optional.of(entry.getResponse());

        } catch (IOException | ClassNotFoundException e) {
            log.warn("Failed to read cache entry for key {}: {}", cacheKey, e.getMessage());
            deleteFile(cacheFile);
            return Optional.empty();
        }
    }

    public void put(String cacheKey, String response) {
        Path cacheFile = getCacheFilePath(cacheKey);

        try {
            Files.createDirectories(cacheFile.getParent());

            CacheEntry entry = new CacheEntry(response, Instant.now());

            try (ObjectOutputStream oos =
                    new ObjectOutputStream(Files.newOutputStream(cacheFile))) {
                oos.writeObject(entry);
            }

            log.debug("Cache entry stored for key: {}", cacheKey);

        } catch (IOException e) {
            log.warn("Failed to store cache entry for key {}: {}", cacheKey, e.getMessage());
        }
    }

    private Path getCacheFilePath(String cacheKey) {
        String prefix = cacheKey.substring(0, Math.min(2, cacheKey.length()));
        return cacheDirectory.resolve("responses").resolve(prefix).resolve(cacheKey + ".cache");
    }

    private void deleteFile(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Failed to delete cache file {}: {}", file, e.getMessage());
        }
    }
}
