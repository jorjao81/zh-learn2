package com.zhlearn.cli;

import com.zhlearn.infrastructure.anki.AnkiNote;
import com.zhlearn.infrastructure.anki.AnkiNoteParser;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Command(
    name = "parse-anki",
    description = "Parse and display Anki collection notes from a file"
)
public class ParseAnkiCommand implements Runnable {
    
    @Parameters(index = "0", description = "Path to the Anki cards file")
    private String filePath;
    
    @Override
    public void run() {
        try {
            Path path = Paths.get(filePath);
            AnkiNoteParser parser = new AnkiNoteParser();
            List<AnkiNote> cards = parser.parseFile(path);
            
            System.out.println("Successfully parsed " + cards.size() + " Anki cards from: " + filePath);
            System.out.println();
            
            // Display the first few cards as examples
            int displayLimit = Math.min(5, cards.size());
            for (int i = 0; i < displayLimit; i++) {
                AnkiNote card = cards.get(i);
                displayAnkiCard(card, i + 1);
            }
            
            if (cards.size() > displayLimit) {
                System.out.println("... and " + (cards.size() - displayLimit) + " more cards.");
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing Anki file: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private void displayAnkiCard(AnkiNote card, int cardNumber) {
        System.out.println("Card " + cardNumber + ":");
        System.out.println("  NoteType: " + card.noteType());
        System.out.println("  Pinyin: " + card.pinyin());
        System.out.println("  Simplified: " + card.simplified());
        System.out.println("  Definition: " + truncate(card.definition(), 100));
        System.out.println("  Examples: " + truncate(card.examples(), 100));
        System.out.println("  Etymology: " + truncate(card.etymology(), 100));
        System.out.println();
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
