package com.zhlearn.cli;

import picocli.CommandLine;

public class ZhLearnApplication {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MainCommand()).execute(args);
        System.exit(exitCode);
    }
}
