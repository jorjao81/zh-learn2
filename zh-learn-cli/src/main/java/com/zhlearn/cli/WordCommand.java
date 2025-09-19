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
    
    @Option(names = {"--provider"}, description = "Set default provider for all services (default: pinyin4j for pinyin, dummy for others). Available: dummy, pinyin4j, gpt-5-nano, deepseek-chat, qwen3-max, qwen3-plus, qwen3-flash, glm-4-flash, glm-4.5")
    private String defaultProvider = "dummy";
    
    @Option(names = {"--pinyin-provider"}, description = "Set specific provider for pinyin. Available: pinyin4j, dummy")
    private String pinyinProvider;
    
    @Option(names = {"--definition-provider"}, description = "Set specific provider for definition. Available: dummy")
    private String definitionProvider;
    
    @Option(names = {"--decomposition-provider"}, description = "Set specific provider for structural decomposition. Available: dummy, gpt-5-nano, deepseek-chat, qwen3-max, qwen3-plus, qwen3-flash, glm-4-flash, glm-4.5")
    private String decompositionProvider;

    @Option(names = {"--example-provider"}, description = "Set specific provider for examples. Available: dummy, gpt-5-nano, deepseek-chat, qwen3-max, qwen3-plus, qwen3-flash, glm-4-flash, glm-4.5")
    private String exampleProvider;

    @Option(names = {"--explanation-provider"}, description = "Set specific provider for explanation. Available: dummy, gpt-5-nano, deepseek-chat, qwen3-max, qwen3-plus, qwen3-flash, glm-4-flash, glm-4.5")
    private String explanationProvider;
    
    @Option(names = {"--audio-provider"}, description = "Set specific provider for audio pronunciation. Available: anki")
    private String audioProvider;
    
    @Option(names = {"--raw", "--raw-output"}, description = "Display raw HTML content instead of formatted output")
    private boolean rawOutput = false;

    @picocli.CommandLine.ParentCommand
    private MainCommand parent;

    @Override
    public void run() {
        // Create service with providers from MainCommand
        WordAnalysisServiceImpl wordAnalysisService = new WordAnalysisServiceImpl(
            parent.getExampleProvider(),
            parent.getExplanationProvider(),
            parent.getDecompositionProvider(),
            parent.getPinyinProvider(),
            parent.getDefinitionProvider(),
            parent.getAudioProvider()
        );

        // Provider names are no longer used for selection - providers are fixed at startup
        // But we keep the config for backward compatibility
        ProviderConfiguration config = new ProviderConfiguration(
            "deepseek-chat", // Fixed provider
            "pinyin4j",      // Fixed provider
            "dictionary",    // Fixed provider
            "deepseek-chat", // Fixed provider
            "deepseek-chat", // Fixed provider
            "deepseek-chat", // Fixed provider
            "anki"           // Fixed provider
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
