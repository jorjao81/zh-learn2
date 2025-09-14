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
import java.nio.file.Paths;
import java.util.*;
import java.text.Normalizer;

/**
 * Audio provider that reuses pronunciations already present in the user's
 * Anki collection export (Chinese.txt). It finds entries by exact pinyin match
 * and returns the pronunciation field if non-empty.
 */
public class ExistingAnkiPronunciationProvider implements AudioProvider {

    private static final Logger log = LoggerFactory.getLogger(ExistingAnkiPronunciationProvider.class);
    private static final String NAME = "existing-anki-pronunciation";
    private static final String DESCRIPTION = "Reuses existing pronunciations from local Anki collection (Chinese.txt) by exact pinyin match.";

    private final Map<String, String> pinyinToPronunciation;

    public ExistingAnkiPronunciationProvider() {
        this.pinyinToPronunciation = new HashMap<>();
        AnkiNoteParser parser = new AnkiNoteParser();
        Path defaultPath = defaultExportPath();
        Path fallbackPath = Paths.get("Chinese.txt");
        Path pathToUse = Files.exists(defaultPath) ? defaultPath : fallbackPath;
        try {
            if (!Files.exists(pathToUse)) {
                log.warn("Anki export not found. Expected at: {}", defaultPath.toAbsolutePath());
                log.warn("Hint: Export your Anki collection as a TSV named 'Chinese.txt' to {}", defaultPath.getParent().toAbsolutePath());
                return; // graceful: provider remains available but has no entries
            }
            List<AnkiNote> notes = parser.parseFile(pathToUse);
            index(notes);
        } catch (IOException e) {
            log.warn("Failed to parse Anki export at {}: {}", pathToUse.toAbsolutePath(), e.getMessage());
        }
    }

    private static Path defaultExportPath() {
        String home = System.getProperty("user.home");
        return Paths.get(home, ".zh-learn", "Chinese.txt");
    }

    public ExistingAnkiPronunciationProvider(Path collectionPath, AnkiNoteParser parser) {
        this.pinyinToPronunciation = new HashMap<>();
        try {
            List<AnkiNote> notes = parser.parseFile(collectionPath);
            index(notes);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse Anki collection: " + e.getMessage(), e);
        }
    }

    public ExistingAnkiPronunciationProvider(List<AnkiNote> notes) {
        this.pinyinToPronunciation = new HashMap<>();
        index(notes);
    }

    public static ExistingAnkiPronunciationProvider fromString(String tsvContent) {
        try {
            Reader r = new StringReader(tsvContent);
            List<AnkiNote> notes = new AnkiNoteParser().parseFromReader(r);
            return new ExistingAnkiPronunciationProvider(notes);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse content: " + e.getMessage(), e);
        }
    }

    private void index(List<AnkiNote> notes) {
        for (AnkiNote n : notes) {
            String p = normalizePinyin(safe(n.pinyin()));
            String pron = safe(n.pronunciation());
            if (!p.isEmpty() && !pron.isEmpty()) {
                // Keep first non-empty pronunciation for a given pinyin
                pinyinToPronunciation.putIfAbsent(p, pron);
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
    public Optional<String> getPronunciation(Hanzi word, Pinyin pinyin) {
        if (pinyin == null || pinyin.pinyin() == null) return Optional.empty();
        String key = normalizePinyin(pinyin.pinyin().trim());
        if (key.isEmpty()) return Optional.empty();
        String result = pinyinToPronunciation.get(key);
        return (result == null || result.isEmpty()) ? Optional.empty() : Optional.of(result);
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
    private static String normalizePinyin(String s) {
        if (s == null) return "";
        String t = s.trim();
        return Normalizer.normalize(t, Normalizer.Form.NFC);
    }
}
