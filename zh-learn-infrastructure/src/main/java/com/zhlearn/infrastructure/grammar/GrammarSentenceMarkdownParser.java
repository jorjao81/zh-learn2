package com.zhlearn.infrastructure.grammar;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zhlearn.domain.model.GrammarSentenceExercise;
import com.zhlearn.domain.model.GrammarSentenceFile;

/**
 * Parser for grammar sentence exercise markdown files. Parses the nested list format with
 * ==highlight== markers.
 */
public class GrammarSentenceMarkdownParser {

    // Pattern to match title: # 似乎 (sìhū) — 四11
    private static final Pattern TITLE_PATTERN =
            Pattern.compile("^#\\s+(.+?)\\s+\\(([^)]+)\\)\\s+[—–-]\\s+(.+)$");

    // Pattern to match grammar link: - **Grammar**: [HSK4-11-似乎](../HSK4-11-似乎.md)
    private static final Pattern GRAMMAR_LINK_PATTERN =
            Pattern.compile("^-\\s+\\*\\*Grammar\\*\\*:\\s+\\[.+?\\]\\(([^)]+)\\)");

    // Pattern to match characters link: - **Characters**: [似乎](../characters/似乎.md)
    private static final Pattern CHARACTERS_LINK_PATTERN =
            Pattern.compile("^-\\s+\\*\\*Characters\\*\\*:\\s+\\[.+?\\]\\(([^)]+)\\)");

    // Pattern to match top-level list item (Chinese sentence): - 天==似乎==要下雨了。
    private static final Pattern CHINESE_SENTENCE_PATTERN = Pattern.compile("^-\\s+(.+)$");

    // Pattern to match sub-item (indented with spaces): - *pinyin* or - English
    private static final Pattern SUB_ITEM_PATTERN = Pattern.compile("^\\s+-\\s+(.+)$");

    // Pattern to extract italic content: *text* or _text_
    private static final Pattern ITALIC_PATTERN = Pattern.compile("^[*_](.+)[*_]$");

    /**
     * Parse a grammar sentence markdown file from a path.
     *
     * @param file path to the markdown file
     * @return parsed GrammarSentenceFile
     * @throws IOException if file cannot be read
     */
    public GrammarSentenceFile parseFile(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return parseFromReader(reader);
        }
    }

    /**
     * Parse grammar sentence markdown from a Reader.
     *
     * @param reader reader containing the markdown content
     * @return parsed GrammarSentenceFile
     * @throws IOException if content cannot be read
     */
    public GrammarSentenceFile parseFromReader(Reader reader) throws IOException {
        String content = readAll(reader);
        return parseContent(content);
    }

    // Pattern to match continuation lines (indented text that's not a list item)
    private static final Pattern CONTINUATION_PATTERN = Pattern.compile("^\\s{4,}(.+)$");

    /**
     * Parse grammar sentence markdown from a string.
     *
     * @param content the markdown content
     * @return parsed GrammarSentenceFile
     */
    public GrammarSentenceFile parseContent(String content) {
        String[] lines = content.split("\n");

        String word = null;
        String pinyin = null;
        String grammarPoint = null;
        String grammarFilePath = null;
        String characterFilePath = null;
        List<GrammarSentenceExercise> exercises = new ArrayList<>();

        boolean inExercisesSection = false;
        String currentChinese = null;
        String currentPinyin = null;
        StringBuilder currentEnglish = null;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();

            // Skip empty lines (but finalize any pending English sentence first)
            if (trimmed.isEmpty()) {
                if (currentEnglish != null && currentChinese != null && currentPinyin != null) {
                    exercises.add(
                            new GrammarSentenceExercise(
                                    currentChinese,
                                    currentPinyin,
                                    currentEnglish.toString().trim()));
                    currentChinese = null;
                    currentPinyin = null;
                    currentEnglish = null;
                }
                continue;
            }

            // Parse title
            if (word == null && trimmed.startsWith("#") && !trimmed.startsWith("##")) {
                Matcher titleMatcher = TITLE_PATTERN.matcher(trimmed);
                if (titleMatcher.matches()) {
                    word = titleMatcher.group(1).trim();
                    pinyin = titleMatcher.group(2).trim();
                    grammarPoint = titleMatcher.group(3).trim();
                }
                continue;
            }

            // Parse grammar link
            if (grammarFilePath == null) {
                Matcher grammarMatcher = GRAMMAR_LINK_PATTERN.matcher(trimmed);
                if (grammarMatcher.find()) {
                    grammarFilePath = grammarMatcher.group(1);
                    continue;
                }
            }

            // Parse characters link
            if (characterFilePath == null && trimmed.contains("**Characters**")) {
                Matcher charMatcher = CHARACTERS_LINK_PATTERN.matcher(trimmed);
                if (charMatcher.find()) {
                    characterFilePath = charMatcher.group(1);
                    continue;
                }
            }

            // Check for exercises section header
            if (trimmed.startsWith("## Sentence Exercises")) {
                inExercisesSection = true;
                continue;
            }

            // Parse exercises
            if (inExercisesSection) {
                // Check if this is a top-level list item (Chinese sentence)
                if (line.matches("^-\\s+.+$")) {
                    // Save previous exercise if complete
                    if (currentEnglish != null && currentChinese != null && currentPinyin != null) {
                        exercises.add(
                                new GrammarSentenceExercise(
                                        currentChinese,
                                        currentPinyin,
                                        currentEnglish.toString().trim()));
                        currentEnglish = null;
                    }

                    Matcher chineseMatcher = CHINESE_SENTENCE_PATTERN.matcher(line);
                    if (chineseMatcher.matches()) {
                        currentChinese = chineseMatcher.group(1).trim();
                        currentPinyin = null; // Reset for new sentence
                    }
                    continue;
                }

                // Check if this is a sub-item
                Matcher subMatcher = SUB_ITEM_PATTERN.matcher(line);
                if (subMatcher.matches()) {
                    // Finalize previous English if starting a new sub-item
                    if (currentEnglish != null && currentChinese != null && currentPinyin != null) {
                        exercises.add(
                                new GrammarSentenceExercise(
                                        currentChinese,
                                        currentPinyin,
                                        currentEnglish.toString().trim()));
                        currentChinese = null;
                        currentPinyin = null;
                        currentEnglish = null;
                    }

                    String subContent = subMatcher.group(1).trim();

                    // Check if it's pinyin (italicized)
                    Matcher italicMatcher = ITALIC_PATTERN.matcher(subContent);
                    if (italicMatcher.matches()) {
                        currentPinyin = italicMatcher.group(1).trim();
                    } else {
                        // It's the English sentence - start accumulating
                        if (currentChinese != null && currentPinyin != null) {
                            currentEnglish = new StringBuilder(subContent);
                        }
                    }
                    continue;
                }

                // Check if this is a continuation line (for multi-line English sentences)
                if (currentEnglish != null) {
                    Matcher contMatcher = CONTINUATION_PATTERN.matcher(line);
                    if (contMatcher.matches()) {
                        currentEnglish.append(" ").append(contMatcher.group(1).trim());
                        continue;
                    }
                }
            }
        }

        // Finalize any remaining exercise
        if (currentEnglish != null && currentChinese != null && currentPinyin != null) {
            exercises.add(
                    new GrammarSentenceExercise(
                            currentChinese, currentPinyin, currentEnglish.toString().trim()));
        }

        // Validate required fields
        if (word == null) {
            throw new IllegalArgumentException(
                    "Missing title line with word, pinyin, and grammar point");
        }
        if (grammarFilePath == null) {
            throw new IllegalArgumentException("Missing grammar file link");
        }
        if (exercises.isEmpty()) {
            throw new IllegalArgumentException("No sentence exercises found");
        }

        return new GrammarSentenceFile(
                word, pinyin, grammarPoint, grammarFilePath, characterFilePath, exercises);
    }

    private String readAll(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[8192];
        int read;
        while ((read = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, read);
        }
        return sb.toString();
    }
}
