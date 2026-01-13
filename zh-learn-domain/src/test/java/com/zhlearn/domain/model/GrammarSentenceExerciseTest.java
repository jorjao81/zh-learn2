package com.zhlearn.domain.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GrammarSentenceExerciseTest {

    @Test
    void shouldCreateValidExercise() {
        GrammarSentenceExercise exercise =
                new GrammarSentenceExercise(
                        "天==似乎==要下雨了。",
                        "Tiān sìhū yào xiàyǔ le.",
                        "It ==seems like== it's going to rain.");

        assertThat(exercise.sentenceCN()).isEqualTo("天==似乎==要下雨了。");
        assertThat(exercise.sentencePinyin()).isEqualTo("Tiān sìhū yào xiàyǔ le.");
        assertThat(exercise.sentenceEN()).isEqualTo("It ==seems like== it's going to rain.");
    }

    @Test
    void shouldExtractHighlightFromChinese() {
        GrammarSentenceExercise exercise =
                new GrammarSentenceExercise(
                        "天==似乎==要下雨了。",
                        "Tiān sìhū yào xiàyǔ le.",
                        "It ==seems like== it's going to rain.");

        assertThat(exercise.extractHighlightCN()).isEqualTo("似乎");
    }

    @Test
    void shouldExtractHighlightFromEnglish() {
        GrammarSentenceExercise exercise =
                new GrammarSentenceExercise(
                        "天==似乎==要下雨了。",
                        "Tiān sìhū yào xiàyǔ le.",
                        "It ==seems like== it's going to rain.");

        assertThat(exercise.extractHighlightEN()).isEqualTo("seems like");
    }

    @Test
    void shouldConvertChineseToHtml() {
        GrammarSentenceExercise exercise =
                new GrammarSentenceExercise(
                        "天==似乎==要下雨了。",
                        "Tiān sìhū yào xiàyǔ le.",
                        "It ==seems like== it's going to rain.");

        assertThat(exercise.sentenceCNAsHtml()).isEqualTo("天<span class=\"hl\">似乎</span>要下雨了。");
    }

    @Test
    void shouldConvertEnglishToHtml() {
        GrammarSentenceExercise exercise =
                new GrammarSentenceExercise(
                        "天==似乎==要下雨了。",
                        "Tiān sìhū yào xiàyǔ le.",
                        "It ==seems like== it's going to rain.");

        assertThat(exercise.sentenceENAsHtml())
                .isEqualTo("It <span class=\"hl\">seems like</span> it's going to rain.");
    }

    @Test
    void shouldThrowExceptionForNullChinese() {
        assertThatThrownBy(
                        () ->
                                new GrammarSentenceExercise(
                                        null,
                                        "Tiān sìhū yào xiàyǔ le.",
                                        "It ==seems like== it's going to rain."))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Chinese sentence cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionForBlankChinese() {
        assertThatThrownBy(
                        () ->
                                new GrammarSentenceExercise(
                                        "   ",
                                        "Tiān sìhū yào xiàyǔ le.",
                                        "It ==seems like== it's going to rain."))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Chinese sentence cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionForNullPinyin() {
        assertThatThrownBy(
                        () ->
                                new GrammarSentenceExercise(
                                        "天==似乎==要下雨了。",
                                        null,
                                        "It ==seems like== it's going to rain."))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pinyin cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionForNullEnglish() {
        assertThatThrownBy(
                        () ->
                                new GrammarSentenceExercise(
                                        "天==似乎==要下雨了。", "Tiān sìhū yào xiàyǔ le.", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("English sentence cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionForChineseWithoutHighlight() {
        assertThatThrownBy(
                        () ->
                                new GrammarSentenceExercise(
                                        "天似乎要下雨了。",
                                        "Tiān sìhū yào xiàyǔ le.",
                                        "It ==seems like== it's going to rain."))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chinese sentence must contain highlight markers");
    }

    @Test
    void shouldThrowExceptionForEnglishWithoutHighlight() {
        assertThatThrownBy(
                        () ->
                                new GrammarSentenceExercise(
                                        "天==似乎==要下雨了。",
                                        "Tiān sìhū yào xiàyǔ le.",
                                        "It seems like it's going to rain."))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("English sentence must contain highlight markers");
    }

    @Test
    void shouldHandleHighlightAtBeginning() {
        GrammarSentenceExercise exercise =
                new GrammarSentenceExercise(
                        "==似乎==天要下雨了。",
                        "Sìhū tiān yào xiàyǔ le.",
                        "==Seems like== it's going to rain.");

        assertThat(exercise.extractHighlightCN()).isEqualTo("似乎");
        assertThat(exercise.extractHighlightEN()).isEqualTo("Seems like");
        assertThat(exercise.sentenceCNAsHtml()).isEqualTo("<span class=\"hl\">似乎</span>天要下雨了。");
    }

    @Test
    void shouldHandleHighlightAtEnd() {
        GrammarSentenceExercise exercise =
                new GrammarSentenceExercise(
                        "天要下雨了==似乎==",
                        "Tiān yào xiàyǔ le sìhū.",
                        "It's going to rain ==it seems==");

        assertThat(exercise.extractHighlightCN()).isEqualTo("似乎");
        assertThat(exercise.extractHighlightEN()).isEqualTo("it seems");
    }

    @Test
    void shouldHandlePinyinHighlight() {
        // Test with pinyin that also has highlight markers (as per updated format)
        GrammarSentenceExercise exercise =
                new GrammarSentenceExercise(
                        "天==似乎==要下雨了。",
                        "Tiān ==sìhū== yào xiàyǔ le.",
                        "It ==seems like== it's going to rain.");

        assertThat(exercise.sentencePinyin()).isEqualTo("Tiān ==sìhū== yào xiàyǔ le.");
    }
}
