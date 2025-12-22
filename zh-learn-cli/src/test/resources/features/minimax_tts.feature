Feature: MiniMax TTS audio provider integration
  As a CLI user
  I want to use MiniMax TTS for audio pronunciation
  So that I can hear Chinese words spoken with high-quality MiniMax voices

  Scenario: Providers command lists MiniMax TTS with voice names
    When I execute the CLI command "providers --detailed"
    Then the command output should include lines:
      | substring |
      | minimax-tts |
      | Wise_Woman |
      | Deep_Voice_Man |
      | Lovely_Girl |
      | Young_Knight |
      | Calm_Woman |

  Scenario: MiniMax TTS provider appears in AI provider list
    When I execute the CLI command "providers --type AI"
    Then the command output should include lines:
      | substring |
      | minimax-tts |
      | MiniMax Speech-2.6-HD |

  Scenario: MiniMax TTS provider shows audio capability
    When I execute the CLI command "providers --detailed"
    Then the command output should include lines:
      | substring |
      | minimax-tts |
      | Audio |
