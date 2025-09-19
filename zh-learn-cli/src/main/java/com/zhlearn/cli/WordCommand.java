package com.zhlearn.cli;

import com.zhlearn.application.service.WordAnalysisServiceImpl;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderConfiguration;
import com.zhlearn.domain.model.WordAnalysis;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;

@Command(
    name = "word",
    description = "Analyze a Chinese word with AI-powered analysis"
)
public class WordCommand implements Runnable {
    
    @Parameters(index = "0", description = "The Chinese word to analyze")
    private String chineseWord;
    
    @Option(names = {"--provider"}, description = "Set default provider for all services (default: deepseek-chat for AI, pinyin4j for pinyin, dummy for others). Available: dummy, pinyin4j, deepseek-chat, qwen-max, qwen-plus, qwen-turbo, glm-4-flash, glm-4.5")
    private String defaultProvider = "dummy";

    @Option(names = {"--pinyin-provider"}, description = "Set specific provider for pinyin. Available: pinyin4j, dummy")
    private String pinyinProvider;

    @Option(names = {"--definition-provider"}, description = "Set specific provider for definition. Available: dummy")
    private String definitionProvider;

    @Option(names = {"--decomposition-provider"}, description = "Set specific provider for structural decomposition. Available: dummy, deepseek-chat, qwen-max, qwen-plus, qwen-turbo, glm-4-flash, glm-4.5")
    private String decompositionProvider;

    @Option(names = {"--example-provider"}, description = "Set specific provider for examples. Available: dummy, deepseek-chat, qwen-max, qwen-plus, qwen-turbo, glm-4-flash, glm-4.5")
    private String exampleProvider;

    @Option(names = {"--explanation-provider"}, description = "Set specific provider for explanation. Available: dummy, deepseek-chat, qwen-max, qwen-plus, qwen-turbo, glm-4-flash, glm-4.5")
    private String explanationProvider;
    
    @Option(names = {"--audio-provider"}, description = "Set specific provider for audio pronunciation. Available: anki")
    private String audioProvider;
    
    @Option(names = {"--raw", "--raw-output"}, description = "Display raw HTML content instead of formatted output")
    private boolean rawOutput = false;

    @picocli.CommandLine.ParentCommand
    private MainCommand parent;

    @Override
    public void run() {
        // Create service with providers selected via CLI options
        WordAnalysisServiceImpl wordAnalysisService = new WordAnalysisServiceImpl(
            parent.createExampleProvider(exampleProvider),
            parent.createExplanationProvider(explanationProvider),
            parent.createDecompositionProvider(decompositionProvider),
            parent.createPinyinProvider(pinyinProvider),
            parent.createDefinitionProvider(definitionProvider),
            parent.getAudioProvider() // Keep audio as before
        );

        // ProviderConfiguration is used by the analysis service - use actual provider names
        ProviderConfiguration config = new ProviderConfiguration(
            exampleProvider != null ? exampleProvider : "deepseek-chat",
            pinyinProvider != null ? pinyinProvider : "pinyin4j",
            definitionProvider != null ? definitionProvider : "dummy",
            decompositionProvider != null ? decompositionProvider : "deepseek-chat",
            explanationProvider != null ? explanationProvider : "deepseek-chat",
            audioProvider != null ? audioProvider : "anki",
            audioProvider != null ? audioProvider : "anki"
        );

        Hanzi word = new Hanzi(chineseWord);
        WordAnalysis analysis = wordAnalysisService.getCompleteAnalysis(word, config);

        if (rawOutput) {
            AnalysisPrinter.printRaw(analysis);
        } else {
            AnalysisPrinter.printFormatted(analysis);
        }
    }
    
    
    private void printAnalysisFormatted(WordAnalysis analysis) { AnalysisPrinter.printFormatted(analysis); }
    private void printAnalysisRaw(WordAnalysis analysis) { AnalysisPrinter.printRaw(analysis); }
}
