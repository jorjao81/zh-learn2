package com.zhlearn.cli;

import com.zhlearn.application.audio.PronunciationCandidate;
import com.zhlearn.cli.audio.PrePlayback;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.infrastructure.audio.AudioPaths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrePlaybackTest {
    private Path tmpHome;

    @BeforeEach
    void setup() throws Exception {
        tmpHome = Files.createTempDirectory("zhlearn-cli-test-home");
        System.setProperty("zhlearn.home", tmpHome.toString());
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("zhlearn.home");
        try { java.nio.file.Files.walk(tmpHome).sorted(java.util.Comparator.reverseOrder()).forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignored) {} }); } catch (Exception ignored) {}
    }

    @Test
    void preprocessesToCachedNormalizedPath() throws Exception {
        Path src = Files.createTempFile("src-", ".mp3");
        Files.write(src, new byte[]{0,1,2,3});
        PronunciationCandidate in = new PronunciationCandidate("test-provider", "[sound:src.mp3]", src);

        List<PronunciationCandidate> out = PrePlayback.preprocessCandidates(new Hanzi("学习"), new Pinyin("xuéxí"), List.of(in));
        assertThat(out).hasSize(1);
        PronunciationCandidate c = out.get(0);
        assertThat(c.file()).exists();
        assertThat(c.file().toString()).contains(AudioPaths.audioDir().toString());
        assertThat(c.soundNotation()).startsWith("[sound:");
        assertThat(c.soundNotation()).contains(".mp3]");
    }
}

