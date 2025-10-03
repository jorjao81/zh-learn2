package com.zhlearn.e2e;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CliStepDefinitions {

    private int exitCode;
    private String stdout;
    private String stderr;

    @When("I run the CLI with {string}")
    public void iRunTheCliWith(String args) throws IOException, InterruptedException {
        Path projectRoot = Paths.get(System.getProperty("user.dir")).getParent();
        Path cliScript = projectRoot.resolve("zh-learn.sh");

        List<String> command = new ArrayList<>();
        command.add(cliScript.toString());

        if (args != null && !args.trim().isEmpty()) {
            String[] argArray = args.trim().split("\\s+");
            for (String arg : argArray) {
                command.add(arg);
            }
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(projectRoot.toFile());

        Process process = processBuilder.start();

        StringBuilder stdoutBuilder = new StringBuilder();
        StringBuilder stderrBuilder = new StringBuilder();

        Thread stdoutThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdoutBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Thread stderrThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderrBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        stdoutThread.start();
        stderrThread.start();

        exitCode = process.waitFor();

        stdoutThread.join();
        stderrThread.join();

        stdout = stdoutBuilder.toString();
        stderr = stderrBuilder.toString();
    }

    @Then("the exit code should be {int}")
    public void theExitCodeShouldBe(int expectedExitCode) {
        assertThat(exitCode)
            .as("Exit code should be %d but was %d. stdout=%s, stderr=%s",
                expectedExitCode, exitCode, stdout, stderr)
            .isEqualTo(expectedExitCode);
    }

    @Then("the output should contain {string}")
    public void theOutputShouldContain(String expectedSubstring) {
        String combinedOutput = stdout + stderr;
        String strippedOutput = stripAnsi(combinedOutput);

        assertThat(strippedOutput)
            .as("Output should contain \"%s\" but was: %s", expectedSubstring, strippedOutput)
            .contains(expectedSubstring);
    }

    private String stripAnsi(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("\\u001B\\[[0-9;]*[mK]", "");
    }
}
