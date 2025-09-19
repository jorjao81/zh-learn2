package com.zhlearn.cli;

import com.zhlearn.domain.model.ProviderInfo;
import com.zhlearn.domain.model.ProviderInfo.ProviderClass;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Command(
    name = "providers",
    description = "List all available providers with their capabilities and descriptions"
)
public class ProvidersCommand implements Runnable {
    
    @Option(names = {"--type", "-t"}, description = "Filter by provider type: AI, DICTIONARY, LOCAL, DUMMY")
    private ProviderType filterType;
    
    @Option(names = {"--class", "-c"}, description = "Filter by provider class: PINYIN, DEFINITION, STRUCTURAL_DECOMPOSITION, EXAMPLE, EXPLANATION")
    private ProviderClass filterClass;
    
    @Option(names = {"--detailed", "-d"}, description = "Show detailed information including individual capabilities")
    private boolean detailed = false;
    
    @picocli.CommandLine.ParentCommand
    private MainCommand parent;

    @Override
    public void run() {
        // Create provider info for all available providers
        List<ProviderInfo> providers = new ArrayList<>();

        // AI Providers - show currently active one and available alternatives
        String currentAI = getCurrentAIProvider();
        providers.add(new ProviderInfo(currentAI, getAIProviderDescription(currentAI),
            ProviderType.AI, EnumSet.of(ProviderClass.EXAMPLE, ProviderClass.EXPLANATION, ProviderClass.STRUCTURAL_DECOMPOSITION)));

        // Show other AI providers if their keys are available
        if (!currentAI.equals("glm-4.5") && hasAPIKey("ZHIPU_API_KEY")) {
            providers.add(new ProviderInfo("glm-4.5", "GLM-4.5 AI provider (available)",
                ProviderType.AI, EnumSet.of(ProviderClass.EXAMPLE, ProviderClass.EXPLANATION, ProviderClass.STRUCTURAL_DECOMPOSITION)));
        }
        if (!currentAI.equals("deepseek-chat") && hasAPIKey("DEEPSEEK_API_KEY")) {
            providers.add(new ProviderInfo("deepseek-chat", "DeepSeek AI provider (available)",
                ProviderType.AI, EnumSet.of(ProviderClass.EXAMPLE, ProviderClass.EXPLANATION, ProviderClass.STRUCTURAL_DECOMPOSITION)));
        }

        // Show other Qwen3 variants if DASHSCOPE key is available
        if (hasAPIKey("DASHSCOPE_API_KEY")) {
            if (!currentAI.equals("qwen3-plus")) {
                providers.add(new ProviderInfo("qwen3-plus", "Qwen3 Plus AI provider (available)",
                    ProviderType.AI, EnumSet.of(ProviderClass.EXAMPLE, ProviderClass.EXPLANATION, ProviderClass.STRUCTURAL_DECOMPOSITION)));
            }
            if (!currentAI.equals("qwen3-flash")) {
                providers.add(new ProviderInfo("qwen3-flash", "Qwen3 Flash AI provider (available)",
                    ProviderType.AI, EnumSet.of(ProviderClass.EXAMPLE, ProviderClass.EXPLANATION, ProviderClass.STRUCTURAL_DECOMPOSITION)));
            }
        }

        // Non-AI providers
        providers.add(new ProviderInfo("pinyin4j", "Pinyin4j local provider",
            ProviderType.LOCAL, EnumSet.of(ProviderClass.PINYIN)));
        providers.add(new ProviderInfo("dictionary", "Dictionary-based definition provider",
            ProviderType.DICTIONARY, EnumSet.of(ProviderClass.DEFINITION)));

        // Audio providers - show all available
        providers.add(new ProviderInfo("anki", "Anki audio pronunciation provider",
            ProviderType.LOCAL, EnumSet.of(ProviderClass.AUDIO)));
        providers.add(new ProviderInfo("qwen-tts", "Qwen text-to-speech (voices: Cherry, Serena, Chelsie)",
            ProviderType.AI, EnumSet.of(ProviderClass.AUDIO)));
        providers.add(new ProviderInfo("forvo", "Forvo pronunciation dictionary",
            ProviderType.DICTIONARY, EnumSet.of(ProviderClass.AUDIO)));

        // Apply filters
        if (filterType != null) {
            providers = providers.stream()
                .filter(p -> p.type() == filterType)
                .collect(Collectors.toList());
        }

        if (filterClass != null) {
            providers = providers.stream()
                .filter(p -> p.supportedClasses().contains(filterClass))
                .collect(Collectors.toList());
        }

        if (providers.isEmpty()) {
            System.out.println("No providers found matching the specified criteria.");
            return;
        }

        displayProviders(providers);
    }
    
    private void displayProviders(List<ProviderInfo> providers) {
        int terminalWidth = TerminalFormatter.getTerminalWidth();
        
        // Group providers by type
        Map<ProviderType, List<ProviderInfo>> providersByType = providers.stream()
            .collect(Collectors.groupingBy(ProviderInfo::type));
        
        System.out.println(TerminalFormatter.createBox("Available Providers", 
            "Found " + providers.size() + " provider(s)", terminalWidth));
        System.out.println();
        
        // Display each type group
        for (ProviderType type : ProviderType.values()) {
            if (!providersByType.containsKey(type)) continue;
            
            List<ProviderInfo> typeProviders = providersByType.get(type);
            displayProviderType(type, typeProviders, terminalWidth);
            System.out.println();
        }
        
        // Show legend
        displayLegend(terminalWidth);
    }
    
    private void displayProviderType(ProviderType type, List<ProviderInfo> providers, int terminalWidth) {
        StringBuilder content = new StringBuilder();
        
        for (int i = 0; i < providers.size(); i++) {
            ProviderInfo provider = providers.get(i);
            
            if (i > 0) content.append("\n\n");
            
            // Provider name and description
            content.append(TerminalFormatter.formatChineseWord(provider.name(), ""))
                   .append("\n")
                   .append(TerminalFormatter.formatProviderDescription(provider.description()))
                   .append("\n\n");
            
            // Supported classes - always show for detailed view or when provider doesn't support everything
            EnumSet<ProviderClass> allClasses = EnumSet.allOf(ProviderClass.class);
            EnumSet<ProviderClass> supportedClasses = EnumSet.copyOf(provider.supportedClasses());
            boolean supportsAll = supportedClasses.size() == allClasses.size();
            
            if (detailed || !supportsAll) {
                content.append(TerminalFormatter.formatBoldLabel("Capabilities:"))
                       .append("\n");
                
                for (ProviderClass clazz : ProviderClass.values()) {
                    content.append("  "); // Indentation for capabilities
                    if (supportedClasses.contains(clazz)) {
                        content.append(TerminalFormatter.formatSupportedCapability(formatProviderClass(clazz)));
                    } else {
                        content.append(TerminalFormatter.formatUnsupportedCapability(formatProviderClass(clazz)));
                    }
                    content.append("\n");
                }
                
                if (!supportsAll) {
                    content.append("\n").append(TerminalFormatter.formatWarning("This provider doesn't support all capabilities"));
                } else if (detailed) {
                    content.append("\n").append("✓ This provider supports all capabilities");
                }
            }
        }
        
        System.out.println(TerminalFormatter.createBox(
            type.name() + " Providers (" + providers.size() + ")",
            content.toString(),
            terminalWidth
        ));
    }
    
    private String formatProviderClass(ProviderClass clazz) {
        return switch (clazz) {
            case PINYIN -> "Pinyin";
            case DEFINITION -> "Definition";
            case STRUCTURAL_DECOMPOSITION -> "Decomposition";
            case EXAMPLE -> "Examples";
            case EXPLANATION -> "Explanation";
            case AUDIO -> "Audio";
        };
    }
    
    private void displayLegend(int terminalWidth) {
        String legend = TerminalFormatter.formatSupportedCapability("Supported") + "    " +
                       TerminalFormatter.formatUnsupportedCapability("Not supported") + "    " +
                       TerminalFormatter.formatWarning("Partial support") + "\n\n" +
                       "Usage: zh-learn word <word> --provider <provider-name>\n" +
                       "       zh-learn word <word> --explanation-provider <provider> --example-provider <provider>";
        
        System.out.println(TerminalFormatter.createBox("Legend", legend, terminalWidth));
    }

    private String getCurrentAIProvider() {
        // Use same logic as MainCommand
        if (hasAPIKey("ZHIPU_API_KEY")) {
            return "glm-4.5";
        }
        if (hasAPIKey("DASHSCOPE_API_KEY")) {
            return "qwen3-max";
        }
        return "deepseek-chat";
    }

    private String getAIProviderDescription(String provider) {
        return switch (provider) {
            case "glm-4.5" -> "GLM-4.5 AI provider (active)";
            case "qwen3-max" -> "Qwen3 Max AI provider (active)";
            case "qwen3-plus" -> "Qwen3 Plus AI provider (active)";
            case "qwen3-flash" -> "Qwen3 Flash AI provider (active)";
            case "deepseek-chat" -> "DeepSeek AI provider (active)";
            default -> provider + " AI provider (active)";
        };
    }

    private boolean hasAPIKey(String keyName) {
        String key = System.getenv(keyName);
        return key != null && !key.isBlank();
    }
}
