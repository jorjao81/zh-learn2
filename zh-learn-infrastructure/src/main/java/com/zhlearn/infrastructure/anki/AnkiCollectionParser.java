package com.zhlearn.infrastructure.anki;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for the Anki collection TSV export (Chinese.txt) where the first column
 * is the Note Type. Only rows with Note Type "Chinese" or "Chinese 2" are parsed
 * and returned.
 */
public class AnkiCollectionParser {

    private static final CSVFormat TSV = CSVFormat.DEFAULT
        .builder()
        .setDelimiter('\t')
        .setQuote('"')
        .setIgnoreEmptyLines(false)
        .setTrim(false)
        .build();

    public List<AnkiCollectionNote> parseFile(Path file) throws IOException {
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return parseFromReader(r);
        }
    }

    public List<AnkiCollectionNote> parseFromReader(Reader reader) throws IOException {
        List<AnkiCollectionNote> notes = new ArrayList<>();

        try (CSVParser parser = TSV.parse(reader)) {
            for (CSVRecord record : parser) {
                if (record.size() == 0) continue;
                String first = record.get(0);
                if (shouldSkipLine(first)) continue;

                // Observed format in Chinese.txt:
                // 0=noteType, 1=pinyin, 2=simplified, 3=pronunciation, 4=definition,
                // 5=examples, 6=etymology, 7=components ... (rest ignored)
                String noteType = get(record, 0);
                if (!isChineseType(noteType)) continue;

                String pinyin = get(record, 1);
                String simplified = get(record, 2);
                String pronunciation = get(record, 3);
                String definition = get(record, 4);
                String examples = get(record, 5);
                String etymology = get(record, 6);
                String components = get(record, 7);

                notes.add(new AnkiCollectionNote(
                    simplified(noteType),
                    safeTrim(simplified),
                    safeTrim(pinyin),
                    safeTrim(pronunciation),
                    safeTrim(definition),
                    safeTrim(examples),
                    safeTrim(etymology),
                    safeTrim(components)
                ));
            }
        }

        return notes;
    }

    private static String get(CSVRecord r, int i) {
        return i < r.size() ? r.get(i) : "";
    }

    private static boolean shouldSkipLine(String firstColumn) {
        return firstColumn == null || firstColumn.trim().isEmpty() || firstColumn.startsWith("#");
    }

    private static boolean isChineseType(String noteType) {
        if (noteType == null) return false;
        String t = noteType.trim();
        return "Chinese".equals(t) || "Chinese 2".equals(t);
    }

    private static String simplified(String s) {
        return s == null ? "" : s;
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
