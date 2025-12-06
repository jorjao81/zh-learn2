package com.zhlearn.cli;

import com.zhlearn.application.service.WordAnalysisServiceImpl;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderConfiguration;
import com.zhlearn.domain.model.WordAnalysis;
import com.zhlearn.domain.provider.AudioProvider;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "word", description = "Analyze a Chinese word with AI-powered analysis")
public class WordCommand implements Runnable {

    @Parameters(index = "0", description = "The Chinese word to analyze")
    private String chineseWord;

    @Option(
            names = {"--pinyin-provider"},
            description = "Set specific provider for pinyin. Available: pinyin4j, dummy",
            defaultValue = "pinyin4j")
    private String pinyinProvider;

    @Option(
            names = {"--definition-provider"},
            description = "Set specific provider for definition. Available: dummy",
            defaultValue = "dummy")
    private String definitionProvider;

    @Option(
            names = {"--definition-formatter-provider"},
            description =
                    "Set specific provider for definition formatting. Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo, openrouter, gemini-2.5-flash, gemini-2.5-pro, gemini-3-pro-preview",
            defaultValue = "dummy")
    private String definitionFormatterProvider;

    @Option(
            names = {"--decomposition-provider"},
            description =
                    "Set specific provider for structural decomposition. Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo, openrouter, gemini-2.5-flash, gemini-2.5-pro, gemini-3-pro-preview",
            defaultValue = "dummy")
    private String decompositionProvider;

    @Option(
            names = {"--example-provider"},
            description =
                    "Set specific provider for examples. Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo, openrouter, gemini-2.5-flash, gemini-2.5-pro, gemini-3-pro-preview",
            defaultValue = "dummy")
    private String exampleProvider;

    @Option(
            names = {"--explanation-provider"},
            description =
                    "Set specific provider for explanation. Available: dummy, deepseek-chat, glm-4-flash, glm-4.5, qwen-max, qwen-plus, qwen-turbo, openrouter, gemini-2.5-flash, gemini-2.5-pro, gemini-3-pro-preview",
            defaultValue = "dummy")
    private String explanationProvider;

    @Option(
            names = {"--audio-provider"},
            description =
                    "Set specific provider for audio pronunciation. Available: anki, forvo, qwen-tts",
            defaultValue = "anki")
    private String audioProvider;

    @Option(
            names = {"--model"},
            description =
                    "AI model to use with provider (e.g., for OpenRouter: gpt-4, claude-3-sonnet, llama-2-70b-chat)",
            defaultValue = "")
    private String model;

    @Option(
            names = {"--raw", "--raw-output"},
            description = "Display raw HTML content instead of formatted output")
    private boolean rawOutput = false;

    @picocli.CommandLine.ParentCommand private MainCommand parent;

    @Override
    public void run() {
        // Create service with providers selected via CLI options
        WordAnalysisServiceImpl wordAnalysisService =
                new WordAnalysisServiceImpl(
                        parent.createExampleProvider(exampleProvider, model),
                        parent.createExplanationProvider(explanationProvider, model),
                        parent.createDecompositionProvider(decompositionProvider, model),
                        parent.createPinyinProvider(pinyinProvider),
                        parent.createDefinitionProvider(definitionProvider),
                        parent.createDefinitionFormatterProvider(
                                definitionFormatterProvider, model),
                        null, // No definition generation needed for word command
                        resolveAudioProvider(audioProvider));

        ProviderConfiguration config =
                new ProviderConfiguration(
                        exampleProvider,
                        pinyinProvider,
                        definitionProvider,
                        definitionFormatterProvider,
                        decompositionProvider,
                        exampleProvider,
                        explanationProvider,
                        audioProvider);

        Hanzi word = new Hanzi(chineseWord);
        WordAnalysis analysis = wordAnalysisService.getCompleteAnalysis(word, config);

        if (rawOutput) {
            parent.getAnalysisPrinter().printRaw(analysis);
        } else {
            parent.getAnalysisPrinter().printFormatted(analysis);
        }
    }

    private AudioProvider resolveAudioProvider(String providerName) {
        return parent.getAudioProviders().stream()
                .filter(provider -> provider.getName().equals(providerName))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Unknown audio provider: " + providerName));
    }
}
