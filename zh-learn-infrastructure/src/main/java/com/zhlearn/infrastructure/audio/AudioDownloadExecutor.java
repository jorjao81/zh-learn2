package com.zhlearn.infrastructure.audio;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioDownloadExecutor {
    private static final int THREAD_POOL_SIZE = 32; // Increased to handle nested parallelism
    private final ExecutorService executor;

    public AudioDownloadExecutor() {
        this.executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void shutdown() {
        executor.shutdown();
    }
}
