package com.zhlearn.cli;

import com.zhlearn.application.service.ProviderRegistry;
import picocli.CommandLine.Command;
import picocli.CommandLine.ScopeType;

@Command(name = "zh-learn",
        mixinStandardHelpOptions = true,
        version = "1.0.0-SNAPSHOT",
        subcommands = { WordCommand.class, ProvidersCommand.class, ParseAnkiCommand.class, ParsePlecoCommand.class, AudioCommand.class, picocli.CommandLine.HelpCommand.class },
        scope = ScopeType.INHERIT)
public class MainCommand implements Runnable {

    private ProviderRegistry providerRegistry;

    public MainCommand() {
        providerRegistry = new ProviderRegistry();
    }

    public ProviderRegistry getProviderRegistry() {
        return providerRegistry;
    }

    @Override
    public void run() {
        // Parent command does nothing on its own
    }
}
