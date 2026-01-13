package com.zhlearn.cli;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.zhlearn.domain.model.GrammarSentenceFile;
import com.zhlearn.infrastructure.grammar.GrammarSentenceMarkdownParser;
import com.zhlearn.infrastructure.grammar.GrammarSentenceTsvExporter;
import com.zhlearn.infrastructure.grammar.GrammarSentenceTsvExporter.ResolvedSentenceFile;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** CLI command to parse grammar sentence markdown files and export to Anki TSV format. */
@Command(
        name = "export-sentences",
        description = "Parse grammar sentence markdown files and export to Anki TSV format")
public class ExportSentencesCommand implements Runnable {

    @Parameters(
            index = "0",
            description =
                    "Path to sentence markdown file or directory containing sentence files (*.md)")
    private Path inputPath;

    @Option(
            names = {"-o", "--output"},
            description = "Output TSV file path (default: <input-basename>.tsv or sentences.tsv)")
    private Path outputPath;

    @Option(
            names = {"--dry-run"},
            description = "Parse files and validate but don't write output")
    private boolean dryRun = false;

    @Override
    public void run() {
        GrammarSentenceMarkdownParser parser = new GrammarSentenceMarkdownParser();
        GrammarSentenceTsvExporter exporter = new GrammarSentenceTsvExporter();

        List<Path> filesToProcess = collectFiles(inputPath);

        if (filesToProcess.isEmpty()) {
            System.err.println("No markdown files found at: " + inputPath);
            System.exit(1);
        }

        System.out.println("Found " + filesToProcess.size() + " sentence file(s) to process");

        List<ResolvedSentenceFile> resolvedFiles = new ArrayList<>();

        for (Path file : filesToProcess) {
            System.out.println("  Parsing: " + file.getFileName());
            GrammarSentenceFile sentenceFile = parseFile(parser, file);

            // Resolve the grammar and character files relative to the sentence file
            Path basePath = file.getParent();
            String grammarExplanation = readRelativeFile(basePath, sentenceFile.grammarFilePath());
            String characterBreakdown =
                    sentenceFile.hasCharacterBreakdown()
                            ? readRelativeFileOrEmpty(basePath, sentenceFile.characterFilePath())
                            : "";

            resolvedFiles.add(
                    new ResolvedSentenceFile(sentenceFile, grammarExplanation, characterBreakdown));

            System.out.println(
                    "    Word: " + sentenceFile.word() + " (" + sentenceFile.pinyin() + ")");
            System.out.println("    Grammar Point: " + sentenceFile.grammarPoint());
            System.out.println("    Exercises: " + sentenceFile.exercises().size());
            System.out.println(
                    "    Character breakdown: "
                            + (sentenceFile.hasCharacterBreakdown() ? "yes" : "no"));
        }

        if (dryRun) {
            System.out.println("\nDry run - no output written.");
            System.out.println(
                    "Total: "
                            + resolvedFiles.size()
                            + " file(s), "
                            + resolvedFiles.stream()
                                    .mapToInt(r -> r.sentenceFile().exercises().size())
                                    .sum()
                            + " exercise(s)");
            return;
        }

        // Determine output path
        Path output = determineOutputPath(inputPath, outputPath);

        // Export to TSV
        exportToTsv(exporter, resolvedFiles, output);

        int totalExercises =
                resolvedFiles.stream().mapToInt(r -> r.sentenceFile().exercises().size()).sum();

        System.out.println("\nExported " + totalExercises + " exercises to: " + output);
    }

    private List<Path> collectFiles(Path path) {
        if (Files.isRegularFile(path)) {
            return List.of(path);
        }

        if (Files.isDirectory(path)) {
            try (Stream<Path> files = Files.list(path)) {
                return files.filter(p -> p.toString().endsWith(".md"))
                        .filter(Files::isRegularFile)
                        .sorted()
                        .toList();
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to list files in: " + path, e);
            }
        }

        return List.of();
    }

    private GrammarSentenceFile parseFile(GrammarSentenceMarkdownParser parser, Path file) {
        try {
            return parser.parseFile(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse file: " + file, e);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid sentence file: " + file);
            System.err.println("  Error: " + e.getMessage());
            System.exit(1);
            return null; // unreachable
        }
    }

    private String readRelativeFile(Path basePath, String relativePath) {
        Path resolved = basePath.resolve(relativePath).normalize();
        if (!Files.exists(resolved)) {
            System.err.println("Referenced file not found: " + resolved);
            System.err.println("  (referenced from: " + basePath + ")");
            System.exit(1);
        }
        try {
            return Files.readString(resolved);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file: " + resolved, e);
        }
    }

    private String readRelativeFileOrEmpty(Path basePath, String relativePath) {
        Path resolved = basePath.resolve(relativePath).normalize();
        if (!Files.exists(resolved)) {
            System.out.println("    (character file not found: " + resolved.getFileName() + ")");
            return "";
        }
        try {
            return Files.readString(resolved);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read file: " + resolved, e);
        }
    }

    private Path determineOutputPath(Path input, Path output) {
        if (output != null) {
            return output;
        }

        if (Files.isRegularFile(input)) {
            // Replace .md with .tsv
            String filename = input.getFileName().toString();
            if (filename.endsWith(".md")) {
                filename = filename.substring(0, filename.length() - 3) + ".tsv";
            } else {
                filename = filename + ".tsv";
            }
            return input.getParent().resolve(filename);
        }

        // Directory input - output to sentences.tsv in that directory
        return input.resolve("sentences.tsv");
    }

    private void exportToTsv(
            GrammarSentenceTsvExporter exporter, List<ResolvedSentenceFile> files, Path output) {
        try {
            exporter.exportMultipleToTsv(files, output);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write TSV: " + output, e);
        }
    }
}
