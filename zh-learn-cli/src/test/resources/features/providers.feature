Feature: Providers listing includes Qwen TTS audio provider
  As a CLI user
  I want to list available providers
  So that I can discover audio capabilities backed by Qwen TTS voices

  Scenario: Providers command lists Qwen TTS with all voices
    When I execute the CLI command "providers --detailed"
    Then the command output should include lines:
      | substring |
      | qwen-tts |
      | Cherry |
      | Serena |
      | Chelsie |
