package com.zhlearn.infrastructure.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Optional;

class FileSystemCacheTest {

    @TempDir
    Path tempDir;

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
}