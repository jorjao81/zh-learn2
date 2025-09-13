package com.zhlearn.infrastructure.pleco;

import com.zhlearn.pinyin.PinyinToneConverter;
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
 * Parser for Pleco export files (TSV format).
 * Parses entries with format: 汉字	pinyin	definition_text
 * Converts numbered pinyin to tone marks during parsing.
 */
public class PlecoExportParser {
    
    private static final CSVFormat TSV = CSVFormat.DEFAULT
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
     */
    public List<PlecoEntry> parseFromReader(Reader reader) throws IOException {
        List<PlecoEntry> entries = new ArrayList<>();
        
        try (CSVParser parser = TSV.parse(reader)) {
            for (CSVRecord record : parser) {
                if (record.size() == 0) continue;
                
                PlecoEntry entry = parseRecord(record);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }
        
        return entries;
    }
    
    /**
     * Parse a single TSV record into a PlecoEntry.
     * Expected format: 汉字	pinyin	definition_text
     * 
     * @param record the CSV record to parse
     * @return PlecoEntry or null if record is invalid
     */
    private PlecoEntry parseRecord(CSVRecord record) {
        if (record.size() < 3) {
            return null;
        }
        
        String firstColumn = get(record, 0);
        String pinyinColumn = get(record, 1);  
        String definitionColumn = get(record, 2);
        
        // Extract hanzi from first column (remove sequence number prefix)
        String hanzi = extractHanzi(firstColumn);
        if (hanzi == null || hanzi.isEmpty()) {
            return null;
        }
        
        // Convert numbered pinyin to tone marks
        String pinyin = PinyinToneConverter.convertToToneMarks(pinyinColumn);
        if (pinyin == null || pinyin.isEmpty()) {
            return null;
        }
        
        // Definition text is used as-is
        String definitionText = definitionColumn != null ? definitionColumn : "";
        
        try {
            return new PlecoEntry(hanzi, pinyin, definitionText);
        } catch (IllegalArgumentException e) {
            // Skip invalid entries
            return null;
        }
    }
    
    /**
     * Extract hanzi characters from the first column.
     * The first column is the hanzi itself in the export.
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
