package com.zhlearn.infrastructure.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MarkdownUtilsTest {

    @Test
    void shouldNormalizeRightArrow() {
        String input = "A $\\rightarrow$ B";
        assertThat(MarkdownUtils.normalizeArrows(input)).isEqualTo("A → B");
    }

    @Test
    void shouldNormalizeToArrow() {
        String input = "A $\\to$ B";
        assertThat(MarkdownUtils.normalizeArrows(input)).isEqualTo("A → B");
    }

    @Test
    void shouldNormalizeLeftArrow() {
        String input = "A $\\leftarrow$ B";
        assertThat(MarkdownUtils.normalizeArrows(input)).isEqualTo("A ← B");
    }

    @Test
    void shouldNormalizeBidirectionalArrow() {
        String input = "A $\\leftrightarrow$ B";
        assertThat(MarkdownUtils.normalizeArrows(input)).isEqualTo("A ↔ B");
    }

    @Test
    void shouldNormalizeDoubleRightArrow() {
        String input = "A $\\Rightarrow$ B";
        assertThat(MarkdownUtils.normalizeArrows(input)).isEqualTo("A ⇒ B");
    }

    @Test
    void shouldNormalizeDoubleLeftArrow() {
        String input = "A $\\Leftarrow$ B";
        assertThat(MarkdownUtils.normalizeArrows(input)).isEqualTo("A ⇐ B");
    }

    @Test
    void shouldNormalizeDoubleBidirectionalArrow() {
        String input = "A $\\Leftrightarrow$ B";
        assertThat(MarkdownUtils.normalizeArrows(input)).isEqualTo("A ⇔ B");
    }

    @Test
    void shouldNormalizeMultipleArrows() {
        String input = "A $\\to$ B $\\rightarrow$ C";
        assertThat(MarkdownUtils.normalizeArrows(input)).isEqualTo("A → B → C");
    }

    @Test
    void shouldReturnNullForNullInput() {
        assertThat(MarkdownUtils.normalizeArrows(null)).isNull();
    }

    @Test
    void shouldReturnUnchangedIfNoArrows() {
        String input = "This is a normal sentence without LaTeX arrows.";
        assertThat(MarkdownUtils.normalizeArrows(input)).isEqualTo(input);
    }

    @Test
    void shouldHandleRealWorldExample() {
        String input = """
                * **Logic**: A happens $\\rightarrow$ B happens (and B is unexpected/extreme).
                * **Semantic Shift**: "Whiskers" $\\to$ "And then/And yet".""";
        String expected = """
                * **Logic**: A happens → B happens (and B is unexpected/extreme).
                * **Semantic Shift**: "Whiskers" → "And then/And yet".""";
        assertThat(MarkdownUtils.normalizeArrows(input)).isEqualTo(expected);
    }
}
