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
            List<Path> paths = provider.getPronunciations(word, pinyin);
            for (Path path : paths) {
                if (path == null) continue;
                list.add(new PronunciationCandidate(
                    provider.getName(),
                    resolvePath(provider.getName(), path)
                ));
            }
        }
        return list;
    }

    static Path resolvePath(String providerName, Path provided) {
        if (provided == null) {
            throw new IllegalArgumentException("Audio provider returned null path");
        }

        Path candidate = provided;
        if (candidate.isAbsolute() && Files.exists(candidate)) {
            return candidate.toAbsolutePath();
        }

        String fileName = candidate.getFileName() != null ? candidate.getFileName().toString() : candidate.toString();

        if (!candidate.isAbsolute()) {
            Path providerCache = audioDir().resolve(providerName).resolve(fileName);
            if (Files.exists(providerCache)) {
                return providerCache.toAbsolutePath();
            }

            Path sharedCache = audioDir().resolve(fileName);
            if (Files.exists(sharedCache)) {
                return sharedCache.toAbsolutePath();
            }

            Path relative = Path.of(candidate.toString());
            if (Files.exists(relative)) {
                return relative.toAbsolutePath();
            }
        }

        Path anki = ankiMediaDir();
        if (anki != null) {
            Path fromAnki = anki.resolve(fileName);
            if (Files.exists(fromAnki)) {
                return fromAnki.toAbsolutePath();
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

        return candidate.isAbsolute() ? candidate.toAbsolutePath() : candidate.toAbsolutePath();
    }

    private static Path audioDir() {
        Path base = audioHome();
        Path audio = base.resolve("audio");
        try {
            Files.createDirectories(audio);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to prepare audio directory at " + audio, e);
        }
        return audio;
    }

    private static Path audioHome() {
        String override = System.getProperty("zhlearn.home");
        if (override == null || override.isBlank()) {
            override = System.getenv("ZHLEARN_HOME");
        }
        Path base = (override == null || override.isBlank())
            ? Path.of(System.getProperty("user.home"), ".zh-learn")
            : Path.of(override);
        try {
            Files.createDirectories(base);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to prepare zh-learn home at " + base, e);
        }
        return base.toAbsolutePath();
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
