package com.zhlearn.application.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefinitionImageFormatterTest {

    private DefinitionImageFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new DefinitionImageFormatter();
    }

    @Test
    void shouldReturnDefinitionUnchangedWhenNoImages() {
        String definition = "学习 (xuéxí): to study; to learn";

        String result = formatter.formatWithImages(definition, List.of());

        assertThat(result).isEqualTo(definition);
    }

    @Test
    void shouldReturnDefinitionUnchangedWhenNullImages() {
        String definition = "学习 (xuéxí): to study; to learn";

        String result = formatter.formatWithImages(definition, null);

        assertThat(result).isEqualTo(definition);
    }

    @Test
    void shouldFormatWithSingleImage() {
        String definition = "学习 (xuéxí): to study; to learn";
        List<Path> images = List.of(Path.of("/anki/media/zh-学习-1.jpg"));

        String result = formatter.formatWithImages(definition, images);

        assertThat(result)
                .contains("<div class=\"definition\">")
                .contains("学习 (xuéxí): to study; to learn")
                .contains("</div>")
                .contains("<div class=\"images\">")
                .contains("<img src=\"zh-学习-1.jpg\" class=\"word-image\">")
                .doesNotContain("/anki/media/"); // Should use filename only
    }

    @Test
    void shouldFormatWithMultipleImages() {
        String definition = "学习 (xuéxí): to study; to learn";
        List<Path> images =
                List.of(
                        Path.of("/anki/media/zh-学习-1.jpg"),
                        Path.of("/anki/media/zh-学习-2.jpg"),
                        Path.of("/anki/media/zh-学习-3.jpg"));

        String result = formatter.formatWithImages(definition, images);

        assertThat(result)
                .contains("<div class=\"definition\">")
                .contains("学习 (xuéxí): to study; to learn")
                .contains("</div>")
                .contains("<div class=\"images\">")
                .contains("<img src=\"zh-学习-1.jpg\" class=\"word-image\">")
                .contains("<img src=\"zh-学习-2.jpg\" class=\"word-image\">")
                .contains("<img src=\"zh-学习-3.jpg\" class=\"word-image\">");
    }

    @Test
    void shouldEscapeHtmlEntities() {
        String definition = "Test <tag> & \"quotes\" & 'apostrophe' > symbols";
        List<Path> images = List.of(Path.of("/anki/media/zh-test-1.jpg"));

        String result = formatter.formatWithImages(definition, images);

        assertThat(result)
                .contains(
                        "Test &lt;tag&gt; &amp; &quot;quotes&quot; &amp; &#39;apostrophe&#39; &gt; symbols")
                .doesNotContain("<tag>")
                .doesNotContain("& \"");
    }

    @Test
    void shouldUseFilenameOnlyNotFullPath() {
        String definition = "大象 (dàxiàng): elephant";
        List<Path> images = List.of(Path.of("/path/to/anki/media/zh-大象-1.jpg"));

        String result = formatter.formatWithImages(definition, images);

        assertThat(result).contains("zh-大象-1.jpg").doesNotContain("/path/to/anki/media/");
    }

    @Test
    void shouldHandleNullDefinition() {
        List<Path> images = List.of(Path.of("/anki/media/zh-test-1.jpg"));

        String result = formatter.formatWithImages(null, images);

        assertThat(result)
                .contains("<div class=\"definition\">")
                .contains("</div>")
                .contains("<div class=\"images\">");
    }

    @Test
    void shouldGenerateCorrectHtmlStructure() {
        String definition = "学习 (xuéxí): to study";
        List<Path> images = List.of(Path.of("zh-学习-1.jpg"), Path.of("zh-学习-2.jpg"));

        String result = formatter.formatWithImages(definition, images);

        // Verify structure
        assertThat(result)
                .startsWith("<div class=\"definition\">")
                .endsWith("</div>")
                .contains("\n</div>\n<div class=\"images\">\n");
    }
}
