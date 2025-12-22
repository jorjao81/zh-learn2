Feature: MiniMax TTS end-to-end audio generation
  As a CLI user
  I want to generate audio using MiniMax TTS
  So that I can hear high-quality Chinese pronunciations

  @requires-minimax-api
  Scenario: Processing single word with MiniMax TTS generates valid MP3
    Given I have a Pleco export file with content:
      """
      学习	xue2xi2	to study; to learn
      """
    When I run parse-pleco with audio parameters "--explanation-provider=openrouter --definition-formatter-provider=openrouter --example-provider=openrouter --decomposition-provider=openrouter --model=google/gemini-2.5-flash-lite-preview-09-2025 --audio-selections=学习:minimax-tts:Wise_Woman"
    Then the exit code should be 0
    And the Anki export file should exist
    And the audio cache should contain files for word "学习" from provider "minimax-tts"
    And the Anki media directory should contain the selected audio for "学习"
    And the Anki export for "学习" should reference the correct audio file
    And the audio file for "学习" should be a valid MP3
