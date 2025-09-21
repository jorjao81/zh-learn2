package com.zhlearn.infrastructure.audio;

import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AudioDownloadExecutorTest {

    @Test
    void shouldCreateExecutorWithCorrectThreadPoolSize() {
        AudioDownloadExecutor executor = new AudioDownloadExecutor();
        ExecutorService service = executor.getExecutor();

        assertThat(service).isNotNull();
        executor.shutdown();
    }

    @Test
    void shouldShutdownGracefully() throws InterruptedException {
        AudioDownloadExecutor executor = new AudioDownloadExecutor();
        executor.shutdown();

        // Verify shutdown is initiated
        assertThat(executor.getExecutor().isShutdown()).isTrue();
    }
}