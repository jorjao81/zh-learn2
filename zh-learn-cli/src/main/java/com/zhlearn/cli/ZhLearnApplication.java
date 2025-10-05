package com.zhlearn.cli;

import picocli.CommandLine;

public class ZhLearnApplication {

    public static void main(String[] args) {
        // Note: Helidon ServiceRegistry integration attempted but not functional yet
        // The annotation processor doesn't generate service descriptors properly in this setup
        // Falling back to manual instantiation via MainCommand no-arg constructor
        // TODO: Fix Helidon service discovery - may need different configuration

        int exitCode = new CommandLine(new MainCommand()).execute(args);
        System.exit(exitCode);
    }
}