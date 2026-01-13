package com.zhlearn.infrastructure.grammar;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.zhlearn.domain.model.GrammarSentenceExercise;
import com.zhlearn.domain.model.GrammarSentenceFile;

class GrammarSentenceMarkdownParserTest {

    private static final String VALID_MARKDOWN =
            """
            # 似乎 (sìhū) — 四11

            - **Grammar**: [HSK4-11-似乎](../HSK4-11-似乎.md)
            - **Characters**: [似乎](../characters/似乎.md)

            ## Sentence Exercises

            - 天==似乎==要下雨了。
              - *Tiān ==sìhū== yào xiàyǔ le.*
              - It ==seems like== it's going to rain.

            - 他==似乎==不太想去。
              - *Tā ==sìhū== bù tài xiǎng qù.*
              - He doesn't ==seem== to want to go.

            - 她==似乎==生气了。
              - *Tā ==sìhū== shēngqì le.*
              - She ==seems== to be angry.
            """;

    @Test
    void shouldParseValidMarkdown() {
        GrammarSentenceMarkdownParser parser = new GrammarSentenceMarkdownParser();
        GrammarSentenceFile result = parser.parseContent(VALID_MARKDOWN);

        assertThat(result.word()).isEqualTo("似乎");
        assertThat(result.pinyin()).isEqualTo("sìhū");
        assertThat(result.grammarPoint()).isEqualTo("四11");
        assertThat(result.grammarFilePath()).isEqualTo("../HSK4-11-似乎.md");
        assertThat(result.characterFilePath()).isEqualTo("../characters/似乎.md");
        assertThat(result.exercises()).hasSize(3);
    }

    @Test
    void shouldParseExercisesCorrectly() {
        GrammarSentenceMarkdownParser parser = new GrammarSentenceMarkdownParser();
        GrammarSentenceFile result = parser.parseContent(VALID_MARKDOWN);

        GrammarSentenceExercise first = result.exercises().get(0);
        assertThat(first.sentenceCN()).isEqualTo("天==似乎==要下雨了。");
        assertThat(first.sentencePinyin()).isEqualTo("Tiān ==sìhū== yào xiàyǔ le.");
        assertThat(first.sentenceEN()).isEqualTo("It ==seems like== it's going to rain.");

        GrammarSentenceExercise second = result.exercises().get(1);
        assertThat(second.sentenceCN()).isEqualTo("他==似乎==不太想去。");
        assertThat(second.sentencePinyin()).isEqualTo("Tā ==sìhū== bù tài xiǎng qù.");
        assertThat(second.sentenceEN()).isEqualTo("He doesn't ==seem== to want to go.");
    }

    @Test
    void shouldHandleMissingCharactersLink() {
        String markdownWithoutCharacters =
                """
                # 岂不 (qǐbù) — NON-HSK

                - **Grammar**: [NON-HSK-岂不](../NON-HSK-岂不.md)

                ## Sentence Exercises

                - ==岂不==很好？
                  - *==Qǐbù== hěn hǎo?*
                  - Wouldn't that ==be great==?
                """;

        GrammarSentenceMarkdownParser parser = new GrammarSentenceMarkdownParser();
        GrammarSentenceFile result = parser.parseContent(markdownWithoutCharacters);

        assertThat(result.word()).isEqualTo("岂不");
        assertThat(result.characterFilePath()).isNull();
        assertThat(result.hasCharacterBreakdown()).isFalse();
    }

    @Test
    void shouldThrowExceptionForMissingTitle() {
        String noTitle =
                """
                - **Grammar**: [HSK4-11-似乎](../HSK4-11-似乎.md)

                ## Sentence Exercises

                - 天==似乎==要下雨了。
                  - *Tiān ==sìhū== yào xiàyǔ le.*
                  - It ==seems like== it's going to rain.
                """;

        GrammarSentenceMarkdownParser parser = new GrammarSentenceMarkdownParser();
        assertThatThrownBy(() -> parser.parseContent(noTitle))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing title line");
    }

    @Test
    void shouldThrowExceptionForMissingGrammarLink() {
        String noGrammarLink =
                """
                # 似乎 (sìhū) — 四11

                ## Sentence Exercises

                - 天==似乎==要下雨了。
                  - *Tiān ==sìhū== yào xiàyǔ le.*
                  - It ==seems like== it's going to rain.
                """;

        GrammarSentenceMarkdownParser parser = new GrammarSentenceMarkdownParser();
        assertThatThrownBy(() -> parser.parseContent(noGrammarLink))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing grammar file link");
    }

    @Test
    void shouldThrowExceptionForNoExercises() {
        String noExercises =
                """
                # 似乎 (sìhū) — 四11

                - **Grammar**: [HSK4-11-似乎](../HSK4-11-似乎.md)

                ## Sentence Exercises
                """;

        GrammarSentenceMarkdownParser parser = new GrammarSentenceMarkdownParser();
        assertThatThrownBy(() -> parser.parseContent(noExercises))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No sentence exercises found");
    }

    @Test
    void shouldHandleTitleWithEnDash() {
        String withEnDash =
                """
                # 似乎 (sìhū) – 四11

                - **Grammar**: [HSK4-11-似乎](../HSK4-11-似乎.md)

                ## Sentence Exercises

                - 天==似乎==要下雨了。
                  - *Tiān ==sìhū== yào xiàyǔ le.*
                  - It ==seems like== it's going to rain.
                """;

        GrammarSentenceMarkdownParser parser = new GrammarSentenceMarkdownParser();
        GrammarSentenceFile result = parser.parseContent(withEnDash);

        assertThat(result.grammarPoint()).isEqualTo("四11");
    }

    @Test
    void shouldHandleTitleWithHyphen() {
        String withHyphen =
                """
                # 似乎 (sìhū) - 四11

                - **Grammar**: [HSK4-11-似乎](../HSK4-11-似乎.md)

                ## Sentence Exercises

                - 天==似乎==要下雨了。
                  - *Tiān ==sìhū== yào xiàyǔ le.*
                  - It ==seems like== it's going to rain.
                """;

        GrammarSentenceMarkdownParser parser = new GrammarSentenceMarkdownParser();
        GrammarSentenceFile result = parser.parseContent(withHyphen);

        assertThat(result.grammarPoint()).isEqualTo("四11");
    }

    @Test
    void shouldParseNonHskGrammarPoint() {
        String nonHsk =
                """
                # 岂不 (qǐbù) — NON-HSK

                - **Grammar**: [NON-HSK-岂不](../NON-HSK-岂不.md)

                ## Sentence Exercises

                - ==岂不==很好？
                  - *==Qǐbù== hěn hǎo?*
                  - Wouldn't that ==be great==?
                """;

        GrammarSentenceMarkdownParser parser = new GrammarSentenceMarkdownParser();
        GrammarSentenceFile result = parser.parseContent(nonHsk);

        assertThat(result.grammarPoint()).isEqualTo("NON-HSK");
    }

    @Test
    void shouldHandleMultiCharacterWord() {
        String multiChar =
                """
                # 以便 (yǐbiàn) — 五23

                - **Grammar**: [HSK5-23-以便](../HSK5-23-以便.md)
                - **Characters**: [以便](../characters/以便.md)

                ## Sentence Exercises

                - 请提前通知我，==以便==我做好准备。
                  - *Qǐng tíqián tōngzhī wǒ, ==yǐbiàn== wǒ zuò hǎo zhǔnbèi.*
                  - Please notify me in advance ==so that== I can be prepared.
                """;

        GrammarSentenceMarkdownParser parser = new GrammarSentenceMarkdownParser();
        GrammarSentenceFile result = parser.parseContent(multiChar);

        assertThat(result.word()).isEqualTo("以便");
        assertThat(result.pinyin()).isEqualTo("yǐbiàn");
        assertThat(result.grammarPoint()).isEqualTo("五23");
    }
}
