package com.zhlearn.infrastructure.audio;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AudioCacheTest {
    private Path tmpHome;
    private AudioCache audioCache;
    private AudioPaths audioPaths;

    @BeforeEach
    void setup() throws Exception {
        tmpHome = Files.createTempDirectory("zhlearn-test-home");
        System.setProperty("zhlearn.home", tmpHome.toString());
        // Disable ffmpeg to make test stable in CI
        // Note: we cannot set env vars reliably in JVM; AudioNormalizer checks env at runtime.
        // We'll rely on copy fallback by ensuring ffmpeg is likely absent, but create mp3-like
        // file.
        // To be robust, write a tiny file and let fallback copy handle it if ffmpeg not present.

        audioPaths = new AudioPaths();
        AudioNormalizer audioNormalizer = new AudioNormalizer();
        audioCache = new AudioCache(audioPaths, audioNormalizer);
    }

    @AfterEach
    void tearDown() throws Exception {
        System.clearProperty("zhlearn.home");
        // best-effort cleanup
        try {
            Files.walk(tmpHome)
                    .sorted(Comparator.reverseOrder())
                    .forEach(
                            p -> {
                                try {
                                    Files.deleteIfExists(p);
                                } catch (Exception ignored) {
                                }
                            });
        } catch (Exception ignored) {
        }
    }

    @Test
    void cachesAndReusesNormalizedFile() throws Exception {
        Path src = Files.createTempFile("src-", ".mp3");
        Files.write(src, new byte[] {0, 1, 2, 3, 4, 5, 6});

        Path out1 =
                audioCache.ensureCachedNormalized(
                        src, "forvo", "学习", "xuéxí", "http://example.com/a.mp3");
        assertThat(out1).exists();
        assertThat(out1.getParent())
                .isEqualTo(audioPaths.audioDir().resolve("forvo").toAbsolutePath());

        long size1 = Files.size(out1);

        // Second call should return same path and not fail
        Path out2 =
                audioCache.ensureCachedNormalized(
                        src, "forvo", "学习", "xuéxí", "http://example.com/a.mp3");
        assertThat(out2).exists();
        assertThat(out2).isEqualTo(out1);
        assertThat(Files.size(out2)).isEqualTo(size1);
    }
}
