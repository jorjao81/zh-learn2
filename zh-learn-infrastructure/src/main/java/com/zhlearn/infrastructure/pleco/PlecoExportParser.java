package com.zhlearn.infrastructure.pleco;

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

import com.zhlearn.pinyin.PinyinToneConverter;

/**
 * Parser for Pleco export files (TSV format). Parses entries with format: 汉字 pinyin definition_text
 * Converts numbered pinyin to tone marks during parsing.
 */
public class PlecoExportParser {

    private static final CSVFormat TSV =
            CSVFormat.DEFAULT
                    .builder()
                    .setDelimiter('\t')
                    .setQuote('"')
                    .setIgnoreEmptyLines(false)
                    .setTrim(false)
                    .build();

    // No sequence pattern needed after simplification

    /**
     * Parse a Pleco export file from a file path.
     *
     * @param file path to the Pleco export file
     * @return list of parsed PlecoEntry objects
     * @throws IOException if file cannot be read
     */
    public List<PlecoEntry> parseFile(Path file) throws IOException {
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return parseFromReader(r);
        }
    }

    /**
     * Parse Pleco export data from a Reader.
     *
     * @param reader reader containing the TSV data
     * @return list of parsed PlecoEntry objects
     * @throws IOException if data cannot be read
     * @throws IllegalArgumentException if any record cannot be parsed
     */
    public List<PlecoEntry> parseFromReader(Reader reader) throws IOException {
        List<PlecoEntry> entries = new ArrayList<>();

        try (CSVParser parser = TSV.parse(reader)) {
            for (CSVRecord record : parser) {
                if (record.size() == 0) continue;

                try {
                    PlecoEntry entry = parseRecord(record);
                    entries.add(entry);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Failed to parse record "
                                    + record.getRecordNumber()
                                    + ": "
                                    + e.getMessage(),
                            e);
                }
            }
        }

        return entries;
    }

    /**
     * Parse a single TSV record into a PlecoEntry. Expected format: 汉字 pinyin [definition_text]
     * Definition text is optional (supports 2 or 3 column format)
     *
     * @param record the CSV record to parse
     * @return PlecoEntry
     * @throws IllegalArgumentException if record cannot be parsed
     */
    private PlecoEntry parseRecord(CSVRecord record) {
        if (record.size() < 2) {
            throw new IllegalArgumentException(
                    "Record must have at least 2 columns (hanzi, pinyin), got "
                            + record.size()
                            + " columns");
        }
        if (record.size() > 3) {
            throw new IllegalArgumentException(
                    "Record must have at most 3 columns (hanzi, pinyin, definition), got "
                            + record.size()
                            + " columns");
        }

        String firstColumn = get(record, 0);
        String pinyinColumn = get(record, 1);
        String definitionColumn = record.size() >= 3 ? get(record, 2) : null;

        // Extract hanzi from first column (remove sequence number prefix)
        String hanzi = extractHanzi(firstColumn);
        if (hanzi == null || hanzi.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid or empty hanzi in first column: '" + firstColumn + "'");
        }

        // Convert numbered pinyin to tone marks
        String pinyin = PinyinToneConverter.convertToToneMarks(pinyinColumn);
        if (pinyin == null || pinyin.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid or empty pinyin in second column: '" + pinyinColumn + "'");
        }

        // Definition text is used as-is (empty if not provided)
        String definitionText = definitionColumn != null ? definitionColumn : "";

        return new PlecoEntry(hanzi, pinyin, definitionText);
    }

    /**
     * Extract hanzi characters from the first column. The first column is the hanzi itself in the
     * export.
     *
     * @param firstColumn the first column value
     * @return hanzi characters or null if invalid
     */
    private String extractHanzi(String firstColumn) {
        if (firstColumn == null) {
            return null;
        }

        // Trim whitespace and strip optional BOM
        String s = firstColumn.trim();
        if (s.isEmpty()) return null;
        if (s.startsWith("\uFEFF")) {
            s = s.substring(1);
        }

        return s;
    }

    /**
     * Get a column value from a CSV record, handling null/empty cases.
     *
     * @param record the CSV record
     * @param index the column index
     * @return column value or null if not available
     */
    private String get(CSVRecord record, int index) {
        if (index >= record.size()) {
            return null;
        }
        String value = record.get(index);
        return value != null && !value.isEmpty() ? value : null;
    }
}
