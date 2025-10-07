package com.zhlearn.infrastructure.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.infrastructure.cache.CachedChatModel;

import dev.langchain4j.model.chat.ChatModel;

/** Provider using a minimal OpenAI-compatible client for z.ai (GLM). */
public class ZhipuChatModelProvider<T> {

    private static final Logger log = LoggerFactory.getLogger(ZhipuChatModelProvider.class);

    private final ChatModel chatModel;
    private final String promptTemplate;
    private final List<String> examples;
    private final ProviderConfig<T> config;

    public ZhipuChatModelProvider(ProviderConfig<T> config) {
        this.config = config;
        this.chatModel = createChatModel(config);
        this.promptTemplate = loadPromptTemplate(config.getTemplateResourcePath());
        this.examples = loadExamples(config.getExamplesResourcePath());
    }

    private ChatModel createChatModel(ProviderConfig<T> config) {
        ChatModel base =
                new ZaiOpenAiChatModel(
                        config.getApiKey(),
                        config.getBaseUrl(),
                        config.getModelName(),
                        config.getTemperature(),
                        config.getMaxTokens());
        return new CachedChatModel(base, config);
    }

    public String getName() {
        return config.getProviderName();
    }

    public T process(Hanzi word) {
        return processWithContext(word, Optional.empty());
    }

    public T process(Hanzi word, Optional<String> definition) {
        return processWithContext(word, definition);
    }

    public T processWithContext(Hanzi word, Optional<String> additionalContext) {
        try {
            String prompt = buildPrompt(word.characters(), additionalContext.orElse(null));

            long startTime = System.currentTimeMillis();
            String timestamp = Instant.now().toString();
            log.info(
                    "[AI Call] {} for '{}': sent at {}",
                    config.getProviderName(),
                    word.characters(),
                    timestamp);

            String response = chatModel.chat(prompt);

            long duration = System.currentTimeMillis() - startTime;
            log.info(
                    "[AI Call] {} for '{}': received after {}ms",
                    config.getProviderName(),
                    word.characters(),
                    duration);

            return config.getResponseMapper().apply(response);
        } catch (Exception e) {
            throw new RuntimeException(config.getErrorMessagePrefix() + ": " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String chineseWord) {
        return buildPrompt(chineseWord, null);
    }

    private String buildPrompt(String chineseWord, String additionalContext) {
        String allExamples = String.join("\n\n", examples);

        String contextSection = "";
        if (additionalContext != null && !additionalContext.trim().isEmpty()) {
            contextSection = "Known context: " + additionalContext;
        }

        return promptTemplate
                .replace("{WORD}", chineseWord)
                .replace("{EXAMPLES}", allExamples)
                .replace("{CONTEXT}", contextSection);
    }

    private String loadPromptTemplate(String templateResourcePath) {
        try (InputStream is = getClass().getResourceAsStream(templateResourcePath)) {
            return new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> loadExamples(String examplesResourcePath) {
        List<String> exampleList = new ArrayList<>();
        for (int i = 1; i <= 99; i++) {
            String filename = String.format("example-%02d.html", i);
            String resourcePath = examplesResourcePath + filename;
            try (InputStream exampleIs = getClass().getResourceAsStream(resourcePath)) {
                if (exampleIs != null) {
                    String content = new String(exampleIs.readAllBytes(), StandardCharsets.UTF_8);
                    exampleList.add(content);
                } else {
                    log.debug("No more example files found, stopped at: {}", resourcePath);
                    break;
                }
            } catch (IOException e) {
                log.warn("Failed to load example file {}: {}", filename, e.getMessage());
            }
        }
        if (exampleList.isEmpty()) {
            log.warn(
                    "No examples found at path {}. This might lead to suboptimal results.",
                    examplesResourcePath);
        }
        return exampleList;
    }
}
