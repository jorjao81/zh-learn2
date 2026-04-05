package com.zhlearn.infrastructure.explanation;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MarkdownToHtmlConverterTest {

    private final MarkdownToHtmlConverter converter = new MarkdownToHtmlConverter();

    @Test
    void shouldConvertH2() {
        assertThat(converter.convert("## Meaning & Usage")).contains("<h3>Meaning & Usage</h3>");
    }

    @Test
    void shouldConvertH3() {
        assertThat(converter.convert("### 水 (shuǐ) — \"water\""))
                .contains("<h4>水 (shuǐ) — \"water\"</h4>");
    }

    @Test
    void shouldConvertBold() {
        assertThat(converter.convert("**water**")).contains("<strong>water</strong>");
    }

    @Test
    void shouldConvertItalic() {
        assertThat(converter.convert("*italic text*")).contains("<em>italic text</em>");
    }

    @Test
    void shouldConvertUnorderedList() {
        String md = "- item one\n- item two";
        String html = converter.convert(md);
        assertThat(html).contains("<ul>");
        assertThat(html).contains("<li>item one</li>");
        assertThat(html).contains("<li>item two</li>");
        assertThat(html).contains("</ul>");
    }

    @Test
    void shouldConvertOrderedList() {
        String md = "1. first\n2. second";
        String html = converter.convert(md);
        assertThat(html).contains("<ol>");
        assertThat(html).contains("<li>first</li>");
        assertThat(html).contains("<li>second</li>");
        assertThat(html).contains("</ol>");
    }

    @Test
    void shouldConvertParagraphs() {
        String html = converter.convert("Some text here.");
        assertThat(html).contains("<p>Some text here.</p>");
    }

    @Test
    void shouldHandleEmptyInput() {
        assertThat(converter.convert("")).isEmpty();
        assertThat(converter.convert(null)).isEmpty();
    }

    @Test
    void shouldConvertBoldWithinListItems() {
        String md = "- **水域** (shuǐ yù): waters";
        String html = converter.convert(md);
        assertThat(html).contains("<strong>水域</strong>");
        assertThat(html).contains("<li>");
    }
}
