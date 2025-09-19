package com.zhlearn.application.audio;

import com.zhlearn.application.service.ProviderRegistry;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.provider.AudioProvider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AudioOrchestrator {

    private final ProviderRegistry registry;

    public AudioOrchestrator(ProviderRegistry registry) {
        this.registry = registry;
    }

    public List<PronunciationCandidate> candidatesFor(Hanzi word, Pinyin pinyin) {
        List<PronunciationCandidate> list = new ArrayList<>();
        for (String providerName : registry.getAvailableAudioProviders()) {
            Optional<AudioProvider> providerOpt = registry.getAudioProvider(providerName);
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
