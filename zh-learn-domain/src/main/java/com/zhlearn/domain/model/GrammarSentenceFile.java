package com.zhlearn.domain.model;

import java.util.List;

/** Represents a parsed grammar sentence markdown file containing metadata and exercises. */
public record GrammarSentenceFile(
        String word,
        String pinyin,
        String grammarPoint,
        String grammarFilePath,
        String characterFilePath,
        List<GrammarSentenceExercise> exercises) {

    public GrammarSentenceFile {
        if (word == null || word.isBlank()) {
            throw new IllegalArgumentException("Word cannot be null or blank");
        }
        if (pinyin == null || pinyin.isBlank()) {
            throw new IllegalArgumentException("Pinyin cannot be null or blank");
        }
        if (grammarPoint == null || grammarPoint.isBlank()) {
            throw new IllegalArgumentException("Grammar point cannot be null or blank");
        }
        if (grammarFilePath == null || grammarFilePath.isBlank()) {
            throw new IllegalArgumentException("Grammar file path cannot be null or blank");
        }
        // characterFilePath can be null if no character breakdown exists
        if (exercises == null) {
            throw new IllegalArgumentException("Exercises list cannot be null");
        }
        if (exercises.isEmpty()) {
            throw new IllegalArgumentException("Exercises list cannot be empty");
        }
        exercises = List.copyOf(exercises);
    }

    /** Check if this file has a character breakdown file. */
    public boolean hasCharacterBreakdown() {
        return characterFilePath != null && !characterFilePath.isBlank();
    }
}
