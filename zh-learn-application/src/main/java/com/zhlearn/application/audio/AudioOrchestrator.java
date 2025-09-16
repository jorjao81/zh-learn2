package com.zhlearn.application.audio;

import com.zhlearn.application.service.ProviderRegistry;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.provider.AudioProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
            java.util.List<String> sounds = provider.getPronunciations(word, pinyin);
            for (String s : sounds) {
                if (s == null || s.isBlank()) continue;
                list.add(new PronunciationCandidate(
                    provider.getName(),
                    s,
                    resolvePath(extractFileName(s))
                ));
            }
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
        Path original = Path.of(fileName);
        if (Files.exists(original)) {
            return original.toAbsolutePath();
        }

        Path anki = ankiMediaDir();
        if (anki != null) {
            Path candidate = anki.resolve(fileName);
            if (Files.exists(candidate)) {
                return candidate.toAbsolutePath();
            }
        }

        String resourcePath = "/fixtures/audio/" + fileName;
        try (InputStream in = openResourceStream(resourcePath)) {
            if (in != null) {
                Path out = Files.createTempFile("zhlearn-", "-" + fileName);
                Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                out.toFile().deleteOnExit();
                return out;
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to resolve audio resource '" + fileName + "'", e);
        }

        return original;
    }

    private static InputStream openResourceStream(String resourcePath) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream in = cl != null ? cl.getResourceAsStream(resourcePath.substring(1)) : null;
        if (in == null) {
            in = AudioOrchestrator.class.getResourceAsStream(resourcePath);
        }
        return in;
    }

    private static Path ankiMediaDir() {
        // priority: system property -> env vars -> OS default (macOS)
        String v = System.getProperty("zhlearn.anki.media.dir");
        if (v == null || v.isBlank()) v = System.getProperty("zhlearn.anki.mediaDir");
        if (v == null || v.isBlank()) v = System.getProperty("anki.media.dir");
        if (v == null || v.isBlank()) v = System.getenv("ZHLEARN_ANKI_MEDIA_DIR");
        if (v == null || v.isBlank()) v = System.getenv("ANKI_MEDIA_DIR");

        if (v != null && !v.isBlank()) {
            Path dir = Path.of(v).toAbsolutePath();
            if (!Files.isDirectory(dir)) {
                throw new IllegalStateException("Configured Anki media directory '" + dir + "' is not a directory");
            }
            return dir;
        }

        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac")) {
            String home = System.getProperty("user.home");
            Path macDefault = Path.of(home, "Library", "Application Support", "Anki2", "User 1", "collection.media");
            if (Files.isDirectory(macDefault)) return macDefault.toAbsolutePath();
        }

        return null;
    }
}
