package com.zhlearn.e2e;

import static org.assertj.core.api.Assertions.assertThat;

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

import com.zhlearn.infrastructure.anki.AnkiNote;
import com.zhlearn.infrastructure.anki.AnkiNoteParser;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CliStepDefinitions {

    private final AnkiNoteParser ankiParser = new AnkiNoteParser();

    private int exitCode;
    private String stdout;
    private String stderr;

    // Parse-pleco specific state
    private Path tempHomeDir;
    private Path plecoInputFile;
    private Path ankiExportFile;
    private Path ankiMediaDir;

    // Improve-anki specific state
    private Path ankiInputFile;
    private Path improvedAnkiExportFile;
    private List<AnkiNote> originalNotes;

    // Image-specific state
    private boolean googleImageSearchConfigured = false;
    private boolean forceUnsetGoogleApiCredentials = false;

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

        Thread stdoutThread =
                new Thread(
                        () -> {
                            try (BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    process.getInputStream(),
                                                    StandardCharsets.UTF_8))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    System.out.println(line);
                                    stdoutBuilder.append(line).append("\n");
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

        Thread stderrThread =
                new Thread(
                        () -> {
                            try (BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    process.getErrorStream(),
                                                    StandardCharsets.UTF_8))) {
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
                .as(
                        "Exit code should be %d but was %d. stdout=%s, stderr=%s",
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
    public void iRunParsePlecoWithParameters(String parameters)
            throws IOException, InterruptedException {
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
        processBuilder.environment().put("JAVA_OPTS", "-Duser.home=" + tempHomeDir.toString());

        Process process = processBuilder.start();

        StringBuilder stdoutBuilder = new StringBuilder();
        StringBuilder stderrBuilder = new StringBuilder();

        Thread stdoutThread =
                new Thread(
                        () -> {
                            try (BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    process.getInputStream(),
                                                    StandardCharsets.UTF_8))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    System.out.println(line);
                                    stdoutBuilder.append(line).append("\n");
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

        Thread stderrThread =
                new Thread(
                        () -> {
                            try (BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    process.getErrorStream(),
                                                    StandardCharsets.UTF_8))) {
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

    @When("I run parse-pleco with audio parameters {string}")
    public void iRunParsePlecoWithAudioParameters(String parameters)
            throws IOException, InterruptedException {
        Path projectRoot = Paths.get(System.getProperty("user.dir")).getParent();
        Path cliScript = projectRoot.resolve("zh-learn.sh");

        // Set up Anki media directory
        ankiMediaDir = tempHomeDir.resolve("anki-media");
        Files.createDirectories(ankiMediaDir);

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

        // Override user.home to use temp directory for clean cache and set Anki media directory
        processBuilder
                .environment()
                .put(
                        "JAVA_OPTS",
                        "-Duser.home="
                                + tempHomeDir.toString()
                                + " -Dzhlearn.anki.media.dir="
                                + ankiMediaDir.toString());

        Process process = processBuilder.start();

        StringBuilder stdoutBuilder = new StringBuilder();
        StringBuilder stderrBuilder = new StringBuilder();

        Thread stdoutThread =
                new Thread(
                        () -> {
                            try (BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    process.getInputStream(),
                                                    StandardCharsets.UTF_8))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    System.out.println(line);
                                    stdoutBuilder.append(line).append("\n");
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

        Thread stderrThread =
                new Thread(
                        () -> {
                            try (BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    process.getErrorStream(),
                                                    StandardCharsets.UTF_8))) {
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
    public void eachWordInTheExportShouldHaveAnExplanationWithMoreThanCharacters(int minLength)
            throws IOException {
        // Use AnkiNoteParser from infrastructure to parse the exported TSV
        List<AnkiNote> notes = ankiParser.parseFile(ankiExportFile);

        assertThat(notes).as("Export file should have at least one parsed note").isNotEmpty();

        for (int i = 0; i < notes.size(); i++) {
            AnkiNote note = notes.get(i);
            String explanation = note.etymology(); // Etymology field contains the explanation

            assertThat(explanation.length())
                    .as(
                            "Explanation for word '%s' (note %d) should have more than %d characters, but was: %s",
                            note.simplified(), i + 1, minLength, explanation)
                    .isGreaterThan(minLength);
        }
    }

    @Then("the definition of {string} should be {string}")
    public void theDefinitionOfShouldBe(String word, String expectedDefinition) throws IOException {
        // Use AnkiNoteParser from infrastructure to parse the exported TSV
        List<AnkiNote> notes = ankiParser.parseFile(ankiExportFile);

        // Find the note for the specified word
        AnkiNote note =
                notes.stream()
                        .filter(n -> word.equals(n.simplified()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new AssertionError(
                                                String.format(
                                                        "Word '%s' not found in export. Available words: %s",
                                                        word,
                                                        notes.stream()
                                                                .map(AnkiNote::simplified)
                                                                .toList())));

        assertThat(note.definition())
                .as("Definition for word '%s' should match expected value", word)
                .isEqualTo(expectedDefinition);
    }

    @Then("the audio cache should contain {int} files for word {string}")
    public void theAudioCacheShouldContainFilesForWord(int expectedCount, String word)
            throws IOException {
        Path audioCacheDir = tempHomeDir.resolve(".zh-learn").resolve("audio");
        assertThat(audioCacheDir)
                .as("Audio cache directory should exist at %s", audioCacheDir)
                .exists()
                .isDirectory();

        // Count all audio files across all provider subdirectories that contain the word
        long totalFiles = 0;
        try (Stream<Path> providerDirs = Files.list(audioCacheDir)) {
            List<Path> dirs = providerDirs.filter(Files::isDirectory).toList();
            for (Path providerDir : dirs) {
                try (Stream<Path> files = Files.list(providerDir)) {
                    long count =
                            files.filter(Files::isRegularFile)
                                    .filter(f -> f.getFileName().toString().contains(word))
                                    .count();
                    totalFiles += count;
                }
            }
        }

        assertThat(totalFiles)
                .as("Audio cache should contain %d files for word '%s'", expectedCount, word)
                .isEqualTo(expectedCount);
    }

    @Then("the Anki media directory should contain the selected audio for {string}")
    public void theAnkiMediaDirectoryShouldContainSelectedAudioFor(String word) throws IOException {
        assertThat(ankiMediaDir)
                .as("Anki media directory should exist at %s", ankiMediaDir)
                .exists()
                .isDirectory();

        // Find audio files for the word in the Anki media directory
        try (Stream<Path> files = Files.list(ankiMediaDir)) {
            long count =
                    files.filter(Files::isRegularFile)
                            .filter(f -> f.getFileName().toString().contains(word))
                            .filter(f -> f.getFileName().toString().endsWith(".mp3"))
                            .count();

            assertThat(count)
                    .as(
                            "Anki media directory should contain at least one audio file for '%s'",
                            word)
                    .isGreaterThan(0);
        }
    }

    @Then("the Anki export for {string} should reference the correct audio file")
    public void theAnkiExportForShouldReferenceCorrectAudioFile(String word) throws IOException {
        // Parse the Anki export file
        List<AnkiNote> notes = ankiParser.parseFile(ankiExportFile);

        // Find the note for the specified word
        AnkiNote note =
                notes.stream()
                        .filter(n -> word.equals(n.simplified()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new AssertionError(
                                                String.format(
                                                        "Word '%s' not found in export. Available words: %s",
                                                        word,
                                                        notes.stream()
                                                                .map(AnkiNote::simplified)
                                                                .toList())));

        // Verify pronunciation field contains a sound reference
        String pronunciation = note.pronunciation();
        assertThat(pronunciation)
                .as("Pronunciation field for '%s' should not be empty", word)
                .isNotEmpty();

        assertThat(pronunciation)
                .as("Pronunciation field for '%s' should contain [sound:...] reference", word)
                .matches("\\[sound:[^\\]]+\\.mp3\\]");

        // Extract the filename from [sound:filename.mp3]
        String filename = pronunciation.replaceAll("\\[sound:([^\\]]+)\\]", "$1");

        // Verify the file exists in the Anki media directory
        Path audioFile = ankiMediaDir.resolve(filename);
        assertThat(audioFile)
                .as(
                        "Audio file '%s' referenced in export should exist in Anki media directory",
                        filename)
                .exists()
                .isRegularFile();
    }

    @Given("I have an Anki export file with content:")
    public void iHaveAnAnkiExportFileWithContent(String tsvContent) throws IOException {
        tempHomeDir = Files.createTempDirectory("zh-learn-e2e-test");
        ankiInputFile = tempHomeDir.resolve("input-anki.tsv");
        improvedAnkiExportFile = tempHomeDir.resolve("improved-export.tsv");

        Files.writeString(ankiInputFile, tsvContent, StandardCharsets.UTF_8);

        // Parse and store original notes for later comparison
        originalNotes = ankiParser.parseFile(ankiInputFile);
    }

    @When("I run improve-anki with parameters {string}")
    public void iRunImproveAnkiWithParameters(String parameters)
            throws IOException, InterruptedException {
        Path projectRoot = Paths.get(System.getProperty("user.dir")).getParent();
        Path cliScript = projectRoot.resolve("zh-learn.sh");

        List<String> command = new ArrayList<>();
        command.add(cliScript.toString());
        command.add("improve-anki");
        command.add(ankiInputFile.toString());

        String[] paramArray = parameters.trim().split("\\s+");
        for (String param : paramArray) {
            command.add(param);
        }

        command.add("--export-anki=" + improvedAnkiExportFile.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(projectRoot.toFile());

        // Override user.home to use temp directory for clean cache
        processBuilder.environment().put("JAVA_OPTS", "-Duser.home=" + tempHomeDir.toString());

        Process process = processBuilder.start();

        StringBuilder stdoutBuilder = new StringBuilder();
        StringBuilder stderrBuilder = new StringBuilder();

        Thread stdoutThread =
                new Thread(
                        () -> {
                            try (BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    process.getInputStream(),
                                                    StandardCharsets.UTF_8))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    System.out.println(line);
                                    stdoutBuilder.append(line).append("\n");
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

        Thread stderrThread =
                new Thread(
                        () -> {
                            try (BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    process.getErrorStream(),
                                                    StandardCharsets.UTF_8))) {
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

    @Then("the improved Anki export file should exist")
    public void theImprovedAnkiExportFileShouldExist() {
        assertThat(improvedAnkiExportFile)
                .as("Improved Anki export file should exist at %s", improvedAnkiExportFile)
                .exists();
    }

    @Then("the audio field of {string} should not be empty")
    public void theAudioFieldOfShouldNotBeEmpty(String word) throws IOException {
        List<AnkiNote> improvedNotes = ankiParser.parseFile(improvedAnkiExportFile);

        AnkiNote improvedNote =
                improvedNotes.stream()
                        .filter(n -> word.equals(n.simplified()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new AssertionError(
                                                String.format(
                                                        "Word '%s' not found in improved export",
                                                        word)));

        assertThat(improvedNote.pronunciation())
                .as("Audio field for word '%s' should not be empty", word)
                .isNotEmpty();
    }

    @Then("the {word} field of {string} should remain unchanged")
    public void theFieldOfShouldRemainUnchanged(String fieldName, String word) throws IOException {
        // Find original note
        AnkiNote originalNote =
                originalNotes.stream()
                        .filter(n -> word.equals(n.simplified()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new AssertionError(
                                                String.format(
                                                        "Word '%s' not found in original export",
                                                        word)));

        // Find improved note
        List<AnkiNote> improvedNotes = ankiParser.parseFile(improvedAnkiExportFile);
        AnkiNote improvedNote =
                improvedNotes.stream()
                        .filter(n -> word.equals(n.simplified()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new AssertionError(
                                                String.format(
                                                        "Word '%s' not found in improved export",
                                                        word)));

        // Get field values
        String originalValue = getFieldValue(originalNote, fieldName);
        String improvedValue = getFieldValue(improvedNote, fieldName);

        assertThat(improvedValue)
                .as(
                        "Field '%s' for word '%s' should remain unchanged. Original: '%s', Improved: '%s'",
                        fieldName, word, originalValue, improvedValue)
                .isEqualTo(originalValue);
    }

    @Then("the {word} field of {string} should contain more than {int} characters")
    public void theFieldOfShouldContainMoreThanCharacters(
            String fieldName, String word, int minLength) throws IOException {
        List<AnkiNote> improvedNotes = ankiParser.parseFile(improvedAnkiExportFile);

        AnkiNote improvedNote =
                improvedNotes.stream()
                        .filter(n -> word.equals(n.simplified()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new AssertionError(
                                                String.format(
                                                        "Word '%s' not found in improved export",
                                                        word)));

        String fieldValue = getFieldValue(improvedNote, fieldName);

        assertThat(fieldValue.length())
                .as(
                        "Field '%s' for word '%s' should have more than %d characters, but was: %s",
                        fieldName, word, minLength, fieldValue)
                .isGreaterThan(minLength);
    }

    private String getFieldValue(AnkiNote note, String fieldName) {
        return switch (fieldName.toLowerCase()) {
            case "audio", "pronunciation" -> note.pronunciation();
            case "etymology", "explanation" -> note.etymology();
            case "examples" -> note.examples();
            case "definition" -> note.definition();
            case "pinyin" -> note.pinyin();
            case "components", "decomposition" -> note.components();
            default ->
                    throw new IllegalArgumentException(
                            "Unknown field name: "
                                    + fieldName
                                    + ". Valid fields: audio, pronunciation, etymology, explanation, examples, definition, pinyin, components, decomposition");
        };
    }

    @Given("Google Custom Search API is configured")
    public void googleCustomSearchAPIIsConfigured() {
        // Set mock values for Google Custom Search API credentials
        // In a real test, these would be set from actual environment variables
        // For now, we just mark that they should be configured
        String apiKey = System.getenv("GOOGLE_SEARCH_API_KEY");
        String engineId = System.getenv("GOOGLE_SEARCH_ENGINE_ID");

        assertThat(apiKey)
                .as("GOOGLE_SEARCH_API_KEY environment variable must be set for image scenarios")
                .isNotNull()
                .isNotBlank();
        assertThat(engineId)
                .as("GOOGLE_SEARCH_ENGINE_ID environment variable must be set for image scenarios")
                .isNotNull()
                .isNotBlank();
        googleImageSearchConfigured = true;
    }

    @Given("Google Search API key is not configured")
    public void googleSearchApiKeyIsNotConfigured() {
        forceUnsetGoogleApiCredentials = true;
        googleImageSearchConfigured = false;
    }

    @Given("the Anki media directory is set up")
    public void theAnkiMediaDirectoryIsSetUp() throws IOException {
        if (tempHomeDir == null) {
            tempHomeDir = Files.createTempDirectory("zh-learn-e2e-test");
        }
        ankiMediaDir = tempHomeDir.resolve("anki-media");
        Files.createDirectories(ankiMediaDir);
    }

    @Given("Anki media directory does not exist")
    public void ankiMediaDirectoryDoesNotExist() throws IOException {
        if (tempHomeDir == null) {
            tempHomeDir = Files.createTempDirectory("zh-learn-e2e-test");
        }
        ankiMediaDir = tempHomeDir.resolve("missing-anki-media");
        if (Files.exists(ankiMediaDir)) {
            try (Stream<Path> files = Files.walk(ankiMediaDir)) {
                files.sorted(Comparator.reverseOrder())
                        .forEach(
                                path -> {
                                    try {
                                        Files.delete(path);
                                    } catch (IOException e) {
                                        // Ignore cleanup errors
                                    }
                                });
            }
        }
        // Ensure directory truly does not exist
        Files.deleteIfExists(ankiMediaDir);
    }

    @When("I run improve-anki without --improve-images flag")
    public void iRunImproveAnkiWithoutImproveImagesFlag() throws IOException, InterruptedException {
        iRunImproveAnkiWithParameters("--improve-examples --example-provider=dummy --skip-audio");
    }

    @When("I run improve-anki with image parameters {string}")
    public void iRunImproveAnkiWithImageParameters(String parameters)
            throws IOException, InterruptedException {
        Path projectRoot = Paths.get(System.getProperty("user.dir")).getParent();
        Path cliScript = projectRoot.resolve("zh-learn.sh");

        List<String> command = new ArrayList<>();
        command.add(cliScript.toString());
        command.add("improve-anki");
        command.add(ankiInputFile.toString());

        String[] paramArray = parameters.trim().split("\\s+");
        for (String param : paramArray) {
            command.add(param);
        }

        command.add("--export-anki=" + improvedAnkiExportFile.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(projectRoot.toFile());

        // Override user.home and set Anki media directory
        String javaOpts = "-Duser.home=" + tempHomeDir.toString();
        if (ankiMediaDir != null) {
            javaOpts += " -Dzhlearn.anki.media.dir=" + ankiMediaDir.toString();
        }

        // Pass through Google Custom Search API credentials from environment if configured
        if (forceUnsetGoogleApiCredentials) {
            processBuilder.environment().remove("GOOGLE_SEARCH_API_KEY");
            processBuilder.environment().remove("GOOGLE_SEARCH_ENGINE_ID");
        } else if (googleImageSearchConfigured) {
            String apiKey = System.getenv("GOOGLE_SEARCH_API_KEY");
            String engineId = System.getenv("GOOGLE_SEARCH_ENGINE_ID");
            if (apiKey != null) {
                processBuilder.environment().put("GOOGLE_SEARCH_API_KEY", apiKey);
            }
            if (engineId != null) {
                processBuilder.environment().put("GOOGLE_SEARCH_ENGINE_ID", engineId);
            }
        }

        processBuilder.environment().put("JAVA_OPTS", javaOpts);

        Process process = processBuilder.start();

        StringBuilder stdoutBuilder = new StringBuilder();
        StringBuilder stderrBuilder = new StringBuilder();

        Thread stdoutThread =
                new Thread(
                        () -> {
                            try (BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    process.getInputStream(),
                                                    StandardCharsets.UTF_8))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    System.out.println(line);
                                    stdoutBuilder.append(line).append("\n");
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

        Thread stderrThread =
                new Thread(
                        () -> {
                            try (BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    process.getErrorStream(),
                                                    StandardCharsets.UTF_8))) {
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

    @Then("the definition field of {string} should contain image references")
    public void theDefinitionFieldOfShouldContainImageReferences(String word) throws IOException {
        List<AnkiNote> improvedNotes = ankiParser.parseFile(improvedAnkiExportFile);

        AnkiNote improvedNote =
                improvedNotes.stream()
                        .filter(n -> word.equals(n.simplified()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new AssertionError(
                                                String.format(
                                                        "Word '%s' not found in improved export",
                                                        word)));

        String definition = improvedNote.definition();

        assertThat(definition)
                .as("Definition field for word '%s' should contain image references", word)
                .contains("<img");
    }

    @Then("the Anki media directory should contain {int} images for {string}")
    public void theAnkiMediaDirectoryShouldContainImagesFor(int expectedCount, String word)
            throws IOException {
        assertThat(ankiMediaDir)
                .as("Anki media directory should exist at %s", ankiMediaDir)
                .exists()
                .isDirectory();

        // Find image files for the word in the Anki media directory
        try (Stream<Path> files = Files.list(ankiMediaDir)) {
            long count =
                    files.filter(Files::isRegularFile)
                            .filter(f -> f.getFileName().toString().contains(word))
                            .filter(
                                    f -> {
                                        String fileName = f.getFileName().toString();
                                        return fileName.endsWith(".jpg")
                                                || fileName.endsWith(".jpeg")
                                                || fileName.endsWith(".png")
                                                || fileName.endsWith(".webp");
                                    })
                            .count();

            assertThat(count)
                    .as(
                            "Anki media directory should contain %d image(s) for word '%s'",
                            expectedCount, word)
                    .isEqualTo(expectedCount);
        }
    }

    @Then("the exit code should be non-zero")
    public void theExitCodeShouldBeNonZero() {
        assertThat(exitCode)
                .as("Exit code should be non-zero. stdout=%s, stderr=%s", stdout, stderr)
                .isNotZero();
    }

    @Then("no images are downloaded")
    public void noImagesAreDownloaded() throws IOException {
        assertThat(ankiMediaDir)
                .as("Anki media directory should exist at %s for verification", ankiMediaDir)
                .exists()
                .isDirectory();

        try (Stream<Path> files = Files.list(ankiMediaDir)) {
            long count = files.filter(Files::isRegularFile).count();
            assertThat(count)
                    .as("Expected no image files to be downloaded, but found %d", count)
                    .isZero();
        }
    }

    @Then("the definition field contains plain text only")
    public void theDefinitionFieldContainsPlainTextOnly() throws IOException {
        List<AnkiNote> improvedNotes = ankiParser.parseFile(improvedAnkiExportFile);
        assertThat(improvedNotes)
                .as("Improved export should contain at least one note")
                .isNotEmpty();

        AnkiNote improved = improvedNotes.get(0);
        assertThat(improved.definition())
                .as("Definition should not contain image tags")
                .doesNotContain("<img");

        if (originalNotes != null && !originalNotes.isEmpty()) {
            AnkiNote original = originalNotes.get(0);
            assertThat(improved.definition())
                    .as("Definition should remain unchanged when images are not enabled")
                    .isEqualTo(original.definition());
        }
    }

    @Then("the error message should mention {string}")
    public void theErrorMessageShouldMention(String expected) {
        String stripped = stripAnsi(stderr);
        assertThat(stripped)
                .as("Expected error message to mention \"%s\" but was: %s", expected, stripped)
                .contains(expected);
    }

    @After
    public void cleanup() throws IOException {
        googleImageSearchConfigured = false;
        forceUnsetGoogleApiCredentials = false;
        if (tempHomeDir != null && Files.exists(tempHomeDir)) {
            try (Stream<Path> paths = Files.walk(tempHomeDir)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(
                                path -> {
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
