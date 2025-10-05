package com.zhlearn.cli;

import com.zhlearn.application.audio.PronunciationCandidate;
import com.zhlearn.cli.audio.PrePlayback;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.infrastructure.audio.AudioPaths;
import com.zhlearn.infrastructure.audio.AudioNormalizer;
import com.zhlearn.infrastructure.audio.AudioCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PrePlaybackTest {
    private Path tmpHome;
    private PrePlayback prePlayback;
    private AudioPaths audioPaths;

    @BeforeEach
    void setup() throws Exception {
        tmpHome = Files.createTempDirectory("zhlearn-cli-test-home");
        System.setProperty("zhlearn.home", tmpHome.toString());

        audioPaths = new AudioPaths();
        AudioNormalizer audioNormalizer = new AudioNormalizer();
        AudioCache audioCache = new AudioCache(audioPaths, audioNormalizer);
        prePlayback = new PrePlayback(audioCache, audioPaths);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("zhlearn.home");
        try { Files.walk(tmpHome).sorted(Comparator.reverseOrder()).forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignored) {} }); } catch (Exception ignored) {}
    }

    @Test
    void preprocessesToCachedNormalizedPath() throws Exception {
        Path src = Files.createTempFile("src-", ".mp3");
        Files.write(src, new byte[]{0,1,2,3});
        PronunciationCandidate in = new PronunciationCandidate("test-provider", src);

        List<PronunciationCandidate> out = prePlayback.preprocessCandidates(new Hanzi("学习"), new Pinyin("xuéxí"), List.of(in));
        assertThat(out).hasSize(1);
        PronunciationCandidate c = out.get(0);
        assertThat(c.file()).exists();
        assertThat(c.file().toString()).contains(audioPaths.audioDir().toString());
        assertThat(c.file().getFileName().toString()).endsWith(".mp3");
    }

    @Test
    void keepsExistingAnkiPronunciationWithoutCaching() throws Exception {
        Path ankiFile = tmpHome.resolve("collection.media").resolve("anki-existing.mp3");
        Files.createDirectories(ankiFile.getParent());
        Files.write(ankiFile, new byte[]{5,6,7});

        PronunciationCandidate in = new PronunciationCandidate("anki", ankiFile);

        List<PronunciationCandidate> out = prePlayback.preprocessCandidates(new Hanzi("学习"), new Pinyin("xuéxí"), List.of(in));
        assertThat(out).hasSize(1);
        PronunciationCandidate result = out.get(0);
        assertThat(result.file()).isEqualTo(ankiFile.toAbsolutePath());

        try (Stream<Path> files = Files.walk(audioPaths.audioDir())) {
            assertThat(files.filter(Files::isRegularFile).toList()).isEmpty();
        }
    }

    @Test
    void reusesAlreadyCachedFilesWithoutRenaming() throws Exception {
        Path providerDir = audioPaths.audioDir().resolve("forvo");
        Files.createDirectories(providerDir);
        Path cached = providerDir.resolve("forvo_学习_UserOne_ABCD123456.mp3");
        Files.write(cached, new byte[]{9,9,9});

        PronunciationCandidate in = new PronunciationCandidate(
            "forvo",
            cached
        );

        List<PronunciationCandidate> out = prePlayback.preprocessCandidates(new Hanzi("学习"), new Pinyin("xuéxí"), List.of(in));
        assertThat(out).hasSize(1);
        PronunciationCandidate candidate = out.get(0);
        assertThat(candidate.file()).isEqualTo(cached.toAbsolutePath());
    }
}
