package com.zhlearn.cli;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MainCommandProvidersTest {

    @Test
    void defaultProvidersIncludeExpectedAiModels() {
        MainCommand main = new MainCommand();
        assertThat(main.getExampleProviders().keySet())
            .contains("deepseek-chat", "gpt-5-nano", "qwen3-max", "qwen3-plus", "qwen3-flash", "glm-4-flash", "glm-4.5", "dummy");
        assertThat(main.getExplanationProviders().keySet())
            .contains("deepseek-chat", "gpt-5-nano", "qwen3-max", "qwen3-plus", "qwen3-flash", "glm-4-flash", "glm-4.5", "dummy");
        assertThat(main.getDecompositionProviders().keySet())
            .contains("deepseek-chat", "gpt-5-nano", "qwen3-max", "qwen3-plus", "qwen3-flash", "glm-4-flash", "glm-4.5", "dummy");
    }

    @Test
    void similaritySuggestionsIncludeExpectedMatches() {
        MainCommand main = new MainCommand();
        assertThat(main.findSimilarProviders("deepseek")).contains("deepseek-chat");
        assertThat(main.findSimilarProviders("qwen")).contains("qwen3-max");
        assertThat(main.findSimilarProviders("dummy")).contains("dummy");
    }
}
