package com.zhlearn.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;

import com.zhlearn.application.audio.AudioPlayer;
import com.zhlearn.application.audio.PronunciationCandidate;
import com.zhlearn.application.audio.SelectionSession;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class AudioSelectionStepDefinitions {

    private List<PronunciationCandidate> candidates;
    private TestAudioPlayer player;
    private SelectionSession session;

    @Given("a fixture audio provider with sample audio")
    public void a_fixture_audio_provider_with_sample_audio() {
        Path file = Path.of("src/test/resources/fixtures/audio/sample.mp3").toAbsolutePath();
        PronunciationCandidate c1 = new PronunciationCandidate("fixture-1", file);
        PronunciationCandidate c2 = new PronunciationCandidate("fixture-2", file);
        candidates = List.of(c1, c2);
    }

    @Given("the terminal selection UI is ready")
    public void the_terminal_selection_ui_is_ready() {
        player = new TestAudioPlayer();
        session = new SelectionSession(candidates, player);
    }

    @When("I request pronunciation for the term {string}")
    public void i_request_pronunciation_for_the_term(String term) {
        // No-op for fixture; session already created and auto-played first item
    }

    @Then("the first item should auto play")
    public void the_first_item_should_auto_play() {
        assertThat(player.playCount).isEqualTo(1);
        assertThat(player.lastPlayed).isNotNull();
        assertThat(session.currentIndex()).isEqualTo(0);
    }

    @When("I press DOWN")
    public void i_press_down() {
        session.pressDown();
    }

    @Then("the second item should auto play")
    public void the_second_item_should_auto_play() {
        assertThat(session.currentIndex()).isEqualTo(1);
        assertThat(player.playCount).isEqualTo(2);
        assertThat(player.lastPlayed).isNotNull();
    }

    @When("I press SPACE")
    public void i_press_space() {
        session.pressSpace();
    }

    @Then("the current item should replay")
    public void the_current_item_should_replay() {
        assertThat(player.playCount).isEqualTo(3);
        assertThat(session.currentIndex()).isEqualTo(1);
    }

    @When("I press ENTER")
    public void i_press_enter() {
        session.pressEnter();
    }

    @Then("the selection should be submitted")
    public void the_selection_should_be_submitted() {
        assertThat(session.selected()).isNotNull();
    }

    @Then("the selected pronunciation should be {string}")
    public void the_selected_pronunciation_should_be(String expected) {
        assertThat(session.selected().file().getFileName().toString()).isEqualTo(expected);
    }

    private static class TestAudioPlayer implements AudioPlayer {
        int playCount = 0;
        Path lastPlayed;

        @SuppressWarnings("unused") // tracked for potential future test assertions
        int stopCount = 0;

        @Override
        public void play(Path file) {
            playCount++;
            lastPlayed = file;
        }

        @Override
        public void stop() {
            stopCount++;
        }
    }
}
