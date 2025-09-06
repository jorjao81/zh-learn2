package com.zhlearn.cli;

import com.zhlearn.application.service.ProviderRegistry;
import com.zhlearn.domain.model.ProviderInfo;
import com.zhlearn.domain.model.ProviderInfo.ProviderClass;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.infrastructure.deepseek.DeepSeekExampleProvider;
import com.zhlearn.infrastructure.deepseek.DeepSeekExplanationProvider;
import com.zhlearn.infrastructure.deepseek.DeepSeekStructuralDecompositionProvider;
import com.zhlearn.infrastructure.dummy.*;
import com.zhlearn.infrastructure.gpt5nano.GPT5NanoExampleProvider;
import com.zhlearn.infrastructure.gpt5nano.GPT5NanoExplanationProvider;
import com.zhlearn.infrastructure.gpt5nano.GPT5NanoStructuralDecompositionProvider;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Command(
    name = "providers",
    description = "List all available providers with their capabilities and descriptions"
)
public class ProvidersCommand implements Runnable {
    
    @Option(names = {"--type", "-t"}, description = "Filter by provider type: AI, DICTIONARY, DUMMY")
    private ProviderType filterType;
    
    @Option(names = {"--class", "-c"}, description = "Filter by provider class: PINYIN, DEFINITION, STRUCTURAL_DECOMPOSITION, EXAMPLE, EXPLANATION")
    private ProviderClass filterClass;
    
    @Option(names = {"--detailed", "-d"}, description = "Show detailed information including individual capabilities")
    private boolean detailed = false;
    
    @picocli.CommandLine.ParentCommand
    private MainCommand parent;

    @Override
    public void run() {
        List<ProviderInfo> providers = parent.getProviderRegistry().getAllProviderInfo();
        
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
    
    
}
