package com.zhlearn.cli.audio;

import com.zhlearn.application.audio.AudioPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SystemAudioPlayer implements AudioPlayer {
    private Process current;

    @Override
    public void play(Path file) {
        stop();
        if (file == null || !Files.exists(file)) {
            // Try to resolve from CLI module resources (fixtures/audio/<name>)
            Path resolved = tryExtractFromResources(file);
            if (resolved == null) {
                System.err.println("[audio] File not found: " + (file == null ? "(null)" : file.toAbsolutePath()));
                return;
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
            // Swallow for now in minimal MVP; a real impl would surface this
            current = null;
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
        try {
            String name = (file == null) ? null : file.getFileName().toString();
            if (name == null || name.isBlank()) return null;
            String resourcePath = "/fixtures/audio/" + name;
            try (var in = SystemAudioPlayer.class.getResourceAsStream(resourcePath)) {
                if (in == null) return null;
                Path out = Files.createTempFile("zhlearn-", "-" + name);
                Files.copy(in, out, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                out.toFile().deleteOnExit();
                return out;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
