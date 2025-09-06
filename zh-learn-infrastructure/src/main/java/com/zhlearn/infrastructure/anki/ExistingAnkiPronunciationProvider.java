package com.zhlearn.infrastructure.anki;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.provider.AudioProvider;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.*;

/**
 * Audio provider that reuses pronunciations already present in the user's
 * Anki collection export (Chinese.txt). It finds entries by exact pinyin match
 * and returns the pronunciation field if non-empty.
 */
public class ExistingAnkiPronunciationProvider implements AudioProvider {

    private static final String NAME = "existing-anki-pronunciation";
    private static final String DESCRIPTION = "Reuses existing pronunciations from local Anki collection (Chinese.txt) by exact pinyin match.";

    private final Map<String, String> pinyinToPronunciation;

    public ExistingAnkiPronunciationProvider(Path collectionPath, AnkiCollectionParser parser) {
        this.pinyinToPronunciation = new HashMap<>();
        try {
            List<AnkiCollectionNote> notes = parser.parseFile(collectionPath);
            index(notes);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse Anki collection: " + e.getMessage(), e);
        }
    }

    public ExistingAnkiPronunciationProvider(List<AnkiCollectionNote> notes) {
        this.pinyinToPronunciation = new HashMap<>();
        index(notes);
    }

    public static ExistingAnkiPronunciationProvider fromString(String tsvContent, AnkiCollectionParser parser) {
        try {
            Reader r = new StringReader(tsvContent);
            List<AnkiCollectionNote> notes = parser.parseFromReader(r);
            return new ExistingAnkiPronunciationProvider(notes);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse content: " + e.getMessage(), e);
        }
    }

    private void index(List<AnkiCollectionNote> notes) {
        for (AnkiCollectionNote n : notes) {
            String p = safe(n.pinyin());
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
    public Optional<String> getPronunciation(Hanzi word, Pinyin pinyin) {
        if (pinyin == null || pinyin.pinyin() == null) return Optional.empty();
        String key = pinyin.pinyin().trim();
        if (key.isEmpty()) return Optional.empty();
        String result = pinyinToPronunciation.get(key);
        return (result == null || result.isEmpty()) ? Optional.empty() : Optional.of(result);
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
}

