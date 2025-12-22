package com.zhlearn.cli;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.zhlearn.domain.model.ProviderInfo;
import com.zhlearn.domain.model.ProviderInfo.ProviderClass;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "providers",
        description = "List all available providers with their capabilities and descriptions")
public class ProvidersCommand implements Runnable {

    @Option(
            names = {"--type", "-t"},
            description = "Filter by provider type: AI, DICTIONARY, LOCAL, DUMMY")
    private ProviderType filterType;

    @Option(
            names = {"--class", "-c"},
            description =
                    "Filter by provider class: PINYIN, DEFINITION, STRUCTURAL_DECOMPOSITION, EXAMPLE, EXPLANATION")
    private ProviderClass filterClass;

    @Option(
            names = {"--detailed", "-d"},
            description = "Show detailed information including individual capabilities")
    private boolean detailed = false;

    @picocli.CommandLine.ParentCommand private MainCommand parent;

    @Override
    public void run() {
        // Create provider info for all available providers
        List<ProviderInfo> providers = new ArrayList<>();

        // AI Providers - show currently active one and available alternatives
        String currentAI = getCurrentAIProvider();
        providers.add(
                new ProviderInfo(
                        currentAI,
                        getAIProviderDescription(currentAI),
                        ProviderType.AI,
                        EnumSet.of(
                                ProviderClass.EXAMPLE,
                                ProviderClass.EXPLANATION,
                                ProviderClass.STRUCTURAL_DECOMPOSITION)));

        // Show other AI providers if their keys are available
        if (!currentAI.equals("glm-4.5") && hasAPIKey("ZHIPU_API_KEY")) {
            providers.add(
                    new ProviderInfo(
                            "glm-4.5",
                            "GLM-4.5 AI provider (available)",
                            ProviderType.AI,
                            EnumSet.of(
                                    ProviderClass.EXAMPLE,
                                    ProviderClass.EXPLANATION,
                                    ProviderClass.STRUCTURAL_DECOMPOSITION)));
        }
        if (!currentAI.equals("deepseek-chat") && hasAPIKey("DEEPSEEK_API_KEY")) {
            providers.add(
                    new ProviderInfo(
                            "deepseek-chat",
                            "DeepSeek AI provider (available)",
                            ProviderType.AI,
                            EnumSet.of(
                                    ProviderClass.EXAMPLE,
                                    ProviderClass.EXPLANATION,
                                    ProviderClass.STRUCTURAL_DECOMPOSITION)));
        }

        // Show Gemini providers if GEMINI_API_KEY is available
        if (hasAPIKey("GEMINI_API_KEY")) {
            if (!currentAI.equals("gemini-3-pro-preview")) {
                providers.add(
                        new ProviderInfo(
                                "gemini-3-pro-preview",
                                "Gemini 3 Pro Preview AI provider (available)",
                                ProviderType.AI,
                                EnumSet.of(
                                        ProviderClass.EXAMPLE,
                                        ProviderClass.EXPLANATION,
                                        ProviderClass.STRUCTURAL_DECOMPOSITION)));
            }
            if (!currentAI.equals("gemini-2.5-flash")) {
                providers.add(
                        new ProviderInfo(
                                "gemini-2.5-flash",
                                "Gemini 2.5 Flash AI provider (available)",
                                ProviderType.AI,
                                EnumSet.of(
                                        ProviderClass.EXAMPLE,
                                        ProviderClass.EXPLANATION,
                                        ProviderClass.STRUCTURAL_DECOMPOSITION)));
            }
            if (!currentAI.equals("gemini-2.5-pro")) {
                providers.add(
                        new ProviderInfo(
                                "gemini-2.5-pro",
                                "Gemini 2.5 Pro AI provider (available)",
                                ProviderType.AI,
                                EnumSet.of(
                                        ProviderClass.EXAMPLE,
                                        ProviderClass.EXPLANATION,
                                        ProviderClass.STRUCTURAL_DECOMPOSITION)));
            }
        }

        // Show other Qwen variants if DASHSCOPE key is available
        if (hasAPIKey("DASHSCOPE_API_KEY")) {
            if (!currentAI.equals("qwen-plus")) {
                providers.add(
                        new ProviderInfo(
                                "qwen-plus",
                                "Qwen Plus AI provider (available)",
                                ProviderType.AI,
                                EnumSet.of(
                                        ProviderClass.EXAMPLE,
                                        ProviderClass.EXPLANATION,
                                        ProviderClass.STRUCTURAL_DECOMPOSITION)));
            }
            if (!currentAI.equals("qwen-turbo")) {
                providers.add(
                        new ProviderInfo(
                                "qwen-turbo",
                                "Qwen Turbo AI provider (available)",
                                ProviderType.AI,
                                EnumSet.of(
                                        ProviderClass.EXAMPLE,
                                        ProviderClass.EXPLANATION,
                                        ProviderClass.STRUCTURAL_DECOMPOSITION)));
            }
        }

        // Non-AI providers
        providers.add(
                new ProviderInfo(
                        "pinyin4j",
                        "Pinyin4j local provider",
                        ProviderType.LOCAL,
                        EnumSet.of(ProviderClass.PINYIN)));
        providers.add(
                new ProviderInfo(
                        "dictionary",
                        "Dictionary-based definition provider",
                        ProviderType.DICTIONARY,
                        EnumSet.of(ProviderClass.DEFINITION)));

        // Audio providers - show all available
        providers.add(
                new ProviderInfo(
                        "anki",
                        "Anki audio pronunciation provider",
                        ProviderType.LOCAL,
                        EnumSet.of(ProviderClass.AUDIO)));
        providers.add(
                new ProviderInfo(
                        "forvo",
                        "Forvo pronunciation dictionary",
                        ProviderType.DICTIONARY,
                        EnumSet.of(ProviderClass.AUDIO)));
        providers.add(
                new ProviderInfo(
                        "qwen-tts",
                        "Qwen text-to-speech (voices: Cherry, Serena, Chelsie)",
                        ProviderType.AI,
                        EnumSet.of(ProviderClass.AUDIO)));
        providers.add(
                new ProviderInfo(
                        "tencent-tts",
                        "Tencent text-to-speech (voices: zhiwei, zhiling)",
                        ProviderType.AI,
                        EnumSet.of(ProviderClass.AUDIO)));
        providers.add(
                new ProviderInfo(
                        "minimax-tts",
                        "MiniMax Speech-2.6-HD (voices: Wise_Woman, Deep_Voice_Man, Lovely_Girl, Young_Knight, Calm_Woman)",
                        ProviderType.AI,
                        EnumSet.of(ProviderClass.AUDIO)));

        // Apply filters
        if (filterType != null) {
            providers =
                    providers.stream()
                            .filter(p -> p.type() == filterType)
                            .collect(Collectors.toList());
        }

        if (filterClass != null) {
            providers =
                    providers.stream()
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
        TerminalFormatter formatter = parent.getTerminalFormatter();
        int terminalWidth = formatter.getTerminalWidth();

        // Group providers by type
        Map<ProviderType, List<ProviderInfo>> providersByType =
                providers.stream().collect(Collectors.groupingBy(ProviderInfo::type));

        System.out.println(
                formatter.createBox(
                        "Available Providers",
                        "Found " + providers.size() + " provider(s)",
                        terminalWidth));
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

    private void displayProviderType(
            ProviderType type, List<ProviderInfo> providers, int terminalWidth) {
        TerminalFormatter formatter = parent.getTerminalFormatter();
        StringBuilder content = new StringBuilder();

        for (int i = 0; i < providers.size(); i++) {
            ProviderInfo provider = providers.get(i);

            if (i > 0) content.append("\n\n");

            // Provider name and description
            content.append(formatter.formatChineseWord(provider.name(), ""))
                    .append("\n")
                    .append(formatter.formatProviderDescription(provider.description()))
                    .append("\n\n");

            // Supported classes - always show for detailed view or when provider doesn't support
            // everything
            EnumSet<ProviderClass> allClasses = EnumSet.allOf(ProviderClass.class);
            EnumSet<ProviderClass> supportedClasses = EnumSet.copyOf(provider.supportedClasses());
            boolean supportsAll = supportedClasses.size() == allClasses.size();

            if (detailed || !supportsAll) {
                content.append(formatter.formatBoldLabel("Capabilities:")).append("\n");

                for (ProviderClass clazz : ProviderClass.values()) {
                    content.append("  "); // Indentation for capabilities
                    if (supportedClasses.contains(clazz)) {
                        content.append(
                                formatter.formatSupportedCapability(formatProviderClass(clazz)));
                    } else {
                        content.append(
                                formatter.formatUnsupportedCapability(formatProviderClass(clazz)));
                    }
                    content.append("\n");
                }

                if (!supportsAll) {
                    content.append("\n")
                            .append(
                                    formatter.formatWarning(
                                            "This provider doesn't support all capabilities"));
                } else if (detailed) {
                    content.append("\n").append("✓ This provider supports all capabilities");
                }
            }
        }

        System.out.println(
                formatter.createBox(
                        type.name() + " Providers (" + providers.size() + ")",
                        content.toString(),
                        terminalWidth));
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
        TerminalFormatter formatter = parent.getTerminalFormatter();
        String legend =
                formatter.formatSupportedCapability("Supported")
                        + "    "
                        + formatter.formatUnsupportedCapability("Not supported")
                        + "    "
                        + formatter.formatWarning("Partial support")
                        + "\n\n"
                        + "Usage: zh-learn word <word> --provider <provider-name>\n"
                        + "       zh-learn word <word> --explanation-provider <provider> --example-provider <provider>";

        System.out.println(formatter.createBox("Legend", legend, terminalWidth));
    }

    private String getCurrentAIProvider() {
        // Use same logic as MainCommand
        if (hasAPIKey("GEMINI_API_KEY")) {
            return "gemini-3-pro-preview";
        }
        if (hasAPIKey("ZHIPU_API_KEY")) {
            return "glm-4.5";
        }
        if (hasAPIKey("DASHSCOPE_API_KEY")) {
            return "qwen-max";
        }
        return "deepseek-chat";
    }

    private String getAIProviderDescription(String provider) {
        return switch (provider) {
            case "gemini-3-pro-preview" -> "Gemini 3 Pro Preview AI provider (active)";
            case "gemini-2.5-flash" -> "Gemini 2.5 Flash AI provider (active)";
            case "gemini-2.5-pro" -> "Gemini 2.5 Pro AI provider (active)";
            case "glm-4.5" -> "GLM-4.5 AI provider (active)";
            case "qwen-max" -> "Qwen Max AI provider (active)";
            case "qwen-plus" -> "Qwen Plus AI provider (active)";
            case "qwen-turbo" -> "Qwen Turbo AI provider (active)";
            case "deepseek-chat" -> "DeepSeek AI provider (active)";
            default -> provider + " AI provider (active)";
        };
    }

    private boolean hasAPIKey(String keyName) {
        String key = System.getenv(keyName);
        return key != null && !key.isBlank();
    }
}
