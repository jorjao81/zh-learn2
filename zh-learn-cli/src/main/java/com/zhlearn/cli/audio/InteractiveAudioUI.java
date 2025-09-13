package com.zhlearn.cli.audio;

import com.zhlearn.application.audio.PronunciationCandidate;
import com.zhlearn.application.audio.SelectionSession;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.Display;
import org.jline.utils.NonBlockingReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class InteractiveAudioUI {

    public PronunciationCandidate run(SelectionSession session) {
        TriState result = runUsingJLine(session);
        if (result.state == TriState.State.SELECTED) {
            return result.value;
        }
        if (result.state == TriState.State.SKIPPED) {
            return null; // explicit skip; do not fallback
        }
        // JLine unavailable; fallback to simple loop
        return runFallback(session);
    }

    private TriState runUsingJLine(SelectionSession session) {
        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            if ("dumb".equalsIgnoreCase(terminal.getType())) {
                return TriState.unavailable(); // fall back in non-interactive environments
            }
            terminal.enterRawMode();
            var writer = terminal.writer();
            renderAnsi(writer, session);
            writer.flush();
            NonBlockingReader reader = terminal.reader();
            while (true) {
                int ch = reader.read();
                if (ch == -1) break; // EOF
                if (ch == 27) { // ESC
                    // Peek quickly to see if this is an escape sequence or lone ESC
                    int peek = reader.peek(25);
                    if (peek == NonBlockingReader.READ_EXPIRED || peek == -1) {
                        writer.print("\u001B[2J\u001B[H\u001B[?25h");
                        writer.flush();
                        return TriState.skipped();
                    }
                    int n1 = reader.read();
                    if (n1 == '[' || n1 == 'O') {
                        int n2 = reader.read();
                        if (n2 == 'A') session.pressUp();
                        else if (n2 == 'B') session.pressDown();
                    } else {
                        // ESC alone: clear screen and return
                        writer.print("\u001B[2J\u001B[H\u001B[?25h");
                        writer.flush();
                        return TriState.skipped();
                    }
                } else if (ch == '\r' || ch == '\n') {
                    session.pressEnter();
                    writer.print("\u001B[2J\u001B[H\u001B[?25h");
                    writer.flush();
                    return TriState.selected(session.selected());
                } else if (ch == ' ') {
                    session.pressSpace();
                } else if (ch == 'q' || ch == 'Q' || ch == 's' || ch == 'S') {
                    writer.print("\u001B[2J\u001B[H\u001B[?25h");
                    writer.flush();
                    return TriState.skipped();
                }
                renderAnsi(writer, session);
                writer.flush();
            }
            // EOF → treat as skip; restore cursor
            writer.print("\u001B[2J\u001B[H\u001B[?25h");
            writer.flush();
            return TriState.skipped();
        } catch (Exception e) {
            System.err.println("[ui] JLine loop failed: " + e.getClass().getName() + ": " + e.getMessage());
            return TriState.unavailable();
        }
    }

    private void renderAnsi(java.io.PrintWriter writer, SelectionSession session) {
        // Clear screen and move cursor to home; hide cursor
        writer.print("\u001B[2J\u001B[H\u001B[?25l");
        writer.println();
        for (int i = 0; i < session.size(); i++) {
            PronunciationCandidate c = session.candidateAt(i);
            boolean selected = (i == session.currentIndex());
            String marker = selected ? ">" : " ";
            String line = String.format("%s %2d. %-20s %s", marker, i + 1, c.label(), c.soundNotation());
            if (selected) {
                writer.print("\u001B[7m"); // reverse video
                writer.println(line);
                writer.print("\u001B[0m");
            } else {
                writer.println(line);
            }
        }
        writer.println();
        writer.println("Use ↑/↓ to navigate, Space to replay, Enter to select, Esc to quit");
    }

    private PronunciationCandidate runFallback(SelectionSession session) {
        printList(session);
        System.out.println("Commands: n=down, p=up, space=replay, Enter=select, q=quit");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.print("> ");
                String line = br.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.isEmpty()) { session.pressEnter(); break; }
                if (line.equalsIgnoreCase("q")) break;
                else if (line.equalsIgnoreCase("n")) session.pressDown();
                else if (line.equalsIgnoreCase("p")) session.pressUp();
                else if (line.equalsIgnoreCase("space") || line.equals(" ")) session.pressSpace();
                else System.out.println("Unknown command: " + line);
                printList(session);
            }
        } catch (Exception ignored) {}
        return session.selected();
    }

    private void printList(SelectionSession session) {
        System.out.println();
        for (int i = 0; i < session.size(); i++) {
            PronunciationCandidate c = session.candidateAt(i);
            String marker = (i == session.currentIndex()) ? ">" : " ";
            System.out.printf("%s %2d. %-20s %s%n", marker, i + 1, c.label(), c.soundNotation());
        }
        System.out.println();
    }
}

// Small tri-state to distinguish selected/skip/unavailable
class TriState {
    enum State { SELECTED, SKIPPED, UNAVAILABLE }
    final State state;
    final com.zhlearn.application.audio.PronunciationCandidate value;
    private TriState(State s, com.zhlearn.application.audio.PronunciationCandidate v) { this.state = s; this.value = v; }
    static TriState selected(com.zhlearn.application.audio.PronunciationCandidate v) { return new TriState(State.SELECTED, v); }
    static TriState skipped() { return new TriState(State.SKIPPED, null); }
    static TriState unavailable() { return new TriState(State.UNAVAILABLE, null); }
}
