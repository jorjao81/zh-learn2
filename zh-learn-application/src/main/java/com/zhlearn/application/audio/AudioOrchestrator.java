package com.zhlearn.application.audio;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.provider.AudioProvider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AudioOrchestrator {

    private final List<AudioProvider> audioProviders;

    public AudioOrchestrator(List<AudioProvider> audioProviders) {
        this.audioProviders = audioProviders;
    }

    public List<PronunciationCandidate> candidatesFor(Hanzi word, Pinyin pinyin) {
        List<PronunciationCandidate> list = new ArrayList<>();
        for (AudioProvider provider : audioProviders) {
            List<Path> paths = provider.getPronunciations(word, pinyin);
            for (Path path : paths) {
                if (path == null) continue;
                list.add(new PronunciationCandidate(
                    provider.getName(),
                    validateAbsolutePath(provider.getName(), path)
                ));
            }
        }
        return list;
    }

    static Path validateAbsolutePath(String providerName, Path provided) {
        if (provided == null) {
            throw new IllegalArgumentException("Audio provider returned null path");
        }

        if (!provided.isAbsolute()) {
            throw new IllegalStateException("Audio provider '" + providerName + "' returned non-absolute path: " + provided);
        }
        return provided.toAbsolutePath();
    }

}
