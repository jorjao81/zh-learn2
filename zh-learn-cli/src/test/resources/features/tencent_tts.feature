Feature: Tencent TTS audio provider integration
  As a CLI user
  I want to use Tencent TTS for audio pronunciation
  So that I can hear Chinese words spoken with Tencent voices

  Scenario: Providers command lists Tencent TTS with voice names
    When I execute the CLI command "providers --detailed"
    Then the command output should include lines:
      | substring |
      | tencent-tts |
      | zhiwei |
      | zhiling |

  Scenario: Tencent TTS provider appears in AI provider list
    When I execute the CLI command "providers --type AI"
    Then the command output should include lines:
      | substring |
      | tencent-tts |
      | Tencent text-to-speech |

  Scenario: Tencent TTS provider shows audio capability
    When I execute the CLI command "providers --detailed"
    Then the command output should include lines:
      | substring |
      | tencent-tts |
      | ✓ Audio |