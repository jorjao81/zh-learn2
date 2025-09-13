package com.zhlearn.application.audio;

import java.util.List;

public class SelectionSession {
    private final List<PronunciationCandidate> candidates;
    private final AudioPlayer player;
    private int index = 0;
    private PronunciationCandidate selected;

    public SelectionSession(List<PronunciationCandidate> candidates, AudioPlayer player) {
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException("candidates must not be empty");
        }
        this.candidates = List.copyOf(candidates);
        this.player = player;
        // Auto-play first item
        player.stop();
        player.play(current().file());
    }

    public int currentIndex() { return index; }
    public PronunciationCandidate current() { return candidates.get(index); }
    public PronunciationCandidate selected() { return selected; }
    public int size() { return candidates.size(); }
    public java.util.List<PronunciationCandidate> items() { return candidates; }
    public PronunciationCandidate candidateAt(int i) { return candidates.get(i); }

    public void pressDown() {
        if (index < candidates.size() - 1) {
            index++;
            player.stop();
            player.play(current().file());
        }
    }

    public void pressUp() {
        if (index > 0) {
            index--;
            player.stop();
            player.play(current().file());
        }
    }

    public void pressSpace() {
        player.stop();
        player.play(current().file());
    }

    public void pressEnter() {
        selected = current();
    }
}
