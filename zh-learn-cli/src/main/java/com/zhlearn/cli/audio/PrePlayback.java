package com.zhlearn.cli.audio;

import com.zhlearn.application.audio.PronunciationCandidate;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.infrastructure.audio.AudioCache;

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
            } catch (Exception e) {
                // If anything fails, keep the original candidate so the user can still try to play it
                out.add(c);
            }
        }
        return out;
    }
}

