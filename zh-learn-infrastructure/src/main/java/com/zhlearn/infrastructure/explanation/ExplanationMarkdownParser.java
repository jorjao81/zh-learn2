package com.zhlearn.infrastructure.explanation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zhlearn.domain.model.ExplanationEntry;

/**
 * Parser for Chinese explanation markdown files. Splits a multi-entry file into individual entries
 * and extracts term, pinyin, entry type, and the full explanation body.
 */
public class ExplanationMarkdownParser {

    private static final Logger LOG = Logger.getLogger(ExplanationMarkdownParser.class.getName());

    // Matches: # Word Analysis: 水域  or  # Sentence Analysis: 在这行混...
    private static final Pattern H1_PATTERN =
            Pattern.compile("^#\\s+(\\S+(?:\\s+\\S+)*)\\s+Analysis:\\s*(.+)$");

    /**
     * Parse all entries from a markdown file.
     *
     * @param file path to the explanations markdown file
     * @return list of parsed entries
     * @throws IOException if file cannot be read
     */
    public List<ExplanationEntry> parseFile(Path file) throws IOException {
        String content = Files.readString(file, StandardCharsets.UTF_8);
        return parseContent(content);
    }

    /**
     * Parse all entries from markdown content.
     *
     * @param content the markdown content
     * @return list of parsed entries
     */
    public List<ExplanationEntry> parseContent(String content) {
        List<String> rawEntries = splitIntoEntries(content);
        List<ExplanationEntry> entries = new ArrayList<>();

        for (int i = 0; i < rawEntries.size(); i++) {
            try {
                entries.add(parseSingleEntry(rawEntries.get(i)));
            } catch (IllegalArgumentException e) {
                LOG.warning("Skipping entry " + (i + 1) + ": " + e.getMessage());
            }
        }

        return entries;
    }

    /** Split the full markdown content into individual entry blocks by detecting H1 headers. */
    private List<String> splitIntoEntries(String content) {
        String[] lines = content.split("\n");
        List<String> entries = new ArrayList<>();
        StringBuilder current = null;

        for (String line : lines) {
            if (H1_PATTERN.matcher(line.trim()).matches()) {
                if (current != null) {
                    entries.add(current.toString());
                }
                current = new StringBuilder();
                current.append(line).append("\n");
            } else if (current != null) {
                current.append(line).append("\n");
            }
        }

        if (current != null) {
            entries.add(current.toString());
        }

        return entries;
    }

    /** Parse a single entry block into an ExplanationEntry. */
    private ExplanationEntry parseSingleEntry(String entryContent) {
        String[] lines = entryContent.split("\n");

        String term = null;
        String entryType = null;
        String pinyin = null;
        StringBuilder explanation = new StringBuilder();

        boolean pastPinyinSection = false;
        boolean inPinyinSection = false;

        for (String line : lines) {
            String trimmed = line.trim();

            // Parse H1 to get term and entry type
            if (term == null) {
                Matcher h1 = H1_PATTERN.matcher(trimmed);
                if (h1.matches()) {
                    entryType = h1.group(1).trim().toLowerCase();
                    term = h1.group(2).trim();
                    continue;
                }
            }

            // Detect ## Pinyin section
            if (!pastPinyinSection && !inPinyinSection && trimmed.equals("## Pinyin")) {
                inPinyinSection = true;
                continue;
            }

            // Inside pinyin section: collect pinyin text
            if (inPinyinSection) {
                if (trimmed.startsWith("## ")) {
                    // Hit next section — pinyin section is done
                    inPinyinSection = false;
                    pastPinyinSection = true;
                    // This line is part of the explanation
                    explanation.append(line).append("\n");
                } else if (!trimmed.isEmpty() && pinyin == null) {
                    pinyin = trimmed;
                }
                continue;
            }

            // Everything after pinyin section goes into explanation
            if (pastPinyinSection) {
                // Skip --- separators at the end
                if (trimmed.equals("---")) {
                    continue;
                }
                explanation.append(line).append("\n");
            }
        }

        // Handle case where pinyin was the last section (shouldn't happen but be safe)

        if (term == null) {
            throw new IllegalArgumentException("No H1 title found");
        }
        if (pinyin == null) {
            throw new IllegalArgumentException("No pinyin found for: " + term);
        }

        String explanationText = explanation.toString().stripTrailing();
        if (explanationText.isBlank()) {
            throw new IllegalArgumentException("No explanation content for: " + term);
        }

        return new ExplanationEntry(term, pinyin, explanationText, entryType);
    }
}
