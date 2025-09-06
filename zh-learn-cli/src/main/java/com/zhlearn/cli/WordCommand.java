package com.zhlearn.cli;

import com.zhlearn.application.service.ProviderRegistry;
import com.zhlearn.application.service.WordAnalysisServiceImpl;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderConfiguration;
import com.zhlearn.domain.model.WordAnalysis;
import com.zhlearn.infrastructure.deepseek.DeepSeekExampleProvider;
import com.zhlearn.infrastructure.deepseek.DeepSeekExplanationProvider;
import com.zhlearn.infrastructure.deepseek.DeepSeekStructuralDecompositionProvider;
import com.zhlearn.infrastructure.dummy.*;
import com.zhlearn.infrastructure.gpt5nano.GPT5NanoExampleProvider;
import com.zhlearn.infrastructure.gpt5nano.GPT5NanoExplanationProvider;
import com.zhlearn.infrastructure.gpt5nano.GPT5NanoStructuralDecompositionProvider;

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
    
    @Option(names = {"--provider"}, description = "Set default provider for all services (default: dummy). Available: dummy, gpt-5-nano, deepseek-chat")
    private String defaultProvider = "dummy";
    
    @Option(names = {"--pinyin-provider"}, description = "Set specific provider for pinyin. Available: dummy")
    private String pinyinProvider;
    
    @Option(names = {"--definition-provider"}, description = "Set specific provider for definition. Available: dummy")
    private String definitionProvider;
    
    @Option(names = {"--decomposition-provider"}, description = "Set specific provider for structural decomposition. Available: dummy, gpt-5-nano, deepseek-chat")
    private String decompositionProvider;
    
    @Option(names = {"--example-provider"}, description = "Set specific provider for examples. Available: dummy, gpt-5-nano, deepseek-chat")
    private String exampleProvider;
    
    @Option(names = {"--explanation-provider"}, description = "Set specific provider for explanation. Available: dummy, gpt-5-nano, deepseek-chat")
    private String explanationProvider;
    
    @Option(names = {"--raw", "--raw-output"}, description = "Display raw HTML content instead of formatted output")
    private boolean rawOutput = false;

    @picocli.CommandLine.ParentCommand
    private MainCommand parent;

    @Override
    public void run() {
        try {
            WordAnalysisServiceImpl wordAnalysisService = new WordAnalysisServiceImpl(parent.getProviderRegistry());
            // Validate providers before processing
            String validationError = validateProviders();
            if (validationError != null) {
                System.err.println(validationError);
                System.exit(1);
            }
            
            ProviderConfiguration config = new ProviderConfiguration(
                defaultProvider,
                pinyinProvider,
                definitionProvider,
                decompositionProvider,
                exampleProvider,
                explanationProvider,
                null // audio provider (default)
            );
            
            Hanzi word = new Hanzi(chineseWord);
            WordAnalysis analysis = wordAnalysisService.getCompleteAnalysis(word, config);
            
            if (rawOutput) {
                printAnalysisRaw(analysis);
            } else {
                printAnalysisFormatted(analysis);
            }
        } catch (Exception e) {
            System.err.println("Error analyzing word: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private String validateProviders() {
        ProviderRegistry registry = parent.getProviderRegistry();
        
        // Check each provider
        String[] providers = {
            defaultProvider, pinyinProvider, definitionProvider, 
            decompositionProvider, exampleProvider, explanationProvider
        };
        String[] providerTypes = {
            "default", "pinyin", "definition", 
            "decomposition", "example", "explanation"
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
               registry.getExplanationProvider(providerName).isPresent();
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
    
    private void printAnalysisFormatted(WordAnalysis analysis) {
        int width = TerminalFormatter.getTerminalWidth();
        
        // Main header with word
        String wordContent = TerminalFormatter.formatChineseWord(analysis.word().characters(), analysis.pinyin().pinyin()) + "\n" +
                            TerminalFormatter.formatProvider("Default: " + analysis.providerName());
        System.out.println(TerminalFormatter.createBox("Chinese Word", wordContent, width));
        System.out.println();
        
        // Pinyin section
        String pinyinContent = TerminalFormatter.formatChineseWord("拼音", analysis.pinyin().pinyin()) + "\n" +
                              TerminalFormatter.formatProvider(analysis.pinyinProvider());
        System.out.println(TerminalFormatter.createBox("Pinyin", pinyinContent, width));
        System.out.println();
        
        // Definition section
        String defContent = TerminalFormatter.formatDefinition(analysis.definition().meaning(), analysis.definition().partOfSpeech()) + "\n" +
                           TerminalFormatter.formatProvider(analysis.definitionProvider());
        System.out.println(TerminalFormatter.createBox("Definition", defContent, width));
        System.out.println();
        
        // Structural Decomposition section
        String decompositionContent = TerminalFormatter.formatStructuralDecomposition(
            analysis.structuralDecomposition().decomposition()) + "\n" +
            TerminalFormatter.formatProvider(analysis.decompositionProvider());
        System.out.println(TerminalFormatter.createBox("Structural Decomposition", decompositionContent, width));
        System.out.println();
        
        // Examples section - use new grouped formatting
        String groupedExamples = TerminalFormatter.formatGroupedExamples(analysis.examples().usages());
        String exampleContent = groupedExamples + "\n" + TerminalFormatter.formatProvider(analysis.exampleProvider());
        System.out.println(TerminalFormatter.createBox("Examples", exampleContent, width));
        System.out.println();
        
        // Explanation section with HTML conversion
        String explanationContent = TerminalFormatter.convertHtmlToAnsi(analysis.explanation().explanation()) + "\n" +
                                   TerminalFormatter.formatProvider(analysis.explanationProvider());
        System.out.println(TerminalFormatter.createBox("Explanation", explanationContent, width));
        
        // Shutdown Jansi when done
        Runtime.getRuntime().addShutdownHook(new Thread(TerminalFormatter::shutdown));
    }
    
    private void printAnalysisRaw(WordAnalysis analysis) {
        System.out.println("Chinese Word: " + analysis.word().characters());
        System.out.println("Default Provider: " + analysis.providerName());
        System.out.println();
        
        System.out.println("Pinyin: " + analysis.pinyin().pinyin());
        System.out.println("  Provider: " + analysis.pinyinProvider());
        System.out.println();
        
        System.out.println("Definition: " + analysis.definition().meaning());
        System.out.println("Part of Speech: " + analysis.definition().partOfSpeech());
        System.out.println("  Provider: " + analysis.definitionProvider());
        System.out.println();
        
        System.out.println("Structural Decomposition: " + analysis.structuralDecomposition().decomposition());
        System.out.println("  Provider: " + analysis.decompositionProvider());
        System.out.println();
        
        System.out.println("Examples:");
        for (var usage : analysis.examples().usages()) {
            System.out.println("  Chinese: " + usage.sentence());
            System.out.println("  Pinyin: " + usage.pinyin());
            System.out.println("  English: " + usage.translation());
            if (usage.context() != null && !usage.context().isEmpty()) {
                System.out.println("  Context: " + usage.context());
            }
        }
        System.out.println("  Provider: " + analysis.exampleProvider());
        System.out.println();
        
        System.out.println("Explanation: " + analysis.explanation().explanation());
        System.out.println("  Provider: " + analysis.explanationProvider());
    }
}
