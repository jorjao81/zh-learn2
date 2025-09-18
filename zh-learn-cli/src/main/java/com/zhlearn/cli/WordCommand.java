package com.zhlearn.cli;

import com.zhlearn.application.service.ProviderRegistry;
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
    
    @Option(names = {"--provider"}, description = "Set default provider for all services (default: pinyin4j for pinyin, dummy for others). Available: dummy, pinyin4j, gpt-5-nano, deepseek-chat")
    private String defaultProvider = "dummy";
    
    @Option(names = {"--pinyin-provider"}, description = "Set specific provider for pinyin. Available: pinyin4j, dummy")
    private String pinyinProvider;
    
    @Option(names = {"--definition-provider"}, description = "Set specific provider for definition. Available: dummy")
    private String definitionProvider;
    
    @Option(names = {"--decomposition-provider"}, description = "Set specific provider for structural decomposition. Available: dummy, gpt-5-nano, deepseek-chat")
    private String decompositionProvider;
    
    @Option(names = {"--example-provider"}, description = "Set specific provider for examples. Available: dummy, gpt-5-nano, deepseek-chat")
    private String exampleProvider;
    
    @Option(names = {"--explanation-provider"}, description = "Set specific provider for explanation. Available: dummy, gpt-5-nano, deepseek-chat")
    private String explanationProvider;
    
    @Option(names = {"--audio-provider"}, description = "Set specific provider for audio pronunciation. Available: anki")
    private String audioProvider;
    
    @Option(names = {"--raw", "--raw-output"}, description = "Display raw HTML content instead of formatted output")
    private boolean rawOutput = false;

    @picocli.CommandLine.ParentCommand
    private MainCommand parent;

    @Override
    public void run() {
        WordAnalysisServiceImpl wordAnalysisService = new WordAnalysisServiceImpl(parent.getProviderRegistry());
        // Validate providers before processing
        String validationError = validateProviders();
        if (validationError != null) {
            System.err.println(validationError);
            System.exit(1);
        }
        
        String effectiveAudioProvider = audioProvider;
        if (effectiveAudioProvider == null || effectiveAudioProvider.isBlank()) {
            effectiveAudioProvider = parent.getProviderRegistry().getAudioProvider("anki").isPresent()
                ? "anki" : null;
        }

        ProviderConfiguration config = new ProviderConfiguration(
            defaultProvider,
            pinyinProvider,
            definitionProvider,
            decompositionProvider,
            exampleProvider,
            explanationProvider,
            effectiveAudioProvider
        );
        
        Hanzi word = new Hanzi(chineseWord);
        WordAnalysis analysis = wordAnalysisService.getCompleteAnalysis(word, config);
        
        if (rawOutput) {
            AnalysisPrinter.printRaw(analysis);
        } else {
            AnalysisPrinter.printFormatted(analysis);
        }
    }
    
    private String validateProviders() {
        ProviderRegistry registry = parent.getProviderRegistry();
        
        // Check each provider
        String[] providers = {
            defaultProvider, pinyinProvider, definitionProvider, 
            decompositionProvider, exampleProvider, explanationProvider,
            audioProvider
        };
        String[] providerTypes = {
            "default", "pinyin", "definition", 
            "decomposition", "example", "explanation",
            "audio"
        };
        
        for (int i = 0; i < providers.length; i++) {
            String provider = providers[i];
            String type = providerTypes[i];
            
            if (provider != null && !isProviderAvailable(registry, provider)) {
                return createProviderNotFoundError(registry, provider, type);
            }
        }
        
        return null; // All providers valid
    }
    
    private boolean isProviderAvailable(ProviderRegistry registry, String providerName) {
        return registry.getPinyinProvider(providerName).isPresent() ||
               registry.getDefinitionProvider(providerName).isPresent() ||
               registry.getStructuralDecompositionProvider(providerName).isPresent() ||
               registry.getExampleProvider(providerName).isPresent() ||
               registry.getExplanationProvider(providerName).isPresent() ||
               registry.getAudioProvider(providerName).isPresent();
    }
    
    private String createProviderNotFoundError(ProviderRegistry registry, String requestedProvider, String providerType) {
        StringBuilder error = new StringBuilder();
        error.append("Provider '").append(requestedProvider).append("' not found");
        if (!providerType.equals("default")) {
            error.append(" for ").append(providerType);
        }
        error.append(".\n\n");
        
        // Find similar providers
        List<String> similarProviders = registry.findSimilarProviders(requestedProvider);
        if (!similarProviders.isEmpty()) {
            error.append("Did you mean one of these?\n");
            for (String similar : similarProviders) {
                error.append("  - ").append(similar).append("\n");
            }
            error.append("\n");
        }
        
        error.append("Use 'zh-learn providers' to see all available providers.");
        
        return error.toString();
    }
    
    private void printAnalysisFormatted(WordAnalysis analysis) { AnalysisPrinter.printFormatted(analysis); }
    private void printAnalysisRaw(WordAnalysis analysis) { AnalysisPrinter.printRaw(analysis); }
}
