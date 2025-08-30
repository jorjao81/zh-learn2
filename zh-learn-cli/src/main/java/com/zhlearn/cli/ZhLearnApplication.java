package com.zhlearn.cli;

import com.zhlearn.application.service.ProviderRegistry;
import com.zhlearn.application.service.WordAnalysisServiceImpl;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ProviderConfiguration;
import com.zhlearn.domain.model.WordAnalysis;
import com.zhlearn.infrastructure.anki.AnkiCard;
import com.zhlearn.infrastructure.anki.AnkiCardParser;
import com.zhlearn.infrastructure.deepseek.DeepSeekExplanationProvider;
import com.zhlearn.infrastructure.dummy.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ZhLearnApplication {
    
    private final WordAnalysisServiceImpl wordAnalysisService;

    public ZhLearnApplication() {

        ProviderRegistry registry = new ProviderRegistry();
        registry.registerDefinitionProvider(new DummyDefinitionProvider());
        registry.registerExampleProvider(new DummyExampleProvider());
        registry.registerExplanationProvider(new DummyExplanationProvider());
        registry.registerExplanationProvider(new DeepSeekExplanationProvider());
        registry.registerPinyinProvider(new DummyPinyinProvider());
        registry.registerStructuralDecompositionProvider(new DummyStructuralDecompositionProvider());

        this.wordAnalysisService = new WordAnalysisServiceImpl(registry);
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            System.exit(1);
        }
        
        String command = args[0];
        ZhLearnApplication app = new ZhLearnApplication();
        
        switch (command) {
            case "word":
                if (args.length < 2) {
                    System.err.println("Error: word command requires a Chinese word argument");
                    printUsage();
                    System.exit(1);
                }
                String chineseWord = args[1];
                ProviderConfiguration config = extractProviderConfiguration(args);
                boolean rawOutput = extractRawOutputFlag(args);
                app.analyzeWord(chineseWord, config, rawOutput);
                break;
                
            case "parse-anki":
                if (args.length < 2) {
                    System.err.println("Error: parse-anki command requires a file path argument");
                    printUsage();
                    System.exit(1);
                }
                String filePath = args[1];
                app.parseAnkiFile(filePath);
                break;
                
            default:
                System.err.println("Unknown command: " + command);
                printUsage();
                System.exit(1);
        }
    }
    
    private static ProviderConfiguration extractProviderConfiguration(String[] args) {
        String defaultProvider = null;
        String pinyinProvider = null;
        String definitionProvider = null;
        String decompositionProvider = null;
        String exampleProvider = null;
        String explanationProvider = null;
        
        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "--provider":
                    defaultProvider = args[i + 1];
                    break;
                case "--pinyin-provider":
                    pinyinProvider = args[i + 1];
                    break;
                case "--definition-provider":
                    definitionProvider = args[i + 1];
                    break;
                case "--decomposition-provider":
                    decompositionProvider = args[i + 1];
                    break;
                case "--example-provider":
                    exampleProvider = args[i + 1];
                    break;
                case "--explanation-provider":
                    explanationProvider = args[i + 1];
                    break;
            }
        }
        
        if (defaultProvider == null) {
            defaultProvider = "dummy";
        }
        
        return new ProviderConfiguration(
            defaultProvider,
            pinyinProvider,
            definitionProvider,
            decompositionProvider,
            exampleProvider,
            explanationProvider
        );
    }
    
    private static boolean extractRawOutputFlag(String[] args) {
        for (String arg : args) {
            if ("--raw".equals(arg) || "--raw-output".equals(arg)) {
                return true;
            }
        }
        return false;
    }
    
    private void analyzeWord(String wordStr, ProviderConfiguration config, boolean rawOutput) {
        try {
            Hanzi word = new Hanzi(wordStr);
            WordAnalysis analysis = wordAnalysisService.getCompleteAnalysis(word, config);
            
            if (rawOutput) {
                printAnalysisRaw(analysis);
            } else {
                printAnalysisFormatted(analysis);
            }
        } catch (Exception e) {
            System.err.println("Error analyzing word: " + e.getMessage());
            e.printStackTrace();
        }
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
        
        // Examples section
        StringBuilder exampleContent = new StringBuilder();
        var usages = analysis.examples().usages();
        for (int i = 0; i < usages.size(); i++) {
            var usage = usages.get(i);
            if (i > 0) {
                exampleContent.append("\n\n");
            }
            exampleContent.append(TerminalFormatter.formatExample(
                usage.sentence(), 
                usage.pinyin(), 
                usage.translation(), 
                usage.context()
            ));
        }
        exampleContent.append("\n").append(TerminalFormatter.formatProvider(analysis.exampleProvider()));
        System.out.println(TerminalFormatter.createBox("Examples", exampleContent.toString(), width));
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
    
    private void parseAnkiFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            AnkiCardParser parser = new AnkiCardParser();
            List<AnkiCard> cards = parser.parseFile(path);
            
            System.out.println("Successfully parsed " + cards.size() + " Anki cards from: " + filePath);
            System.out.println();
            
            // Display the first few cards as examples
            int displayLimit = Math.min(5, cards.size());
            for (int i = 0; i < displayLimit; i++) {
                AnkiCard card = cards.get(i);
                displayAnkiCard(card, i + 1);
            }
            
            if (cards.size() > displayLimit) {
                System.out.println("... and " + (cards.size() - displayLimit) + " more cards.");
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing Anki file: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private void displayAnkiCard(AnkiCard card, int cardNumber) {
        System.out.println("Card " + cardNumber + ":");
        System.out.println("  Simplified: " + card.simplified());
        System.out.println("  Pinyin: " + card.pinyin());
        System.out.println("  Definition: " + truncate(card.definition(), 100));
        System.out.println("  Examples: " + truncate(card.examples(), 100));
        System.out.println("  Etymology: " + truncate(card.etymology(), 100));
        System.out.println();
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
    
    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  zh-learn word <chinese_word> [options]");
        System.out.println("  zh-learn parse-anki <file_path>");
        System.out.println();
        System.out.println("Output Options:");
        System.out.println("  --raw, --raw-output            Display raw HTML content instead of formatted output");
        System.out.println();
        System.out.println("Provider Options:");
        System.out.println("  --provider <name>              Set default provider for all services");
        System.out.println("  --pinyin-provider <name>       Set specific provider for pinyin");
        System.out.println("  --definition-provider <name>   Set specific provider for definition");
        System.out.println("  --decomposition-provider <name> Set specific provider for structural decomposition");
        System.out.println("  --example-provider <name>      Set specific provider for examples");
        System.out.println("  --explanation-provider <name>  Set specific provider for explanation");
        System.out.println();
        System.out.println("Provider Selection Logic:");
        System.out.println("  - Use --provider as default for all services");
        System.out.println("  - Override individual services with specific --*-provider flags");
        System.out.println("  - If no --provider is specified, defaults to 'dummy'");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  zh-learn word 汉语");
        System.out.println("  zh-learn word 汉语 --raw");
        System.out.println("  zh-learn word 汉语 --provider dummy");
        System.out.println("  zh-learn word 学习 --explanation-provider deepseek-chat");
        System.out.println("  zh-learn word 中文 --provider dummy --explanation-provider deepseek-chat --raw");
        System.out.println("  zh-learn word 你好 --pinyin-provider dummy --definition-provider dummy --explanation-provider deepseek-chat");
        System.out.println();
        System.out.println("Anki Import Examples:");
        System.out.println("  zh-learn parse-anki /path/to/anki_cards.txt");
        System.out.println("  zh-learn parse-anki SelectedNotes.txt");
        System.out.println();
        System.out.println("Available Providers:");
        System.out.println("  Dummy: dummy");
        System.out.println("  DeepSeek: deepseek-chat (explanation only)");
        System.out.println();
        System.out.println("Note: Most providers are currently dummy implementations.");
        System.out.println("DeepSeek requires DEEPSEEK_API_KEY environment variable.");
    }
}