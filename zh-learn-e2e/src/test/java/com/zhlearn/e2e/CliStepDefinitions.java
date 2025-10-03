package com.zhlearn.e2e;

import com.zhlearn.infrastructure.anki.AnkiNote;
import com.zhlearn.infrastructure.anki.AnkiNoteParser;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class CliStepDefinitions {

    private final AnkiNoteParser ankiParser = new AnkiNoteParser();

    private int exitCode;
    private String stdout;
    private String stderr;

    // Parse-pleco specific state
    private Path tempHomeDir;
    private Path plecoInputFile;
    private Path ankiExportFile;

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
                    System.out.println(line);
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
                    System.err.println(line);
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

    @Given("I have a Pleco export file with content:")
    public void iHaveAPlecoExportFileWithContent(String tsvContent) throws IOException {
        tempHomeDir = Files.createTempDirectory("zh-learn-e2e-test");
        plecoInputFile = tempHomeDir.resolve("input.tsv");
        ankiExportFile = tempHomeDir.resolve("export.tsv");

        Files.writeString(plecoInputFile, tsvContent, StandardCharsets.UTF_8);
    }

    @When("I run parse-pleco with parameters {string}")
    public void iRunParsePlecoWithParameters(String parameters) throws IOException, InterruptedException {
        Path projectRoot = Paths.get(System.getProperty("user.dir")).getParent();
        Path cliScript = projectRoot.resolve("zh-learn.sh");

        List<String> command = new ArrayList<>();
        command.add(cliScript.toString());
        command.add("parse-pleco");
        command.add(plecoInputFile.toString());

        String[] paramArray = parameters.trim().split("\\s+");
        for (String param : paramArray) {
            command.add(param);
        }

        command.add("--export-anki=" + ankiExportFile.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(projectRoot.toFile());

        // Override user.home to use temp directory for clean cache
        processBuilder.environment().put("JAVA_OPTS",
            "-Duser.home=" + tempHomeDir.toString());

        Process process = processBuilder.start();

        StringBuilder stdoutBuilder = new StringBuilder();
        StringBuilder stderrBuilder = new StringBuilder();

        Thread stdoutThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
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
                    System.err.println(line);
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

    @Then("the Anki export file should exist")
    public void theAnkiExportFileShouldExist() {
        assertThat(ankiExportFile)
            .as("Anki export file should exist at %s", ankiExportFile)
            .exists();
    }

    @Then("each word in the export should have an explanation with more than {int} characters")
    public void eachWordInTheExportShouldHaveAnExplanationWithMoreThanCharacters(int minLength) throws IOException {
        // Use AnkiNoteParser from infrastructure to parse the exported TSV
        List<AnkiNote> notes = ankiParser.parseFile(ankiExportFile);

        assertThat(notes)
            .as("Export file should have at least one parsed note")
            .isNotEmpty();

        for (int i = 0; i < notes.size(); i++) {
            AnkiNote note = notes.get(i);
            String explanation = note.etymology(); // Etymology field contains the explanation

            assertThat(explanation.length())
                .as("Explanation for word '%s' (note %d) should have more than %d characters, but was: %s",
                    note.simplified(), i + 1, minLength, explanation)
                .isGreaterThan(minLength);
        }
    }

    @Then("the definition of {string} should be {string}")
    public void theDefinitionOfShouldBe(String word, String expectedDefinition) throws IOException {
        // Use AnkiNoteParser from infrastructure to parse the exported TSV
        List<AnkiNote> notes = ankiParser.parseFile(ankiExportFile);

        // Find the note for the specified word
        AnkiNote note = notes.stream()
            .filter(n -> word.equals(n.simplified()))
            .findFirst()
            .orElseThrow(() -> new AssertionError(
                String.format("Word '%s' not found in export. Available words: %s",
                    word, notes.stream().map(AnkiNote::simplified).toList())));

        assertThat(note.definition())
            .as("Definition for word '%s' should match expected value", word)
            .isEqualTo(expectedDefinition);
    }

    @After
    public void cleanup() throws IOException {
        if (tempHomeDir != null && Files.exists(tempHomeDir)) {
            try (Stream<Path> paths = Files.walk(tempHomeDir)) {
                paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
            }
        }
    }

    private String stripAnsi(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("\\u001B\\[[0-9;]*[mK]", "");
    }
}
