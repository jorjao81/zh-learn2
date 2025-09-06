package com.zhlearn.cli;

import com.zhlearn.application.service.WordAnalysisServiceImpl;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderConfiguration;
import com.zhlearn.domain.model.WordAnalysis;
import com.zhlearn.infrastructure.dictionary.PlecoExportDictionary;
import com.zhlearn.infrastructure.dictionary.DictionaryDefinitionProvider;
import com.zhlearn.infrastructure.dictionary.DictionaryExplanationProvider;
import com.zhlearn.infrastructure.dictionary.DictionaryStructuralDecompositionProvider;
import com.zhlearn.infrastructure.pleco.PlecoEntry;
import com.zhlearn.infrastructure.pleco.PlecoExportParser;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * CLI command to parse Pleco export files and process all words through the analysis pipeline.
 * Processes each word found in the export like the existing "word" command.
 */
@Command(
    name = "parse-pleco",
    description = "Parse Pleco export file and analyze all Chinese words"
)
public class ParsePlecoCommand implements Runnable {
    
    @Parameters(index = "0", description = "Path to the Pleco export file (TSV format)")
    private String filePath;
    
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
    
    @Option(names = {"--raw", "--raw-output"}, description = "Display raw HTML content instead of formatted output")
    private boolean rawOutput = false;
    
    @Option(names = {"--limit"}, description = "Limit the number of words to process (default: all)")
    private Integer limit;

    @picocli.CommandLine.ParentCommand
    private MainCommand parent;
    
    @Override
    public void run() {
        try {
            Path path = Paths.get(filePath);
            PlecoExportParser parser = new PlecoExportParser();
            List<PlecoEntry> entries = parser.parseFile(path);
            
            System.out.println("Successfully parsed " + entries.size() + " entries from: " + filePath);
            System.out.println();
            
            // Create dictionary for any dictionary-based providers
            PlecoExportDictionary dictionary = new PlecoExportDictionary(entries);

            // Register dictionary-backed providers so users can opt-in via --*-provider flags
            var registry = parent.getProviderRegistry();
            registry.registerDefinitionProvider(new DictionaryDefinitionProvider(dictionary));
            registry.registerExplanationProvider(new DictionaryExplanationProvider(dictionary));
            registry.registerStructuralDecompositionProvider(new DictionaryStructuralDecompositionProvider(dictionary));
            
            // Set up word analysis service
            WordAnalysisServiceImpl wordAnalysisService = new WordAnalysisServiceImpl(parent.getProviderRegistry());
            
            ProviderConfiguration config = new ProviderConfiguration(
                defaultProvider,
                pinyinProvider,
                definitionProvider,
                decompositionProvider,
                exampleProvider,
                explanationProvider,
                null // audio provider (default)
            );
            
            // Process each word through the analysis pipeline
            int processedCount = 0;
            int maxToProcess = limit != null ? limit : entries.size();
            
            for (PlecoEntry entry : entries) {
                if (processedCount >= maxToProcess) {
                    break;
                }
                
                try {
                    Hanzi word = new Hanzi(entry.hanzi());
                    WordAnalysis analysis = wordAnalysisService.getCompleteAnalysis(word, config);
                    
                    System.out.println("=".repeat(80));
                    System.out.println("Word " + (processedCount + 1) + "/" + Math.min(maxToProcess, entries.size()));
                    System.out.println("=".repeat(80));
                    
                    if (rawOutput) {
                        printAnalysisRaw(analysis, entry);
                    } else {
                        printAnalysisFormatted(analysis, entry);
                    }
                    
                    System.out.println();
                    processedCount++;
                    
                } catch (Exception e) {
                    System.err.println("Error analyzing word '" + entry.hanzi() + "': " + e.getMessage());
                    // Continue with next word
                }
            }
            
            System.out.println("Processed " + processedCount + " words successfully.");
            
        } catch (Exception e) {
            System.err.println("Error parsing Pleco file: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private void printAnalysisFormatted(WordAnalysis analysis, PlecoEntry originalEntry) {
        int width = TerminalFormatter.getTerminalWidth();
        
        // Main header with word and original Pleco pinyin
        String wordContent = TerminalFormatter.formatChineseWord(analysis.word().characters(), analysis.pinyin().pinyin()) + "\n" +
                            "Original Pleco pinyin: " + originalEntry.pinyin() + "\n" +
                            TerminalFormatter.formatProvider("Default: " + analysis.providerName());
        System.out.println(TerminalFormatter.createBox("Chinese Word", wordContent, width));
        System.out.println();
        
        // Original Pleco definition
        String plecoDefContent = originalEntry.definitionText() + "\n" +
                                "Source: Pleco Export";
        System.out.println(TerminalFormatter.createBox("Original Pleco Definition", plecoDefContent, width));
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
        
        // Examples section
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
    
    private void printAnalysisRaw(WordAnalysis analysis, PlecoEntry originalEntry) {
        System.out.println("Chinese Word: " + analysis.word().characters());
        System.out.println("Original Pleco Pinyin: " + originalEntry.pinyin());
        System.out.println("Original Pleco Definition: " + originalEntry.definitionText());
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
