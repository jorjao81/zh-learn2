package com.zhlearn.infrastructure.audio;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AudioPaths {
    private AudioPaths() {}

    public static Path homeDir() {
        String override = System.getProperty("zhlearn.home");
        if (override == null || override.isBlank()) override = System.getenv("ZHLEARN_HOME");
        Path base = (override == null || override.isBlank())
                ? Path.of(System.getProperty("user.home"), ".zh-learn")
                : Path.of(override);
        try {
            Files.createDirectories(base);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create zh-learn home directory at " + base, e);
        }
        return base.toAbsolutePath();
    }

    public static Path audioDir() {
        Path dir = homeDir().resolve("audio");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create zh-learn audio directory at " + dir, e);
        }
        return dir.toAbsolutePath();
    }

    public static String sanitize(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.isEmpty()) return "_";
        return t.replaceAll("[^\\p{L}\\p{N}_-]", "_");
    }
}
