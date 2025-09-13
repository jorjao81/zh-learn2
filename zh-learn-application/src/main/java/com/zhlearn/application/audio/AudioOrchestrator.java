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
            Optional<String> sound = provider.getPronunciation(word, pinyin);
            sound.ifPresent(s -> list.add(new PronunciationCandidate(
                provider.getName(),
                s,
                resolvePath(extractFileName(s))
            )));
        }
        return list;
    }

    static String extractFileName(String soundNotation) {
        // Expect format: [sound:filename.mp3]
        if (soundNotation == null) return "";
        int colon = soundNotation.indexOf(':');
        int end = soundNotation.indexOf(']');
        if (colon != -1 && end != -1 && end > colon + 1) {
            return soundNotation.substring(colon + 1, end);
        }
        return soundNotation;
    }

    static Path resolvePath(String fileName) {
        try {
            Path p = Path.of(fileName);
            if (java.nio.file.Files.exists(p)) {
                return p.toAbsolutePath();
            }
            // Try classpath resource fallback (e.g., fixtures)
            String resourcePath = "/fixtures/audio/" + fileName;
            var cl = Thread.currentThread().getContextClassLoader();
            java.io.InputStream in = cl != null ? cl.getResourceAsStream(resourcePath.substring(1)) : null;
            if (in == null) {
                in = AudioOrchestrator.class.getResourceAsStream(resourcePath);
            }
            if (in != null) {
                java.nio.file.Path out = java.nio.file.Files.createTempFile("zhlearn-", "-" + fileName);
                java.nio.file.Files.copy(in, out, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                try { in.close(); } catch (Exception ignored) {}
                out.toFile().deleteOnExit();
                return out;
            }
        } catch (Exception ignored) {
        }
        return Path.of(fileName);
    }
}
