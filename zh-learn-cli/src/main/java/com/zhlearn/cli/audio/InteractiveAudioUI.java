package com.zhlearn.cli.audio;

import java.io.IOException;
import java.io.PrintWriter;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import com.zhlearn.application.audio.PronunciationCandidate;
import com.zhlearn.application.audio.SelectionSession;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;

public class InteractiveAudioUI {

    public PronunciationCandidate run(SelectionSession session, Hanzi word, Pinyin pinyin) {
        TriState result = runUsingJLine(session, word, pinyin);
        return switch (result.state) {
            case SELECTED -> result.value;
            case SKIPPED, UNAVAILABLE -> null;
        };
    }

    private TriState runUsingJLine(SelectionSession session, Hanzi word, Pinyin pinyin) {
        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            if ("dumb".equalsIgnoreCase(terminal.getType())) {
                return TriState.unavailable();
            }
            terminal.enterRawMode();
            PrintWriter writer = terminal.writer();
            renderModernUI(writer, session, word, pinyin);
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
                    session.pressEnter();
                    clearScreen(writer);
                    return TriState.selected(session.selected());
                } else if (ch == ' ') {
                    session.pressSpace();
                } else if (ch == 'q' || ch == 'Q' || ch == 's' || ch == 'S') {
                    clearScreen(writer);
                    return TriState.skipped();
                }
                renderModernUI(writer, session, word, pinyin);
                writer.flush();
            }
            clearScreen(writer);
            return TriState.skipped();
        } catch (IOException e) {
            throw new RuntimeException("JLine interactive UI failed", e);
        }
    }

    private void renderModernUI(
            PrintWriter writer, SelectionSession session, Hanzi word, Pinyin pinyin) {
        clearScreen(writer);

        // Fixed width approach to ensure perfect alignment
        int boxWidth = 70; // Fixed box width for consistent alignment

        // Enhanced header using TerminalFormatter styling
        writer.println();
        writer.print("\033[34m"); // Blue (Box color)
        writer.print("┌─ ");
        writer.print("\033[1m\033[37m"); // Bold white for "Pronunciation for:"
        writer.print("Pronunciation for: ");
        writer.print("\033[36m"); // Cyan (Chinese color)
        writer.print(word.characters());
        writer.print(" ");
        writer.print("\033[33m"); // Yellow (Pinyin color)
        writer.print(pinyin.pinyin());
        writer.print("\033[0m\033[34m"); // Reset to blue for border
        writer.print(" ");

        // Calculate remaining dashes for top border to match total width
        String titleContent =
                "┌─ Pronunciation for: " + word.characters() + " " + pinyin.pinyin() + " ";
        int remainingDashes = boxWidth - titleContent.length() - 1; // -1 for the closing ┐
        writer.print("─".repeat(Math.max(0, remainingDashes)));
        writer.println("┐");
        writer.print("\033[0m"); // Reset
        writer.println();

        // List items with improved styling
        for (int i = 0; i < session.size(); i++) {
            PronunciationCandidate candidate = session.candidateAt(i);
            boolean selected = (i == session.currentIndex());

            if (selected) {
                // Selected item: only highlight the arrow in green
                writer.print("  ");
                writer.print("\033[32m"); // Green color for arrow
                writer.print("❯ ");
                writer.print("\033[0m"); // Reset
                writer.print(formatItem(candidate));
            } else {
                // Unselected item: normal with space
                writer.print("    ");
                writer.print(formatItem(candidate));
            }
            writer.println();
        }

        writer.println();
        writer.print("\033[34m"); // Blue (Box color)
        writer.print("└");
        writer.print("─".repeat(boxWidth - 2)); // Total width minus corners (└ and ┘)
        writer.println("┘");
        writer.print("\033[0m"); // Reset
        writer.println();

        // Instructions with muted styling
        writer.print("\033[90m"); // Dark gray
        writer.print("  ↑/↓ Navigate   Space Replay   Enter Select   Esc Skip");
        writer.print("\033[0m"); // Reset
        writer.println();
    }

    private String formatItem(PronunciationCandidate candidate) {
        String provider = formatProviderName(candidate.label());
        String description = candidate.displayDescription();
        return String.format("%-12s %s", provider, description);
    }

    private String formatProviderName(String providerLabel) {
        return switch (providerLabel) {
            case "anki" -> "Anki";
            case "forvo" -> "Forvo";
            case "qwen-tts" -> "Qwen TTS";
            case "tencent-tts" -> "Tencent TTS";
            default -> providerLabel;
        };
    }

    private void clearScreen(PrintWriter writer) {
        writer.print("\033[2J\033[H\033[?25h"); // Clear screen, home cursor, show cursor
    }
}

// Small tri-state to distinguish selected/skip/unavailable
class TriState {
    enum State {
        SELECTED,
        SKIPPED,
        UNAVAILABLE
    }

    final State state;
    final PronunciationCandidate value;

    private TriState(State s, PronunciationCandidate v) {
        this.state = s;
        this.value = v;
    }

    static TriState selected(PronunciationCandidate v) {
        return new TriState(State.SELECTED, v);
    }

    static TriState skipped() {
        return new TriState(State.SKIPPED, null);
    }

    static TriState unavailable() {
        return new TriState(State.UNAVAILABLE, null);
    }
}
