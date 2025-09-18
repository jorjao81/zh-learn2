package com.zhlearn.cli.audio;

import com.zhlearn.application.audio.PronunciationCandidate;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.infrastructure.audio.AudioCache;
import com.zhlearn.infrastructure.audio.AudioPaths;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PrePlayback {
    private PrePlayback() {}

    public static List<PronunciationCandidate> preprocessCandidates(Hanzi word, Pinyin pinyin, List<PronunciationCandidate> list) {
        List<PronunciationCandidate> out = new ArrayList<>();
        Path audioBase = AudioPaths.audioDir();
        for (PronunciationCandidate c : list) {
            Path absolute = c.file().toAbsolutePath();
            if (shouldBypassCache(c.label())) {
                out.add(new PronunciationCandidate(c.label(), absolute));
                continue;
            }
            if (isAlreadyCached(absolute, audioBase)) {
                out.add(new PronunciationCandidate(c.label(), absolute));
                continue;
            }
            try {
                Path normalized = AudioCache.ensureCachedNormalized(
                    absolute,
                    c.label(),
                    word.characters(),
                    pinyin.pinyin(),
                    null
                );
                out.add(new PronunciationCandidate(c.label(), normalized));
            } catch (IOException e) {
                throw new RuntimeException("Failed to normalize audio candidate '" + c.label() + "'", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Normalization interrupted for candidate '" + c.label() + "'", e);
            }
        }
        return out;
    }

    private static boolean shouldBypassCache(String providerLabel) {
        return "anki".equals(providerLabel);
    }

    private static boolean isAlreadyCached(Path file, Path audioBase) {
        Path normalizedFile = file.toAbsolutePath().normalize();
        Path normalizedBase = audioBase.toAbsolutePath().normalize();
        return normalizedFile.startsWith(normalizedBase);
    }
}
