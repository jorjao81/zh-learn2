package com.zhlearn.infrastructure.cache;

import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CachedChatModelTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldCallDelegateOnCacheMiss() {
        ChatModel delegate = mock(ChatModel.class);
        when(delegate.chat("test prompt")).thenReturn("test response");
        
        // Use a custom FileSystemCache with temp directory for testing
        CachedChatModel cachedModel = new TestCachedChatModel(delegate, "http://test.com", "test-model", 0.5, 100, tempDir);
        
        String result = cachedModel.chat("test prompt");
        
        assertThat(result).isEqualTo("test response");
        verify(delegate, times(1)).chat("test prompt");
    }

    @Test
    void shouldUseCacheOnSecondCall() {
        ChatModel delegate = mock(ChatModel.class);
        when(delegate.chat("test prompt")).thenReturn("test response");
        
        CachedChatModel cachedModel = new TestCachedChatModel(delegate, "http://test.com", "test-model", 0.5, 100, tempDir);
        
        // First call should hit the delegate
        String result1 = cachedModel.chat("test prompt");
        assertThat(result1).isEqualTo("test response");
        
        // Second call should use cache
        String result2 = cachedModel.chat("test prompt");
        assertThat(result2).isEqualTo("test response");
        
        // Delegate should only be called once
        verify(delegate, times(1)).chat("test prompt");
    }


    // Test helper class to inject custom FileSystemCache with temp directory
    private static class TestCachedChatModel extends CachedChatModel {
        public TestCachedChatModel(ChatModel delegate, String baseUrl, String modelName, Double temperature, Integer maxTokens, Path tempDir) {
            super(delegate, baseUrl, modelName, temperature, maxTokens);
            // Replace the cache with one using temp directory
            try {
                Field cacheField = CachedChatModel.class.getDeclaredField("cache");
                cacheField.setAccessible(true);
                cacheField.set(this, new FileSystemCache(tempDir, 3600));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}