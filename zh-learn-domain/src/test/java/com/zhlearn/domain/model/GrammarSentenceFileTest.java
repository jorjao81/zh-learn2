package com.zhlearn.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class GrammarSentenceFileTest {

    private GrammarSentenceExercise createSampleExercise() {
        return new GrammarSentenceExercise(
                "天==似乎==要下雨了。", "Tiān sìhū yào xiàyǔ le.", "It ==seems like== it's going to rain.");
    }

    @Test
    void shouldCreateValidFile() {
        GrammarSentenceFile file =
                new GrammarSentenceFile(
                        "似乎",
                        "sìhū",
                        "四11",
                        "../HSK4-11-似乎.md",
                        "../characters/似乎.md",
                        List.of(createSampleExercise()));

        assertThat(file.word()).isEqualTo("似乎");
        assertThat(file.pinyin()).isEqualTo("sìhū");
        assertThat(file.grammarPoint()).isEqualTo("四11");
        assertThat(file.grammarFilePath()).isEqualTo("../HSK4-11-似乎.md");
        assertThat(file.characterFilePath()).isEqualTo("../characters/似乎.md");
        assertThat(file.exercises()).hasSize(1);
        assertThat(file.hasCharacterBreakdown()).isTrue();
    }

    @Test
    void shouldAllowNullCharacterFilePath() {
        GrammarSentenceFile file =
                new GrammarSentenceFile(
                        "似乎",
                        "sìhū",
                        "四11",
                        "../HSK4-11-似乎.md",
                        null,
                        List.of(createSampleExercise()));

        assertThat(file.characterFilePath()).isNull();
        assertThat(file.hasCharacterBreakdown()).isFalse();
    }

    @Test
    void shouldAllowBlankCharacterFilePath() {
        GrammarSentenceFile file =
                new GrammarSentenceFile(
                        "似乎",
                        "sìhū",
                        "四11",
                        "../HSK4-11-似乎.md",
                        "   ",
                        List.of(createSampleExercise()));

        assertThat(file.hasCharacterBreakdown()).isFalse();
    }

    @Test
    void shouldMakeExercisesImmutable() {
        List<GrammarSentenceExercise> mutableList = new ArrayList<>();
        mutableList.add(createSampleExercise());

        GrammarSentenceFile file =
                new GrammarSentenceFile("似乎", "sìhū", "四11", "../HSK4-11-似乎.md", null, mutableList);

        assertThatThrownBy(() -> file.exercises().add(createSampleExercise()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldThrowExceptionForNullWord() {
        assertThatThrownBy(
                        () ->
                                new GrammarSentenceFile(
                                        null,
                                        "sìhū",
                                        "四11",
                                        "../HSK4-11-似乎.md",
                                        null,
                                        List.of(createSampleExercise())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Word cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionForBlankWord() {
        assertThatThrownBy(
                        () ->
                                new GrammarSentenceFile(
                                        "   ",
                                        "sìhū",
                                        "四11",
                                        "../HSK4-11-似乎.md",
                                        null,
                                        List.of(createSampleExercise())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Word cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionForNullPinyin() {
        assertThatThrownBy(
                        () ->
                                new GrammarSentenceFile(
                                        "似乎",
                                        null,
                                        "四11",
                                        "../HSK4-11-似乎.md",
                                        null,
                                        List.of(createSampleExercise())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pinyin cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionForNullGrammarPoint() {
        assertThatThrownBy(
                        () ->
                                new GrammarSentenceFile(
                                        "似乎",
                                        "sìhū",
                                        null,
                                        "../HSK4-11-似乎.md",
                                        null,
                                        List.of(createSampleExercise())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Grammar point cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionForNullGrammarFilePath() {
        assertThatThrownBy(
                        () ->
                                new GrammarSentenceFile(
                                        "似乎",
                                        "sìhū",
                                        "四11",
                                        null,
                                        null,
                                        List.of(createSampleExercise())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Grammar file path cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionForNullExercises() {
        assertThatThrownBy(
                        () ->
                                new GrammarSentenceFile(
                                        "似乎", "sìhū", "四11", "../HSK4-11-似乎.md", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exercises list cannot be null");
    }

    @Test
    void shouldThrowExceptionForEmptyExercises() {
        assertThatThrownBy(
                        () ->
                                new GrammarSentenceFile(
                                        "似乎", "sìhū", "四11", "../HSK4-11-似乎.md", null, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exercises list cannot be empty");
    }

    @Test
    void shouldHandleMultipleExercises() {
        GrammarSentenceExercise ex1 =
                new GrammarSentenceExercise(
                        "天==似乎==要下雨了。",
                        "Tiān sìhū yào xiàyǔ le.",
                        "It ==seems like== it's going to rain.");
        GrammarSentenceExercise ex2 =
                new GrammarSentenceExercise(
                        "他==似乎==不想去。",
                        "Tā sìhū bù xiǎng qù.",
                        "He doesn't ==seem== to want to go.");

        GrammarSentenceFile file =
                new GrammarSentenceFile(
                        "似乎", "sìhū", "四11", "../HSK4-11-似乎.md", null, List.of(ex1, ex2));

        assertThat(file.exercises()).hasSize(2);
    }
}
