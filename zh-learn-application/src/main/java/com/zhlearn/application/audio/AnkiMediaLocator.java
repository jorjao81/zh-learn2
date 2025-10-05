package com.zhlearn.application.audio;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Resolves the Anki media directory based on system properties, environment variables,
 * or reasonable platform defaults.
 */
public class AnkiMediaLocator {
    public AnkiMediaLocator() {
    }

    /**
     * Locate the Anki media directory if configured or discoverable.
     *
     * @return Optional containing the absolute path to the Anki media directory.
     * @throws IllegalStateException if a configured media directory path exists but is not a directory.
     */
    public Optional<Path> locate() {
        String configured = firstNonBlank(
            System.getProperty("zhlearn.anki.media.dir"),
            System.getProperty("zhlearn.anki.mediaDir"),
            System.getProperty("anki.media.dir"),
            System.getenv("ZHLEARN_ANKI_MEDIA_DIR"),
            System.getenv("ANKI_MEDIA_DIR")
        );

        if (configured != null) {
            Path dir = Path.of(configured).toAbsolutePath();
            if (!Files.isDirectory(dir)) {
                throw new IllegalStateException("Configured Anki media directory '" + dir + "' is not a directory");
            }
            return Optional.of(dir);
        }

        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac")) {
            String home = System.getProperty("user.home");
            Path macDefault = Path.of(home, "Library", "Application Support", "Anki2", "User 1", "collection.media");
            if (Files.isDirectory(macDefault)) {
                return Optional.of(macDefault.toAbsolutePath());
            }
        }

        return Optional.empty();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null) {
                String trimmed = value.trim();
                if (!trimmed.isEmpty()) {
                    return trimmed;
                }
            }
        }
        return null;
    }
}
