package com.zhlearn.infrastructure.explanation;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.zhlearn.domain.model.ExplanationEntry;

class ExplanationMarkdownParserTest {

    private final ExplanationMarkdownParser parser = new ExplanationMarkdownParser();

    private static final String WORD_ENTRY =
            """
            # Word Analysis: 水域

            ## Original Term
            水域

            ## Pinyin
            shuǐ yù

            ## Literal Translation
            "Waters / water area"

            ## Character Breakdown

            ### 水 (shuǐ) — "water"
            - Water, liquid, fluid

            ### 域 (yù) — "area / region / territory"
            - A defined area, zone, or region

            ## Meaning & Usage

            **水域** (shuǐ yù) = **waters / water area**

            ### Common Usages

            - **这片水域** (zhè piàn shuǐ yù): this body of water

            ## Contextual Note (Crimson Desert)

            In a game like Crimson Desert, 水域 likely refers to bodies of water on the map.

            ---
            """;

    private static final String SENTENCE_ENTRY =
            """
            # Sentence Analysis: 在这行混，能活下来才是最大的本事

            ## Pinyin

            Zài zhè háng hùn, néng huó xià lái cái shì zuì dà de běn shi

            ## Literal Translation

            "In this trade, being able to survive is the greatest ability."

            ## Grammar Breakdown

            ### 在这行混 (zài zhè háng hùn)

            - **在** (zài): in, at

            ## Meaning

            **"In this line of work, survival itself is the greatest achievement."**

            ## Cultural Context

            This type of expression is common in martial arts fiction.

            ---
            """;

    private static final String IDIOM_ENTRY =
            """
            # Idiom Analysis: 受益匪浅

            ## Original Term
            受益匪浅

            ## Pinyin
            shòu yì fěi qiǎn

            ## Literal Translation
            "Received benefit [is] not shallow."

            ## Character Breakdown

            ### 受益 (shòu yì) — "to benefit"
            - **受** (shòu): to receive
            - **益** (yì): benefit

            ## Meaning & Usage

            **受益匪浅** = **"to benefit greatly"**

            ## Contextual Note (Crimson Desert)

            Used when an NPC imparts wisdom.
            """;

    @Test
    void shouldParseWordEntry() {
        List<ExplanationEntry> entries = parser.parseContent(WORD_ENTRY);
        assertThat(entries).hasSize(1);

        ExplanationEntry entry = entries.getFirst();
        assertThat(entry.term()).isEqualTo("水域");
        assertThat(entry.pinyin()).isEqualTo("shuǐ yù");
        assertThat(entry.entryType()).isEqualTo("word");
    }

    @Test
    void shouldParseSentenceEntry() {
        List<ExplanationEntry> entries = parser.parseContent(SENTENCE_ENTRY);
        assertThat(entries).hasSize(1);

        ExplanationEntry entry = entries.getFirst();
        assertThat(entry.term()).isEqualTo("在这行混，能活下来才是最大的本事");
        assertThat(entry.pinyin())
                .isEqualTo("Zài zhè háng hùn, néng huó xià lái cái shì zuì dà de běn shi");
        assertThat(entry.entryType()).isEqualTo("sentence");
    }

    @Test
    void shouldParseIdiomEntry() {
        List<ExplanationEntry> entries = parser.parseContent(IDIOM_ENTRY);
        assertThat(entries).hasSize(1);

        ExplanationEntry entry = entries.getFirst();
        assertThat(entry.term()).isEqualTo("受益匪浅");
        assertThat(entry.pinyin()).isEqualTo("shòu yì fěi qiǎn");
        assertThat(entry.entryType()).isEqualTo("idiom");
    }

    @Test
    void shouldParseMultipleEntries() {
        String combined = WORD_ENTRY + "\n" + SENTENCE_ENTRY + "\n" + IDIOM_ENTRY;
        List<ExplanationEntry> entries = parser.parseContent(combined);
        assertThat(entries).hasSize(3);
        assertThat(entries.get(0).entryType()).isEqualTo("word");
        assertThat(entries.get(1).entryType()).isEqualTo("sentence");
        assertThat(entries.get(2).entryType()).isEqualTo("idiom");
    }

    @Test
    void shouldExcludePinyinFromExplanation() {
        List<ExplanationEntry> entries = parser.parseContent(WORD_ENTRY);
        ExplanationEntry entry = entries.getFirst();
        // Explanation should not contain the pinyin value itself as a section
        assertThat(entry.explanation()).doesNotContain("## Pinyin");
        // But should contain the rest
        assertThat(entry.explanation()).contains("## Literal Translation");
        assertThat(entry.explanation()).contains("## Character Breakdown");
        assertThat(entry.explanation()).contains("## Meaning & Usage");
    }

    @Test
    void shouldStripSeparatorsFromExplanation() {
        List<ExplanationEntry> entries = parser.parseContent(WORD_ENTRY);
        assertThat(entries.getFirst().explanation()).doesNotContain("---");
    }

    @Test
    void shouldHandleEntriesWithoutSeparator() {
        // IDIOM_ENTRY has no --- at the end
        List<ExplanationEntry> entries = parser.parseContent(IDIOM_ENTRY);
        assertThat(entries).hasSize(1);
        assertThat(entries.getFirst().term()).isEqualTo("受益匪浅");
    }

    @Test
    void shouldHandleMultipleEntriesWithoutSeparator() {
        // Two entries back to back with no --- between them
        String noSeparator =
                """
                # Word Analysis: 测试

                ## Pinyin
                cè shì

                ## Meaning & Usage

                **Test word.**

                # Word Analysis: 另一个

                ## Pinyin
                lìng yī gè

                ## Meaning & Usage

                **Another word.**
                """;
        List<ExplanationEntry> entries = parser.parseContent(noSeparator);
        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).term()).isEqualTo("测试");
        assertThat(entries.get(1).term()).isEqualTo("另一个");
    }
}
