Feature: Audio selection from a single provider
  As a Chinese language learner
  I want to hear pronunciations and select one
  So that I can choose the best option

  Scenario: Play and select from single provider using keys
    Given a fixture audio provider with sample audio
    And the terminal selection UI is ready
    When I request pronunciation for the term "学习"
    Then the first item should auto play
    When I press DOWN
    Then the second item should auto play
    When I press SPACE
    Then the current item should replay
    When I press ENTER
    Then the selection should be submitted
    And the selected pronunciation should be "sample.mp3"
