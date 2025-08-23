package com.zhlearn.infrastructure.anki;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for Anki card data from TSV format using Apache Commons CSV for robust parsing.
 * Handles the external Anki TSV format and converts it to AnkiCard objects.
 */
public class AnkiCardParser {
    
    // Configure CSV format for tab-separated values with proper escaping
    private static final CSVFormat TSV_FORMAT = CSVFormat.DEFAULT
            .builder()
            .setDelimiter('\t')
            .setQuote('"')
            .setRecordSeparator('\n')
            .setIgnoreEmptyLines(false)  // We want to handle empty lines ourselves
            .setTrim(false)              // Preserve whitespace as-is
            .build();
    
    public AnkiCard parseLine(String line) {
        if (shouldSkipLine(line)) {
            throw new IllegalArgumentException("Cannot parse header or empty line: " + line);
        }
        
        try (StringReader stringReader = new StringReader(line);
             CSVParser parser = CSVFormat.DEFAULT.withDelimiter('\t').parse(stringReader)) {
            
            CSVRecord record = parser.iterator().next();
            return parseRecord(record);
            
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse line: " + line, e);
        }
    }
    
    public List<AnkiCard> parseFromReader(Reader reader) throws IOException {
        List<AnkiCard> cards = new ArrayList<>();
        
        try (CSVParser parser = TSV_FORMAT.parse(reader)) {
            for (CSVRecord record : parser) {
                // Skip header lines and empty lines
                if (record.size() == 0 || shouldSkipLine(record.get(0))) {
                    continue;
                }
                
                try {
                    AnkiCard card = parseRecord(record);
                    cards.add(card);
                } catch (Exception e) {
                    // Log the error but continue processing other records
                    System.err.println("Warning: Failed to parse record at line " + 
                                     record.getRecordNumber() + ": " + e.getMessage());
                }
            }
        }
        
        return cards;
    }
    
    public List<AnkiCard> parseFile(Path filePath) throws IOException {
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            return parseFromReader(reader);
        }
    }
    
    /**
     * Converts a CSVRecord to an AnkiCard.
     * Handles missing fields by padding with empty strings.
     * Handles extra fields by ignoring them.
     */
    private AnkiCard parseRecord(CSVRecord record) {
        String[] fields = new String[11];
        
        // Extract up to 11 fields from the record
        for (int i = 0; i < Math.min(11, record.size()); i++) {
            fields[i] = record.get(i);
        }
        
        // Pad missing fields with empty strings
        for (int i = record.size(); i < 11; i++) {
            fields[i] = "";
        }
        
        return AnkiCard.of(fields);
    }
    
    /**
     * Checks if a line should be skipped (header lines, empty lines, etc.)
     * 
     * @param line the line to check
     * @return true if the line should be skipped
     */
    public boolean shouldSkipLine(String line) {
        return line == null || line.trim().isEmpty() || line.startsWith("#");
    }
}