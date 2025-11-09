package com.zhlearn.application.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zhlearn.domain.model.Image;
import com.zhlearn.domain.model.ImageCandidate;

class ImageSelectionSessionTest {

    private List<ImageCandidate> candidates;

    @BeforeEach
    void setUp() {
        candidates =
                List.of(
                        createCandidate("http://example.com/1.jpg"),
                        createCandidate("http://example.com/2.jpg"),
                        createCandidate("http://example.com/3.jpg"));
    }

    @Test
    void shouldStartWithFirstItemSelected() {
        ImageSelectionSession session = new ImageSelectionSession(candidates);

        assertThat(session.currentIndex()).isEqualTo(0);
        assertThat(session.current()).isEqualTo(candidates.get(0));
    }

    @Test
    void shouldNavigateDown() {
        ImageSelectionSession session = new ImageSelectionSession(candidates);

        session.pressDown();

        assertThat(session.currentIndex()).isEqualTo(1);
        assertThat(session.current()).isEqualTo(candidates.get(1));
    }

    @Test
    void shouldNavigateUp() {
        ImageSelectionSession session = new ImageSelectionSession(candidates);
        session.pressDown();
        session.pressDown();

        session.pressUp();

        assertThat(session.currentIndex()).isEqualTo(1);
    }

    @Test
    void shouldNotNavigateBelowZero() {
        ImageSelectionSession session = new ImageSelectionSession(candidates);

        session.pressUp();

        assertThat(session.currentIndex()).isEqualTo(0);
    }

    @Test
    void shouldNotNavigateAboveMax() {
        ImageSelectionSession session = new ImageSelectionSession(candidates);

        session.pressDown();
        session.pressDown();
        session.pressDown(); // Should stop at index 2

        assertThat(session.currentIndex()).isEqualTo(2);
    }

    @Test
    void shouldToggleSelectionWithSpace() {
        ImageSelectionSession session = new ImageSelectionSession(candidates);

        assertThat(session.isSelected(0)).isFalse();

        session.pressSpace();
        assertThat(session.isSelected(0)).isTrue();

        session.pressSpace();
        assertThat(session.isSelected(0)).isFalse();
    }

    @Test
    void shouldTrackMultipleSelections() {
        ImageSelectionSession session = new ImageSelectionSession(candidates);

        session.pressSpace(); // Select index 0
        session.pressDown();
        session.pressSpace(); // Select index 1

        assertThat(session.isSelected(0)).isTrue();
        assertThat(session.isSelected(1)).isTrue();
        assertThat(session.isSelected(2)).isFalse();
        assertThat(session.selectedCount()).isEqualTo(2);
    }

    @Test
    void shouldReturnSelectedCandidatesInOrder() {
        ImageSelectionSession session = new ImageSelectionSession(candidates);

        session.pressDown();
        session.pressDown();
        session.pressSpace(); // Select index 2
        session.pressUp();
        session.pressSpace(); // Select index 1

        List<ImageCandidate> selected = session.getSelected();

        assertThat(selected).hasSize(2);
        assertThat(selected.get(0)).isEqualTo(candidates.get(1));
        assertThat(selected.get(1)).isEqualTo(candidates.get(2));
    }

    @Test
    void shouldAllowEmptySelection() {
        ImageSelectionSession session = new ImageSelectionSession(candidates);

        List<ImageCandidate> selected = session.getSelected();

        assertThat(selected).isEmpty();
        assertThat(session.selectedCount()).isEqualTo(0);
    }

    @Test
    void shouldProvideSize() {
        ImageSelectionSession session = new ImageSelectionSession(candidates);

        assertThat(session.size()).isEqualTo(3);
    }

    @Test
    void shouldProvideCandidateAtIndex() {
        ImageSelectionSession session = new ImageSelectionSession(candidates);

        assertThat(session.candidateAt(0)).isEqualTo(candidates.get(0));
        assertThat(session.candidateAt(1)).isEqualTo(candidates.get(1));
        assertThat(session.candidateAt(2)).isEqualTo(candidates.get(2));
    }

    @Test
    void shouldRejectNullCandidates() {
        assertThatThrownBy(() -> new ImageSelectionSession(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("candidates must not be empty");
    }

    @Test
    void shouldRejectEmptyCandidates() {
        assertThatThrownBy(() -> new ImageSelectionSession(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("candidates must not be empty");
    }

    private ImageCandidate createCandidate(String url) {
        Image metadata =
                new Image(
                        URI.create(url),
                        "http://thumb.example.com/thumb.jpg",
                        Optional.of("Test Image"),
                        800,
                        600,
                        "image/jpeg");
        return new ImageCandidate(metadata, Path.of("/tmp/test.jpg"));
    }
}
