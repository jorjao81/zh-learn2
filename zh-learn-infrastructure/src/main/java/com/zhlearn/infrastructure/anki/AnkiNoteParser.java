package com.zhlearn.infrastructure.anki;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * Parser for the Anki collection TSV export (Chinese.txt). Only rows with note type "Chinese 2" are
 * returned.
 */
public class AnkiNoteParser {

    private static final CSVFormat TSV =
            CSVFormat.DEFAULT
                    .builder()
                    .setDelimiter('\t')
                    .setQuote('"')
                    .setIgnoreEmptyLines(false)
                    .setTrim(false)
                    .build();

    public List<AnkiNote> parseFile(Path file) throws IOException {
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return parseFromReader(r);
        }
    }

    public List<AnkiNote> parseFromReader(Reader reader) throws IOException {
        List<AnkiNote> notes = new ArrayList<>();
        try (CSVParser parser = TSV.parse(reader)) {
            for (CSVRecord record : parser) {
                if (record.size() == 0) continue;
                String first = record.get(0);
                if (shouldSkipLine(first)) continue;
                String noteType = first != null ? first.trim() : "";
                if (!isChinese2Type(noteType)) continue;

                String col1 = get(record, 1);
                String col2 = get(record, 2);
                // For "Chinese 2": 1=simplified, 2=pinyin
                String simplified = col1;
                String pinyin = col2;
                String pronunciation = get(record, 3);
                String definition = get(record, 4);
                String examples = get(record, 5);
                String etymology = get(record, 6);
                String components = get(record, 7);
                String similar = get(record, 8);
                String passive = get(record, 9);
                String alt = get(record, 10);
                String noHearing = get(record, 11);

                notes.add(
                        AnkiNote.ofCollection(
                                noteType,
                                pinyin,
                                simplified,
                                pronunciation,
                                definition,
                                examples,
                                etymology,
                                components,
                                similar,
                                passive,
                                alt,
                                noHearing));
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

    private static boolean isChinese2Type(String noteType) {
        if (noteType == null) return false;
        String t = noteType.trim();
        return "Chinese 2".equals(t);
    }
}
