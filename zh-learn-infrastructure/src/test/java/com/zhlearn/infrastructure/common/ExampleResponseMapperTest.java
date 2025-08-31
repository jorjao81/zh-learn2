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
                examples:
                  - hanzi: "我很喜欢这部电影"
                    pinyin: "wǒ hěn xǐhuān zhè bù diànyǐng"
                    translation: "I really like this movie"
                  - hanzi: "她喜欢听音乐"
                    pinyin: "tā xǐhuān tīng yīnyuè"
                    translation: "She likes listening to music"
              - meaning: "to prefer"
                examples:
                  - hanzi: "我更喜欢茶"
                    pinyin: "wǒ gèng xǐhuān chá"
                    translation: "I prefer tea"
            """;
        
        Example result = mapper.apply(yamlResponse);
        
        assertThat(result.usages()).hasSize(3);
        
        Example.Usage firstUsage = result.usages().get(0);
        assertThat(firstUsage.sentence()).isEqualTo("我很喜欢这部电影");
        assertThat(firstUsage.pinyin()).isEqualTo("wǒ hěn xǐhuān zhè bù diànyǐng");
        assertThat(firstUsage.translation()).isEqualTo("I really like this movie");
        assertThat(firstUsage.context()).isEqualTo("to like");
        
        Example.Usage lastUsage = result.usages().get(2);
        assertThat(lastUsage.context()).isEqualTo("to prefer");
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
                examples:
                  - hanzi: "我很喜欢这部电影"
                    pinyin: "wǒ hěn xǐhuān zhè bù diànyǐng"
                    translation: "I really like this movie"
            ```
            """;
        
        Example result = mapper.apply(yamlWithMarkdown);
        
        assertThat(result.usages()).hasSize(1);
        Example.Usage usage = result.usages().get(0);
        assertThat(usage.sentence()).isEqualTo("我很喜欢这部电影");
        assertThat(usage.pinyin()).isEqualTo("wǒ hěn xǐhuān zhè bù diànyǐng");
        assertThat(usage.translation()).isEqualTo("I really like this movie");
        assertThat(usage.context()).isEqualTo("to like");
    }
    
    @Test
    void shouldHandleGenericMarkdownCodeBlocks() {
        String yamlWithGenericMarkdown = """
            ```
            response:
              - meaning: "to prefer"
                examples:
                  - hanzi: "我更喜欢茶"
                    pinyin: "wǒ gèng xǐhuān chá"
                    translation: "I prefer tea"
            ```
            """;
        
        Example result = mapper.apply(yamlWithGenericMarkdown);
        
        assertThat(result.usages()).hasSize(1);
        Example.Usage usage = result.usages().get(0);
        assertThat(usage.sentence()).isEqualTo("我更喜欢茶");
        assertThat(usage.pinyin()).isEqualTo("wǒ gèng xǐhuān chá");
        assertThat(usage.translation()).isEqualTo("I prefer tea");
        assertThat(usage.context()).isEqualTo("to prefer");
    }
}