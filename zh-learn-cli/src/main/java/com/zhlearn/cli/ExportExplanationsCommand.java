package com.zhlearn.cli;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.zhlearn.domain.model.ExplanationEntry;
import com.zhlearn.infrastructure.explanation.ExplanationMarkdownParser;
import com.zhlearn.infrastructure.explanation.ExplanationTsvExporter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

/** CLI command to parse Chinese explanation markdown and export to Anki TSV format. */
@Command(
        name = "export-explanations",
        description = "Parse Chinese explanation markdown and export to Anki TSV format")
public class ExportExplanationsCommand implements Runnable {

    @Parameters(index = "0", description = "Path to explanations markdown file")
    private Path inputPath;

    @Option(
            names = {"-o", "--output"},
            description = "Output TSV file path (default: <input-basename>.tsv)")
    private Path outputPath;

    @Option(
            names = {"--dry-run"},
            description = "Parse and validate but don't write output")
    private boolean dryRun = false;

    @Option(
            names = {"--type"},
            description = "Filter by entry type (e.g. word, phrase, sentence, idiom)")
    private String typeFilter;

    @ParentCommand private MainCommand parent;

    @Override
    public void run() {
        if (!Files.isRegularFile(inputPath)) {
            System.err.println("File not found: " + inputPath);
            System.exit(1);
        }

        ExplanationMarkdownParser parser = new ExplanationMarkdownParser();
        ExplanationTsvExporter exporter = new ExplanationTsvExporter();

        List<ExplanationEntry> entries = parseFile(parser);

        System.out.println("Parsed " + entries.size() + " entries");

        // Print type breakdown
        Map<String, Long> typeCounts =
                entries.stream()
                        .collect(
                                Collectors.groupingBy(
                                        ExplanationEntry::entryType, Collectors.counting()));
        typeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue()));

        // Apply type filter
        if (typeFilter != null) {
            String filter = typeFilter.toLowerCase();
            entries = entries.stream().filter(e -> e.entryType().equals(filter)).toList();
            System.out.println(
                    "After filtering by '" + filter + "': " + entries.size() + " entries");
        }

        if (dryRun) {
            System.out.println("\nDry run - no output written.");
            return;
        }

        if (entries.isEmpty()) {
            System.err.println("No entries to export.");
            System.exit(1);
        }

        Path output = determineOutputPath();
        exportToTsv(exporter, entries, output);
        System.out.println("\nExported " + entries.size() + " entries to: " + output);
    }

    private List<ExplanationEntry> parseFile(ExplanationMarkdownParser parser) {
        try {
            return parser.parseFile(inputPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse file: " + inputPath, e);
        }
    }

    private Path determineOutputPath() {
        if (outputPath != null) {
            return outputPath;
        }
        String filename = inputPath.getFileName().toString();
        if (filename.endsWith(".md")) {
            filename = filename.substring(0, filename.length() - 3) + ".tsv";
        } else {
            filename = filename + ".tsv";
        }
        return inputPath.getParent().resolve(filename);
    }

    private void exportToTsv(
            ExplanationTsvExporter exporter, List<ExplanationEntry> entries, Path output) {
        Path markdownDir = inputPath.getParent();
        Path ankiMediaDir = parent.getAnkiMediaLocator().locate().orElse(null);
        if (ankiMediaDir != null) {
            System.out.println("Anki media directory: " + ankiMediaDir);
        } else {
            System.out.println(
                    "Anki media directory not found"
                            + " - audio references will be set but files not copied");
        }
        try {
            exporter.exportToTsv(entries, output, markdownDir, ankiMediaDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write TSV: " + output, e);
        }
    }
}
