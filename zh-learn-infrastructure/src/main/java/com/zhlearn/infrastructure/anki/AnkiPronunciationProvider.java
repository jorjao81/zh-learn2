package com.zhlearn.infrastructure.anki;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.AudioProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Audio provider that reuses pronunciations already present in the user's
 * Anki collection export (Chinese.txt). It finds entries by exact pinyin match
 * and returns the pronunciation file if non-empty.
 */
public class AnkiPronunciationProvider implements AudioProvider {

    private static final Logger log = LoggerFactory.getLogger(AnkiPronunciationProvider.class);
    private static final String NAME = "anki";
    private static final String DESCRIPTION = "Reuses existing pronunciations from local Anki collection (Chinese.txt) by exact pinyin match.";

    private final Map<String, Path> pinyinToPronunciation;

    public AnkiPronunciationProvider() {
        this.pinyinToPronunciation = new HashMap<>();
        AnkiNoteParser parser = new AnkiNoteParser();
        Path exportPath = defaultExportPath();
        try {
            if (!Files.exists(exportPath)) {
                log.warn("Anki export not found. Expected at: {}", exportPath.toAbsolutePath());
                Path parent = exportPath.getParent();
                if (parent != null) {
                    log.warn("Hint: Export your Anki collection as a TSV named 'Chinese.txt' to {}", parent.toAbsolutePath());
                }
                return; // graceful: provider remains available but has no entries
            }
            List<AnkiNote> notes = parser.parseFile(exportPath);
            index(notes);
        } catch (IOException e) {
            log.warn("Failed to parse Anki export at {}: {}", exportPath.toAbsolutePath(), e.getMessage());
        }
    }

    private static Path defaultExportPath() {
        String home = System.getProperty("user.home");
        return Path.of(home, ".zh-learn", "Chinese.txt");
    }

    public AnkiPronunciationProvider(Path collectionPath, AnkiNoteParser parser) {
        this.pinyinToPronunciation = new HashMap<>();
        try {
            List<AnkiNote> notes = parser.parseFile(collectionPath);
            index(notes);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse Anki collection: " + e.getMessage(), e);
        }
    }

    public AnkiPronunciationProvider(List<AnkiNote> notes) {
        this.pinyinToPronunciation = new HashMap<>();
        index(notes);
    }

    public static AnkiPronunciationProvider fromString(String tsvContent) {
        try {
            Reader r = new StringReader(tsvContent);
            List<AnkiNote> notes = new AnkiNoteParser().parseFromReader(r);
            return new AnkiPronunciationProvider(notes);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse content: " + e.getMessage(), e);
        }
    }

    private void index(List<AnkiNote> notes) {
        for (AnkiNote n : notes) {
            String p = normalizePinyin(safe(n.pinyin()));
            String pron = safe(n.pronunciation());
            if (!p.isEmpty() && !pron.isEmpty()) {
                resolvePronunciationPath(pron).ifPresent(path ->
                    pinyinToPronunciation.putIfAbsent(p, path)
                );
            }
        }
    }

    @Override
    public String getName() { return NAME; }

    @Override
    public String getDescription() { return DESCRIPTION; }

    @Override
    public ProviderType getType() { return ProviderType.DICTIONARY; }

    @Override
    public Optional<Path> getPronunciation(Hanzi word, Pinyin pinyin) {
        if (pinyin == null || pinyin.pinyin() == null) return Optional.empty();
        String key = normalizePinyin(pinyin.pinyin().trim());
        if (key.isEmpty()) return Optional.empty();
        Path result = pinyinToPronunciation.get(key);
        return Optional.ofNullable(result);
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private static String normalizePinyin(String s) {
        if (s == null) return "";
        String t = s.trim();
        return Normalizer.normalize(t, Normalizer.Form.NFC);
    }

    private static Optional<Path> resolvePronunciationPath(String soundNotation) {
        Optional<String> fileNameOpt = extractFileName(soundNotation);
        if (fileNameOpt.isEmpty()) {
            return Optional.empty();
        }
        String fileName = fileNameOpt.get();
        Path rawPath = Path.of(fileName);
        if (rawPath.isAbsolute()) {
            return Optional.of(rawPath.toAbsolutePath());
        }
        Path base = ankiMediaDir();
        if (base != null) {
            Path resolved = base.resolve(fileName).toAbsolutePath();
            return Optional.of(resolved);
        }
        return Optional.of(rawPath.toAbsolutePath());
    }

    private static Optional<String> extractFileName(String soundNotation) {
        int colon = soundNotation.indexOf(':');
        int close = soundNotation.indexOf(']');
        if (colon >= 0 && close > colon) {
            return Optional.of(soundNotation.substring(colon + 1, close));
        }
        if (soundNotation.endsWith("]")) {
            return Optional.empty();
        }
        return Optional.of(soundNotation.trim());
    }

    private static Path ankiMediaDir() {
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
