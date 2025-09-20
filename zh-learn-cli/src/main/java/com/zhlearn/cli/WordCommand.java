package com.zhlearn.cli;

import com.zhlearn.application.service.WordAnalysisServiceImpl;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderConfiguration;
import com.zhlearn.domain.model.WordAnalysis;
import com.zhlearn.domain.provider.AudioProvider;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "word",
    description = "Analyze a Chinese word with AI-powered analysis"
)
public class WordCommand implements Runnable {
    
    @Parameters(index = "0", description = "The Chinese word to analyze")
    private String chineseWord;
    
    @Option(names = {"--pinyin-provider"}, description = "Set specific provider for pinyin. Available: pinyin4j, dummy", defaultValue = "pinyin4j")
    private String pinyinProvider;

    @Option(names = {"--definition-provider"}, description = "Set specific provider for definition. Available: dummy", defaultValue = "dummy")
    private String definitionProvider;

    @Option(names = {"--decomposition-provider"}, description = "Set specific provider for structural decomposition. Available: dummy, deepseek-chat, qwen-max, qwen-plus, qwen-turbo, glm-4-flash, glm-4.5", defaultValue = "deepseek-chat")
    private String decompositionProvider;

    @Option(names = {"--example-provider"}, description = "Set specific provider for examples. Available: dummy, deepseek-chat, qwen-max, qwen-plus, qwen-turbo, glm-4-flash, glm-4.5", defaultValue = "deepseek-chat")
    private String exampleProvider;

    @Option(names = {"--explanation-provider"}, description = "Set specific provider for explanation. Available: dummy, deepseek-chat, qwen-max, qwen-plus, qwen-turbo, glm-4-flash, glm-4.5", defaultValue = "deepseek-chat")
    private String explanationProvider;
    
    @Option(names = {"--audio-provider"}, description = "Set specific provider for audio pronunciation. Available: anki, forvo, qwen-tts", defaultValue = "anki")
    private String audioProvider;
    
    @Option(names = {"--raw", "--raw-output"}, description = "Display raw HTML content instead of formatted output")
    private boolean rawOutput = false;

    @Option(names = {"--open-router-model"}, description = "Set the model for OpenRouter provider")
    private String openRouterModel;

    @picocli.CommandLine.ParentCommand
    private MainCommand parent;

    @Override
    public void run() {
        // Create service with providers selected via CLI options
        WordAnalysisServiceImpl wordAnalysisService = new WordAnalysisServiceImpl(
            parent.createExampleProvider(exampleProvider, openRouterModel),
            parent.createExplanationProvider(explanationProvider, openRouterModel),
            parent.createDecompositionProvider(decompositionProvider, openRouterModel),
            parent.createPinyinProvider(pinyinProvider),
            parent.createDefinitionProvider(definitionProvider),
            resolveAudioProvider(audioProvider)
        );

        ProviderConfiguration config = new ProviderConfiguration(
            exampleProvider,
            pinyinProvider,
            definitionProvider,
            decompositionProvider,
            exampleProvider,
            explanationProvider,
            audioProvider
        );

        Hanzi word = new Hanzi(chineseWord);
        WordAnalysis analysis = wordAnalysisService.getCompleteAnalysis(word, config);

        if (rawOutput) {
            AnalysisPrinter.printRaw(analysis);
        } else {
            AnalysisPrinter.printFormatted(analysis);
        }
    }
    
    private AudioProvider resolveAudioProvider(String providerName) {
        return parent.getAudioProviders().stream()
            .filter(provider -> provider.getName().equals(providerName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown audio provider: " + providerName));
    }
}
