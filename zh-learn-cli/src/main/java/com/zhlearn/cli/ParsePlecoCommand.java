package com.zhlearn.cli;

import com.zhlearn.application.service.WordAnalysisServiceImpl;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderConfiguration;
import com.zhlearn.domain.model.WordAnalysis;
import com.zhlearn.infrastructure.dictionary.PlecoExportDictionary;
import com.zhlearn.infrastructure.dictionary.DictionaryDefinitionProvider;
import com.zhlearn.infrastructure.dictionary.DictionaryPinyinProvider;
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
            registry.registerPinyinProvider(new DictionaryPinyinProvider(dictionary));
            
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
                        AnalysisPrinter.printRaw(analysis);
                    } else {
                        AnalysisPrinter.printFormatted(analysis);
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
    
    // Printing is delegated to AnalysisPrinter to match 'word' command output
}
