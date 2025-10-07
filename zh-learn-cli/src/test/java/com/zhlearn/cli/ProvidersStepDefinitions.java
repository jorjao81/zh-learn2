package com.zhlearn.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import picocli.CommandLine;

public class ProvidersStepDefinitions {

    private String lastOutput;
    private int lastExitCode;

    @When("I execute the CLI command {string}")
    public void iExecuteTheCliCommand(String commandLine) {
        String[] args =
                commandLine.trim().isEmpty() ? new String[0] : commandLine.trim().split("\\s+");

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        PrintStream capture = new PrintStream(stdout, true, StandardCharsets.UTF_8);

        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        try {
            System.setOut(capture);
            System.setErr(capture);

            MainCommand main = new MainCommand();
            CommandLine cmd = new CommandLine(main);

            lastExitCode = cmd.execute(args);
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
            capture.flush();
        }

        lastOutput = stdout.toString(StandardCharsets.UTF_8);
    }

    @Then("the command output should include lines:")
    public void theCommandOutputShouldIncludeLines(DataTable table) {
        if (lastExitCode != 0 && lastOutput != null) {
            System.out.println("CLI command output:\n" + lastOutput);
        }
        assertThat(lastExitCode).as("CLI command should exit successfully").isZero();

        String strippedOutput = stripAnsi(lastOutput);

        for (List<String> row : table.asLists(String.class)) {
            if (row.isEmpty()) {
                continue;
            }
            String substring = row.get(0);
            if (substring == null || substring.isBlank()) {
                continue;
            }
            if ("substring".equalsIgnoreCase(substring.trim())) {
                continue;
            }
            assertThat(strippedOutput)
                    .as("Output should contain \"%s\" but was:%n%s", substring, strippedOutput)
                    .contains(substring);
        }
    }

    private String stripAnsi(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("\\u001B\\[[0-9;]*[mK]", "");
    }
}
