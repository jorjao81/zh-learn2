package com.zhlearn.domain.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ExplanationEntryTest {

    @Test
    void shouldCreateValidEntry() {
        var entry = new ExplanationEntry("水域", "shuǐ yù", "## Literal Translation\n...", "word");
        assertThat(entry.term()).isEqualTo("水域");
        assertThat(entry.pinyin()).isEqualTo("shuǐ yù");
        assertThat(entry.entryType()).isEqualTo("word");
    }

    @Test
    void shouldRejectNullTerm() {
        assertThatThrownBy(() -> new ExplanationEntry(null, "shuǐ yù", "content", "word"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlankPinyin() {
        assertThatThrownBy(() -> new ExplanationEntry("水域", " ", "content", "word"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlankExplanation() {
        assertThatThrownBy(() -> new ExplanationEntry("水域", "shuǐ yù", "", "word"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullEntryType() {
        assertThatThrownBy(() -> new ExplanationEntry("水域", "shuǐ yù", "content", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
