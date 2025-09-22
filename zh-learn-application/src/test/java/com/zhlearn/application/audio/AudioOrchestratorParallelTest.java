package com.zhlearn.application.audio;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.AudioProvider;
import com.zhlearn.infrastructure.audio.AudioDownloadExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class AudioOrchestratorParallelTest {

    @Test
    @Timeout(10)
    void shouldExecuteProvidersInParallel() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);

        // Create slow providers to test parallel execution
        AudioProvider slowProvider1 = new SlowAudioProvider("slow1", callCount, 1000);
        AudioProvider slowProvider2 = new SlowAudioProvider("slow2", callCount, 1000);

        AudioDownloadExecutor executor = new AudioDownloadExecutor();
        AudioOrchestrator orchestrator = new AudioOrchestrator(
            List.of(slowProvider1, slowProvider2),
            executor
        );

        Hanzi word = new Hanzi("测试");
        Pinyin pinyin = new Pinyin("cèshì");

        long startTime = System.currentTimeMillis();
        List<PronunciationCandidate> candidates = orchestrator.candidatesFor(word, pinyin);
        long endTime = System.currentTimeMillis();

        // Should complete in roughly 1 second (parallel execution)
        // rather than 2 seconds (sequential execution)
        assertThat(endTime - startTime).isLessThan(1500);

        // Both providers should have been called
        assertThat(callCount.get()).isEqualTo(2);

        // Should have candidates from both providers
        assertThat(candidates).hasSize(2);

        executor.shutdown();
    }

    private static class SlowAudioProvider implements AudioProvider {
        private final String name;
        private final AtomicInteger callCount;
        private final long delayMs;

        SlowAudioProvider(String name, AtomicInteger callCount, long delayMs) {
            this.name = name;
            this.callCount = callCount;
            this.delayMs = delayMs;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return "Slow provider for testing";
        }

        @Override
        public ProviderType getType() {
            return ProviderType.DICTIONARY;
        }

        @Override
        public Optional<Path> getPronunciation(Hanzi word, Pinyin pinyin) {
            callCount.incrementAndGet();
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return Optional.of(Path.of("/tmp/test_" + name + ".mp3"));
        }

        @Override
        public List<PronunciationDescription> getPronunciationsWithDescriptions(Hanzi word, Pinyin pinyin) {
            callCount.incrementAndGet();
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return List.of(new PronunciationDescription(
                Path.of("/tmp/test_" + name + ".mp3"),
                name + " pronunciation"
            ));
        }

        @Override
        public List<Path> getPronunciations(Hanzi word, Pinyin pinyin) {
            return getPronunciationsWithDescriptions(word, pinyin).stream()
                .map(PronunciationDescription::path)
                .toList();
        }
    }
}