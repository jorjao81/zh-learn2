package com.zhlearn.infrastructure.common;

import com.zhlearn.domain.model.Example;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ExampleResponseMapperTest {
    
    private final ExampleResponseMapper mapper = new ExampleResponseMapper();
    
    @Test
    void shouldParseValidYamlResponse() {
        String yamlResponse = """
            response:
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
            response: []
            """;
        
        Example result = mapper.apply(yamlResponse);
        
        assertThat(result.usages()).isEmpty();
    }
    
    @Test
    void shouldHandleInvalidYaml() {
        String invalidYaml = "invalid yaml content [[[";
        
        Example result = mapper.apply(invalidYaml);
        
        assertThat(result.usages()).isEmpty();
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
            response:
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
            response:
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
}