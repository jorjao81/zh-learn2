package com.zhlearn.application.image;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.zhlearn.domain.model.ImageCandidate;

/** Manages interactive multi-select session for image candidates. */
public class ImageSelectionSession {
    private final List<ImageCandidate> candidates;
    private int currentIndex = 0;
    private final Set<Integer> selectedIndices = new HashSet<>();

    public ImageSelectionSession(List<ImageCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException("candidates must not be empty");
        }
        this.candidates = List.copyOf(candidates);
    }

    public int currentIndex() {
        return currentIndex;
    }

    public ImageCandidate current() {
        return candidates.get(currentIndex);
    }

    public int size() {
        return candidates.size();
    }

    public ImageCandidate candidateAt(int index) {
        return candidates.get(index);
    }

    public boolean isSelected(int index) {
        return selectedIndices.contains(index);
    }

    public int selectedCount() {
        return selectedIndices.size();
    }

    public List<ImageCandidate> getSelected() {
        return selectedIndices.stream().sorted().map(candidates::get).toList();
    }

    public void pressDown() {
        if (currentIndex < candidates.size() - 1) {
            currentIndex++;
        }
    }

    public void pressUp() {
        if (currentIndex > 0) {
            currentIndex--;
        }
    }

    public void pressSpace() {
        if (selectedIndices.contains(currentIndex)) {
            selectedIndices.remove(currentIndex);
        } else {
            selectedIndices.add(currentIndex);
        }
    }
}
