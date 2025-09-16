package com.zhlearn.cli.audio;

import com.zhlearn.application.audio.PronunciationCandidate;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.infrastructure.audio.AudioCache;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PrePlayback {
    private PrePlayback() {}

    public static List<PronunciationCandidate> preprocessCandidates(Hanzi word, Pinyin pinyin, List<PronunciationCandidate> list) {
        List<PronunciationCandidate> out = new ArrayList<>();
        for (PronunciationCandidate c : list) {
            try {
                Path normalized = AudioCache.ensureCachedNormalized(
                    c.file(),
                    c.label(),
                    word.characters(),
                    pinyin.pinyin(),
                    null
                );
                String sound = "[sound:" + normalized.toAbsolutePath() + "]";
                out.add(new PronunciationCandidate(c.label(), sound, normalized));
            } catch (IOException e) {
                throw new RuntimeException("Failed to normalize audio candidate '" + c.label() + "'", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Normalization interrupted for candidate '" + c.label() + "'", e);
            }
        }
        return out;
    }
}
