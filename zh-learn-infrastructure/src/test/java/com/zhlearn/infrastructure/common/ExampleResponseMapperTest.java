package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Example;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExampleResponseMapperTest {
    
    private final ExampleResponseMapper mapper = new ExampleResponseMapper();
    
    @Test
    void shouldParseValidYamlResponse() {
        String yamlResponse = """
            words:
              - meaning: "to like"
                pinyin: "xǐhuān"
                examples:
                  - hanzi: "喜欢"
                    pinyin: "xǐhuān"
                    translation: "to like; to love"
                    breakdown: "To like (喜) and be happy (欢) about something."
                  - hanzi: "喜爱"
                    pinyin: "xǐài"
                    translation: "to love; to like"
                    breakdown: "To like (喜) and love (爱) someone or something."
              - meaning: "to prefer"
                pinyin: "gèng xǐhuān"
                examples:
                  - hanzi: "更喜欢"
                    pinyin: "gèng xǐhuān"
                    translation: "to prefer"
                    breakdown: "More (更) liking (喜欢) one thing over another."
            """;
        
        Example result = mapper.apply(yamlResponse);
        
        assertThat(result.usages()).hasSize(3);
        
        Example.Usage firstUsage = result.usages().get(0);
        assertThat(firstUsage.sentence()).isEqualTo("喜欢");
        assertThat(firstUsage.pinyin()).isEqualTo("xǐhuān");
        assertThat(firstUsage.translation()).isEqualTo("to like; to love");
        assertThat(firstUsage.context()).isEqualTo("to like (xǐhuān)");
        assertThat(firstUsage.breakdown()).isEqualTo("To like (喜) and be happy (欢) about something.");
        
        Example.Usage lastUsage = result.usages().get(2);
        assertThat(lastUsage.context()).isEqualTo("to prefer (gèng xǐhuān)");
        assertThat(lastUsage.breakdown()).isEqualTo("More (更) liking (喜欢) one thing over another.");
    }
    
    @Test
    void shouldHandleEmptyResponse() {
        String yamlResponse = """
            words: []
            """;
        
        Example result = mapper.apply(yamlResponse);
        
        assertThat(result.usages()).isEmpty();
    }
    
    @Test
    void shouldHandleInvalidYaml() {
        String invalidYaml = "invalid yaml content [[[";
        assertThrows(RuntimeException.class, () -> mapper.apply(invalidYaml));
    }
    
    @Test
    void shouldHandleMissingResponseKey() {
        String yamlResponse = """
            examples:
              - hanzi: "test"
                pinyin: "test"
                translation: "test"
            """;
        
        Example result = mapper.apply(yamlResponse);
        
        assertThat(result.usages()).isEmpty();
    }
    
    @Test
    void shouldHandleYamlWrappedInMarkdownCodeBlocks() {
        String yamlWithMarkdown = """
            ```yaml
            words:
              - meaning: "to like"
                pinyin: "xǐhuān"
                examples:
                  - hanzi: "喜欢"
                    pinyin: "xǐhuān"
                    translation: "to like; to love"
                    breakdown: "To like (喜) and be happy (欢) about something."
            ```
            """;
        
        Example result = mapper.apply(yamlWithMarkdown);
        
        assertThat(result.usages()).hasSize(1);
        Example.Usage usage = result.usages().get(0);
        assertThat(usage.sentence()).isEqualTo("喜欢");
        assertThat(usage.pinyin()).isEqualTo("xǐhuān");
        assertThat(usage.translation()).isEqualTo("to like; to love");
        assertThat(usage.context()).isEqualTo("to like (xǐhuān)");
        assertThat(usage.breakdown()).isEqualTo("To like (喜) and be happy (欢) about something.");
    }
    
    @Test
    void shouldHandleGenericMarkdownCodeBlocks() {
        String yamlWithGenericMarkdown = """
            ```
            words:
              - meaning: "to prefer"
                pinyin: "gèng xǐhuān"
                examples:
                  - hanzi: "更喜欢"
                    pinyin: "gèng xǐhuān"
                    translation: "to prefer"
                    breakdown: "More (更) liking (喜欢) one thing over another."
            ```
            """;
        
        Example result = mapper.apply(yamlWithGenericMarkdown);
        
        assertThat(result.usages()).hasSize(1);
        Example.Usage usage = result.usages().get(0);
        assertThat(usage.sentence()).isEqualTo("更喜欢");
        assertThat(usage.pinyin()).isEqualTo("gèng xǐhuān");
        assertThat(usage.translation()).isEqualTo("to prefer");
        assertThat(usage.context()).isEqualTo("to prefer (gèng xǐhuān)");
        assertThat(usage.breakdown()).isEqualTo("More (更) liking (喜欢) one thing over another.");
    }

    @Test
    void shouldParseOptionalPhoneticSeries() {
        String yaml = """
            words:
              - meaning: "to learn"
                pinyin: "xué"
                examples:
                  - hanzi: "学习"
                    pinyin: "xuéxí"
                    translation: "to study"
                    breakdown: "study (学) + practice (习)"
            phonetic_series:
              - hanzi: "觉"
                pinyin: "jué"
                meaning: "to feel; sense"
              - hanzi: "较"
                pinyin: "jiào"
                meaning: "compare; fairly"
            """;

        Example result = mapper.apply(yaml);
        assertThat(result.phoneticSeries()).hasSize(2);
        assertThat(result.phoneticSeries().get(0).hanzi()).isEqualTo("觉");
        assertThat(result.phoneticSeries().get(0).pinyin()).isEqualTo("jué");
        assertThat(result.phoneticSeries().get(0).meaning()).isEqualTo("to feel; sense");
    }

    // No standalone sentence support
}
