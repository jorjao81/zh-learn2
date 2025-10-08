package com.zhlearn.cli.audio;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.zhlearn.application.audio.PronunciationCandidate;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.infrastructure.audio.AudioCache;
import com.zhlearn.infrastructure.audio.AudioPaths;

public class PrePlayback {
    private final AudioCache audioCache;
    private final AudioPaths audioPaths;

    public PrePlayback(AudioCache audioCache, AudioPaths audioPaths) {
        this.audioCache = audioCache;
        this.audioPaths = audioPaths;
    }

    public List<PronunciationCandidate> preprocessCandidates(
            Hanzi word, Pinyin pinyin, List<PronunciationCandidate> list) {
        List<PronunciationCandidate> out = new ArrayList<>();
        Path audioBase = audioPaths.audioDir();
        for (PronunciationCandidate c : list) {
            Path absolute = c.file().toAbsolutePath();
            if (shouldBypassCache(c.label())) {
                out.add(new PronunciationCandidate(c.label(), absolute, c.description()));
                continue;
            }
            if (isAlreadyCached(absolute, audioBase)) {
                out.add(new PronunciationCandidate(c.label(), absolute, c.description()));
                continue;
            }
            try {
                Path normalized =
                        audioCache.ensureCachedNormalized(
                                absolute, c.label(), word.characters(), pinyin.pinyin(), null);
                out.add(new PronunciationCandidate(c.label(), normalized, c.description()));
            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to normalize audio candidate '" + c.label() + "'", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(
                        "Normalization interrupted for candidate '" + c.label() + "'", e);
            }
        }
        return out;
    }

    private boolean shouldBypassCache(String providerLabel) {
        return "anki".equals(providerLabel);
    }

    private boolean isAlreadyCached(Path file, Path audioBase) {
        Path normalizedFile = file.toAbsolutePath().normalize();
        Path normalizedBase = audioBase.toAbsolutePath().normalize();
        return normalizedFile.startsWith(normalizedBase);
    }
}
