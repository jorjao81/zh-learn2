package com.zhlearn.cli.image;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import com.zhlearn.application.image.ImageSelectionSession;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.ImageCandidate;

/** Interactive UI for selecting multiple images with iTerm2 inline preview support. */
public class InteractiveImageUI {

    private final HttpClient httpClient;
    private final Supplier<Terminal> terminalSupplier;
    private final Function<String, String> environmentLookup;

    public InteractiveImageUI() {
        this(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),
                () -> {
                    try {
                        return TerminalBuilder.builder().system(true).build();
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to create system terminal", e);
                    }
                },
                System::getenv);
    }

    InteractiveImageUI(
            HttpClient httpClient,
            Supplier<Terminal> terminalSupplier,
            Function<String, String> environmentLookup) {
        this.httpClient = httpClient;
        this.terminalSupplier = terminalSupplier;
        this.environmentLookup = environmentLookup;
    }

    /**
     * Run interactive image selection UI.
     *
     * @param session the selection session
     * @param word the Chinese word being processed
     * @return list of selected images, empty list if none selected, or null if skipped
     */
    public List<ImageCandidate> run(ImageSelectionSession session, Hanzi word) {
        TriState result = runUsingJLine(session, word);
        return switch (result.state) {
            case SELECTED -> result.value;
            case SKIPPED, UNAVAILABLE -> null;
        };
    }

    private TriState runUsingJLine(ImageSelectionSession session, Hanzi word) {
        try (Terminal terminal = terminalSupplier.get()) {
            if ("dumb".equalsIgnoreCase(terminal.getType())) {
                return TriState.unavailable();
            }
            terminal.enterRawMode();
            PrintWriter writer = terminal.writer();
            renderUI(writer, session, word);
            writer.flush();
            NonBlockingReader reader = terminal.reader();
            while (true) {
                int ch = reader.read();
                if (ch == -1) break; // EOF
                if (ch == 27) { // ESC
                    int peek = reader.peek(25);
                    if (peek == NonBlockingReader.READ_EXPIRED || peek == -1) {
                        clearScreen(writer);
                        return TriState.skipped();
                    }
                    int n1 = reader.read();
                    if (n1 == '[' || n1 == 'O') {
                        int n2 = reader.read();
                        if (n2 == 'A') session.pressUp();
                        else if (n2 == 'B') session.pressDown();
                    } else {
                        clearScreen(writer);
                        return TriState.skipped();
                    }
                } else if (ch == '\r' || ch == '\n') {
                    clearScreen(writer);
                    return TriState.selected(session.getSelected());
                } else if (ch == ' ') {
                    session.pressSpace();
                } else if (ch == 'q' || ch == 'Q' || ch == 's' || ch == 'S') {
                    clearScreen(writer);
                    return TriState.skipped();
                }
                renderUI(writer, session, word);
                writer.flush();
            }
            clearScreen(writer);
            return TriState.skipped();
        } catch (IOException e) {
            throw new RuntimeException("JLine interactive UI failed", e);
        }
    }

    private void renderUI(PrintWriter writer, ImageSelectionSession session, Hanzi word) {
        clearScreen(writer);

        int boxWidth = 70;

        // Header
        writer.println();
        writer.print("\033[34m"); // Blue
        writer.print("┌─ ");
        writer.print("\033[1m\033[37m"); // Bold white
        writer.print("Select Images for: ");
        writer.print("\033[36m"); // Cyan
        writer.print(word.characters());
        writer.print("\033[0m\033[34m"); // Reset to blue
        writer.print(" ");

        String titleContent = "┌─ Select Images for: " + word.characters() + " ";
        int remainingDashes = boxWidth - titleContent.length() - 1;
        writer.print("─".repeat(Math.max(0, remainingDashes)));
        writer.println("┐");
        writer.print("\033[0m"); // Reset
        writer.println();

        // List items
        for (int i = 0; i < session.size(); i++) {
            ImageCandidate candidate = session.candidateAt(i);
            boolean current = (i == session.currentIndex());
            boolean selected = session.isSelected(i);

            if (current) {
                writer.print("  ");
                writer.print("\033[32m"); // Green arrow
                writer.print("❯ ");
                writer.print("\033[0m");
            } else {
                writer.print("    ");
            }

            // Selection indicator
            if (selected) {
                writer.print("\033[32m✓\033[0m "); // Green checkmark
            } else {
                writer.print("○ ");
            }

            writer.print(formatItem(candidate, i + 1));
            writer.println();

            // Show inline image preview for current item (iTerm2 only)
            if (current && isITerm2()) {
                displayInlineImage(writer, candidate);
            }
        }

        writer.println();
        writer.print("\033[34m"); // Blue
        writer.print("└");
        writer.print("─".repeat(boxWidth - 2));
        writer.println("┘");
        writer.print("\033[0m"); // Reset
        writer.println();

        // Instructions
        writer.print("\033[90m"); // Dark gray
        writer.print(
                "  ↑/↓ Navigate   Space Toggle   Enter Confirm ("
                        + session.selectedCount()
                        + " selected)   Esc Skip");
        writer.print("\033[0m");
        writer.println();
    }

    private String formatItem(ImageCandidate candidate, int number) {
        int width = candidate.metadata().width();
        int height = candidate.metadata().height();
        String dimensions = width + "×" + height;

        String title = candidate.metadata().title().orElse(candidate.metadata().thumbnailUrl());
        if (title.length() > 40) {
            title = title.substring(0, 37) + "...";
        }

        return String.format("Image %d (%s) - %s", number, dimensions, title);
    }

    private boolean isITerm2() {
        // Check TERM_PROGRAM environment variable
        String termProgram = environmentLookup.apply("TERM_PROGRAM");
        return "iTerm.app".equals(termProgram);
    }

    private void displayInlineImage(PrintWriter writer, ImageCandidate candidate) {
        try {
            // Download thumbnail
            String thumbnailUrl = candidate.metadata().thumbnailUrl();
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(thumbnailUrl))
                            .timeout(Duration.ofSeconds(5))
                            .GET()
                            .build();

            HttpResponse<byte[]> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                // Encode to base64
                String base64Image = Base64.getEncoder().encodeToString(response.body());

                // iTerm2 inline image protocol
                // ESC ] 1337 ; File=inline=1;width=40;preserveAspectRatio=1:[base64] BEL
                writer.print("      "); // Indent
                writer.print("\033]1337;File=inline=1;width=40;preserveAspectRatio=1:");
                writer.print(base64Image);
                writer.print("\007"); // BEL character
                writer.println();
            }
        } catch (IOException e) {
            // Silently fail if image preview doesn't work
            // The user can still see metadata
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // Silently fail if interrupted - user can still see metadata
        } catch (IllegalArgumentException e) {
            // Silently fail if thumbnail URL is invalid
            // The user can still see metadata
        }
    }

    private void clearScreen(PrintWriter writer) {
        writer.print("\033[2J\033[H\033[?25h"); // Clear screen, home cursor, show cursor
    }
}

// Tri-state to distinguish selected/skip/unavailable
class TriState {
    enum State {
        SELECTED,
        SKIPPED,
        UNAVAILABLE
    }

    final State state;
    final List<ImageCandidate> value;

    private TriState(State s, List<ImageCandidate> v) {
        this.state = s;
        this.value = v;
    }

    static TriState selected(List<ImageCandidate> v) {
        return new TriState(State.SELECTED, v);
    }

    static TriState skipped() {
        return new TriState(State.SKIPPED, null);
    }

    static TriState unavailable() {
        return new TriState(State.UNAVAILABLE, null);
    }
}
