package com.zhlearn.cli;

import com.zhlearn.application.service.ProviderRegistry;
import com.zhlearn.infrastructure.deepseek.DeepSeekExampleProvider;
import com.zhlearn.infrastructure.deepseek.DeepSeekExplanationProvider;
import com.zhlearn.infrastructure.deepseek.DeepSeekStructuralDecompositionProvider;
import com.zhlearn.infrastructure.dummy.DummyDefinitionProvider;
import com.zhlearn.infrastructure.dummy.DummyExampleProvider;
import com.zhlearn.infrastructure.dummy.DummyExplanationProvider;
import com.zhlearn.infrastructure.pinyin4j.Pinyin4jProvider;
import com.zhlearn.infrastructure.dummy.DummyStructuralDecompositionProvider;
import com.zhlearn.infrastructure.gpt5nano.GPT5NanoExampleProvider;
import com.zhlearn.infrastructure.gpt5nano.GPT5NanoExplanationProvider;
import com.zhlearn.infrastructure.gpt5nano.GPT5NanoStructuralDecompositionProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.ScopeType;

@Command(name = "zh-learn",
        mixinStandardHelpOptions = true,
        version = "1.0.0-SNAPSHOT",
        subcommands = { WordCommand.class, ProvidersCommand.class, ParseAnkiCommand.class, picocli.CommandLine.HelpCommand.class },
        scope = ScopeType.INHERIT)
public class MainCommand implements Runnable {

    private ProviderRegistry providerRegistry;

    public MainCommand() {
        providerRegistry = new ProviderRegistry();
        // Register all providers
        providerRegistry.registerDefinitionProvider(new DummyDefinitionProvider());
        providerRegistry.registerExampleProvider(new DummyExampleProvider());
        providerRegistry.registerExplanationProvider(new DummyExplanationProvider());
        providerRegistry.registerPinyinProvider(new Pinyin4jProvider());
        providerRegistry.registerStructuralDecompositionProvider(new DummyStructuralDecompositionProvider());

        providerRegistry.registerExampleProvider(new DeepSeekExampleProvider());
        providerRegistry.registerExplanationProvider(new DeepSeekExplanationProvider());
        providerRegistry.registerStructuralDecompositionProvider(new DeepSeekStructuralDecompositionProvider());

        providerRegistry.registerExampleProvider(new GPT5NanoExampleProvider());
        providerRegistry.registerExplanationProvider(new GPT5NanoExplanationProvider());
        providerRegistry.registerStructuralDecompositionProvider(new GPT5NanoStructuralDecompositionProvider());
    }

    public ProviderRegistry getProviderRegistry() {
        return providerRegistry;
    }

    @Override
    public void run() {
        // Parent command does nothing on its own
    }
}
