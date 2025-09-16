package com.zhlearn.cli.audio;

import com.zhlearn.application.audio.AudioPlayer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SystemAudioPlayer implements AudioPlayer {
    private Process current;

    @Override
    public void play(Path file) {
        stop();
        if (file == null || !Files.exists(file)) {
            // Try Anki media directory if configured
            Path fromAnki = tryFromAnkiMedia(file);
            if (fromAnki != null) {
                file = fromAnki;
            }

            // Try to resolve from CLI module resources (fixtures/audio/<name>)
            Path resolved = tryExtractFromResources(file);
            if (resolved == null) {
                throw new IllegalStateException("Audio file not found: " + (file == null ? "(null)" : file.toAbsolutePath()));
            }
            file = resolved;
        }
        String os = System.getProperty("os.name", "").toLowerCase();
        ProcessBuilder pb;
        if (os.contains("mac")) {
            pb = new ProcessBuilder("afplay", file.toAbsolutePath().toString());
        } else {
            pb = new ProcessBuilder("ffplay", "-nodisp", "-autoexit", "-loglevel", "error", file.toAbsolutePath().toString());
        }
        try {
            current = pb.start();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to launch audio player process", e);
        }
    }

    @Override
    public void stop() {
        if (current != null && current.isAlive()) {
            current.destroy();
            current = null;
        }
    }

    private Path tryExtractFromResources(Path file) {
        String name = (file == null) ? null : file.getFileName().toString();
        if (name == null || name.isBlank()) return null;
        String resourcePath = "/fixtures/audio/" + name;
        try (var in = SystemAudioPlayer.class.getResourceAsStream(resourcePath)) {
            if (in == null) return null;
            Path out = Files.createTempFile("zhlearn-", "-" + name);
            Files.copy(in, out, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            out.toFile().deleteOnExit();
            return out;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to extract bundled audio resource '" + name + "'", e);
        }
    }

    private Path tryFromAnkiMedia(Path file) {
        String name = (file == null) ? null : file.getFileName().toString();
        if (name == null || name.isBlank()) return null;
        String cfg = System.getProperty("anki.media.dir");
        if (cfg == null || cfg.isBlank()) cfg = System.getProperty("zhlearn.anki.media.dir");
        if (cfg == null || cfg.isBlank()) cfg = System.getenv("ANKI_MEDIA_DIR");
        if (cfg == null || cfg.isBlank()) cfg = System.getenv("ZHLEARN_ANKI_MEDIA_DIR");
        if (cfg == null || cfg.isBlank()) return null;
        Path dir = Path.of(cfg);
        Path candidate = dir.resolve(name);
        if (!Files.exists(candidate)) {
            throw new IllegalStateException("Configured Anki media file not found: " + candidate.toAbsolutePath());
        }
        return candidate.toAbsolutePath();
    }
}
