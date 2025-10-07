package com.zhlearn.infrastructure.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSystemCacheTest {

    @TempDir Path tempDir;

    @Test
    void shouldReturnEmptyWhenKeyNotFound() {
        FileSystemCache cache = new FileSystemCache(tempDir, 3600);

        Optional<String> result = cache.get("nonexistent-key");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldStoreAndRetrieveValue() {
        FileSystemCache cache = new FileSystemCache(tempDir, 3600);
        String key = "test-key";
        String value = "test-response";

        cache.put(key, value);
        Optional<String> result = cache.get(key);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(value);
    }

    @Test
    void shouldHandleMultipleKeys() {
        FileSystemCache cache = new FileSystemCache(tempDir, 3600);

        cache.put("key1", "value1");
        cache.put("key2", "value2");

        assertThat(cache.get("key1")).hasValue("value1");
        assertThat(cache.get("key2")).hasValue("value2");
    }

    @Test
    void shouldExpireOldEntries() throws InterruptedException {
        FileSystemCache cache = new FileSystemCache(tempDir, 1); // 1 second TTL
        String key = "test-key";
        String value = "test-response";

        cache.put(key, value);
        assertThat(cache.get(key)).hasValue(value);

        Thread.sleep(1100); // Wait for expiration

        Optional<String> result = cache.get(key);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldCreateCacheDirectoryStructure() {
        FileSystemCache cache = new FileSystemCache(tempDir, 3600);
        String key = "abcdef1234567890";

        cache.put(key, "test");

        // Verify directory structure is created
        Path expectedDir = tempDir.resolve("responses").resolve("ab");
        assertThat(expectedDir).exists();

        Path expectedFile = expectedDir.resolve(key + ".cache");
        assertThat(expectedFile).exists();
    }

    @Test
    void defaultConstructorThrowsWhenUserHomeMissing() {
        String originalUserHome = System.getProperty("user.home");
        try {
            System.clearProperty("user.home");

            assertThatThrownBy(FileSystemCache::new)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("user.home system property must be set");
        } finally {
            if (originalUserHome == null) {
                System.clearProperty("user.home");
            } else {
                System.setProperty("user.home", originalUserHome);
            }
        }
    }

    @Test
    void defaultConstructorCreatesCacheUnderZhLearnDirectory(@TempDir Path tempHome)
            throws Exception {
        String originalUserHome = System.getProperty("user.home");
        try {
            System.setProperty("user.home", tempHome.toString());

            FileSystemCache cache = new FileSystemCache();

            Path expectedCacheDir = tempHome.resolve(".zh-learn").resolve("cache");
            Path expectedResponsesDir = expectedCacheDir.resolve("responses");
            assertThat(expectedResponsesDir).exists();

            Field cacheDirectoryField = FileSystemCache.class.getDeclaredField("cacheDirectory");
            cacheDirectoryField.setAccessible(true);
            Path actualCacheDir = (Path) cacheDirectoryField.get(cache);
            assertThat(actualCacheDir).isEqualTo(expectedCacheDir);
        } finally {
            if (originalUserHome == null) {
                System.clearProperty("user.home");
            } else {
                System.setProperty("user.home", originalUserHome);
            }
        }
    }
}
