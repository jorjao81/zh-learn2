package com.zhlearn.application.audio;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.provider.AudioProvider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AudioOrchestrator {

    private final Map<String, AudioProvider> audioProviders;

    public AudioOrchestrator(Map<String, AudioProvider> audioProviders) {
        this.audioProviders = audioProviders;
    }

    public List<PronunciationCandidate> candidatesFor(Hanzi word, Pinyin pinyin) {
        List<PronunciationCandidate> list = new ArrayList<>();
        for (String providerName : orderedProviderNames()) {
            Optional<AudioProvider> providerOpt = Optional.ofNullable(audioProviders.get(providerName));
            if (providerOpt.isEmpty()) continue;
            AudioProvider provider = providerOpt.get();
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

    private LinkedHashSet<String> orderedProviderNames() {
        List<String> names = new ArrayList<>(audioProviders.keySet());
        names.sort((a, b) -> {
            if (a.equals("anki") && !b.equals("anki")) return -1;
            if (b.equals("anki") && !a.equals("anki")) return 1;
            return a.compareTo(b);
        });
        return new LinkedHashSet<>(names);
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
