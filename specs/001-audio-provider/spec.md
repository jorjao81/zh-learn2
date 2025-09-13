# Feature Specification: Audio Provider System

**Feature Branch**: `001-audio-provider`
**Created**: 2025-09-13
**Status**: Draft
**Input**: User description: "add a feature to obtain pronunciation for chinese words. It should support multiple providers, like forvo, existing pronunciations in anki and tts generators (like tencent and alibaba). It should allow combining different providers, it should allow functions to auto-select pronunciations but it should also provide a way for the user to select their preferred pronunciation after listening to it. It should keep the actual interface for selection (e.g, the terminal interface) separate from the audio provider spec, so that eventually we could implement a web based interface, etc. All audio should be normalized to a common volume and cached to avoid re-downloading or re generating. The filenames should reflect informatiom about their source. The format should be mp3."

## Execution Flow (main)
```
1. Parse user description from Input
   � If empty: ERROR "No feature description provided"
2. Extract key concepts from description
   � Identify: actors, actions, data, constraints
3. For each unclear aspect:
   � Mark with [NEEDS CLARIFICATION: specific question]
4. Fill User Scenarios & Testing section
   � If no clear user flow: ERROR "Cannot determine user scenarios"
5. Generate Functional Requirements
   � Each requirement must be testable
   � Mark ambiguous requirements
6. Identify Key Entities (if data involved)
7. Run Review Checklist
   � If any [NEEDS CLARIFICATION]: WARN "Spec has uncertainties"
   � If implementation details found: ERROR "Remove tech details"
8. Return: SUCCESS (spec ready for planning)
```

---

## � Quick Guidelines
-  Focus on WHAT users need and WHY
- L Avoid HOW to implement (no tech stack, APIs, code structure)
- =e Written for business stakeholders, not developers

### Section Requirements
- **Mandatory sections**: Must be completed for every feature
- **Optional sections**: Include only when relevant to the feature
- When a section doesn't apply, remove it entirely (don't leave as "N/A")

### For AI Generation
When creating this spec from a user prompt:
1. **Mark all ambiguities**: Use [NEEDS CLARIFICATION: specific question] for any assumption you'd need to make
2. **Don't guess**: If the prompt doesn't specify something (e.g., "login system" without auth method), mark it
3. **Think like a tester**: Every vague requirement should fail the "testable and unambiguous" checklist item
4. **Common underspecified areas**:
   - User types and permissions
   - Data retention/deletion policies
   - Performance targets and scale
   - Error handling behaviors
   - Integration requirements
   - Security/compliance needs

---

## User Scenarios & Testing *(mandatory)*

### Primary User Story
A Chinese language learner using the zh-learn application (e.g., parse-pleco functionality) wants to hear accurate pronunciation of Chinese words and characters to improve their speaking and listening skills. They need access to multiple audio sources to compare pronunciations and select the most suitable one for their learning needs.

### Acceptance Scenarios
1. **Given** a Chinese word or phrase in zh-learn application, **When** user triggers AudioProvider functionality, **Then** system displays pronunciation options in a navigable list with provider identification
2. **Given** multiple pronunciation options are displayed, **When** user navigates with arrow keys, **Then** highlighted pronunciation automatically plays and user can hear each option
3. **Given** a pronunciation is highlighted, **When** user presses Enter, **Then** system selects that pronunciation for the word
4. **Given** a pronunciation is highlighted, **When** user presses Space, **Then** system replays the currently highlighted pronunciation
5. **Given** pronunciation options are available, **When** user presses 's' or ESC, **Then** system skips selection and continues without audio
6. **Given** user is at the first pronunciation option, **When** user presses up arrow, **Then** navigation stays at the first option without moving
7. **Given** user is at the last pronunciation option, **When** user presses down arrow, **Then** navigation stays at the last option without moving
8. **Given** audio playback fails for a pronunciation, **When** error occurs, **Then** system displays error message and remains at current selection allowing retry with Space
9. **Given** no pronunciation sources are available for a word, **When** user requests pronunciation, **Then** system continues without audio and does not block the workflow
10. **Given** a pronunciation has been previously requested, **When** the same word is requested again, **Then** system provides cached audio without re-downloading or regenerating
11. **Given** pronunciations from different sources, **When** audio is played, **Then** all audio files have normalized volume levels for consistent listening experience
12. **Given** auto-selection is enabled for a pronunciation provider, **When** that provider has suitable audio available, **Then** system automatically selects pronunciation without showing selection interface
13. **Given** Forvo provider with auto-selection enabled, **When** any pronunciation from a known good user is available, **Then** system automatically selects that pronunciation, otherwise shows selection interface
14. **Given** multiple providers have auto-selectable pronunciations, **When** user requests pronunciation, **Then** system selects from highest priority provider (Existing > Forvo > Qwen TTS > Tencent TTS)
15. **Given** user is rapidly navigating between pronunciations, **When** user moves to next pronunciation before current audio completes, **Then** system interrupts previous audio and immediately starts playing new pronunciation
16. **Given** pronunciation is downloaded or generated for first time, **When** audio becomes available, **Then** system caches it in the application's user data storage regardless of user selection
17. **Given** an Anki pronunciation export is available, **When** user requests pronunciation for a word, **Then** system includes Anki pronunciation as highest priority source
18. **Given** external service requires API authentication, **When** system accesses Forvo or TTS providers, **Then** system uses credentials from configured environment or application settings
19. **Given** no TTS voice configuration is present, **When** system first runs, **Then** default Qwen TTS voices (Chelsie, Cherry, Ethan, Serena) and Tencent TTS voices (zhiwei, zhifang, zhiyou, zhiyu, zhiling) are provided
20. **Given** TTS voice configuration exists, **When** system runs, **Then** system uses the existing configuration instead of defaults
21. **Given** auto-selection fails for all providers, **When** user requests pronunciation, **Then** system displays all available pronunciations for manual selection
22. **Given** external service returns an audio format different from the system's common format, **When** system caches the audio, **Then** system converts audio to the common format before storing
23. **Given** external service request exceeds the defined timeout threshold, **When** timeout occurs, **Then** system treats request as failed and continues with other providers
24. **Given** user selects a pronunciation from Forvo, Qwen TTS, or Tencent TTS, **When** selection is made, **Then** system copies the pronunciation file to the configured Anki media directory
25. **Given** user selects an existing Anki pronunciation, **When** selection is made, **Then** system does not copy the file as it already exists in the Anki media directory
26. **Given** TTS providers have pronunciations available, **When** user requests pronunciation, **Then** system always shows TTS options for manual selection without auto-selecting
27. **Given** selected pronunciation file already exists in Anki media directory, **When** copying occurs, **Then** system skips copying and prints warning message
28. **Given** the application's user data storage doesn't exist, **When** system first runs, **Then** system creates the storage automatically
29. **Given** Anki media directory doesn't exist or write permissions denied, **When** system attempts to copy files, **Then** system crashes with error message
30. **Given** user is downloading pronunciations, **When** operations are in progress, **Then** system displays status bar showing current operation and progress percentage
31. **Given** Chinese characters are processed, **When** creating filenames or API requests, **Then** system handles Chinese characters correctly and consistently
32. **Given** the user interface is terminal-based, **When** a web interface is implemented in the future, **Then** the same pronunciation data and functionality is available through the new interface

### Edge Cases
- System continues normally when no pronunciation sources are available for a specific word
- How does system handle network failures when accessing external pronunciation sources?
- What happens when cached pronunciation files become corrupted or unavailable?
- How does system behave when multiple providers return identical pronunciation data?
- System must allow users to skip pronunciation selection even when options are available
- Navigation is bounded - user cannot move beyond first or last pronunciation option
- Audio playback failures display error message but keep user at current selection for retry
- System crashes when required API keys are missing from configuration
- System creates the application's user data storage if it doesn't exist when first caching audio
- Audio interruption occurs immediately when user navigates, with no delay or fade-out
- Forvo known good users list is empty by default and must be manually configured
- External service requests timeout after a defined threshold
- All cached audio is stored in the system's common audio format regardless of source format
- Cache entries include information to identify the word, provider, and voice to ensure uniqueness (via naming and/or metadata)
- Selected pronunciations are copied to Anki media directory for integration, except existing Anki files
- TTS providers require manual selection and do not auto-select pronunciations
- System crashes on missing Anki directory or permission errors, creates the application's user data storage if missing
- File conflicts in Anki media directory are skipped with warning messages
- Progress feedback shows detailed status during pronunciation operations
- All text processing correctly handles Chinese characters for filenames and requests

## Requirements *(mandatory)*

### Functional Requirements
- **FR-001**: System MUST support multiple pronunciation providers including Forvo, Anki collections, and TTS generators
- **FR-002**: System MUST allow users to listen to pronunciation options from different providers before selecting
- **FR-003**: System MUST provide automatic pronunciation selection when user doesn't manually choose
- **FR-004**: System MUST cache all pronunciation audio to avoid re-downloading or regenerating identical content
- **FR-005**: System MUST normalize audio volume across all pronunciation sources to consistent levels
- **FR-006**: System MUST persist cached audio in a single, widely compatible audio format consistent across providers
- **FR-007**: System MUST include source information in audio filename for traceability
- **FR-008**: System MUST separate pronunciation selection interface from pronunciation provider functionality
- **FR-009**: System MUST allow combining results from multiple pronunciation providers for the same word
- **FR-010**: Users MUST be able to select their preferred pronunciation after listening to available options OR skip selection entirely
- **FR-011**: System MUST continue normal operation when no pronunciation is found or selected for a word
- **FR-012**: System MUST allow users to skip pronunciation selection even when pronunciation options are available
- **FR-013**: CLI interface MUST support arrow key navigation to highlight different pronunciation options
- **FR-014**: CLI interface MUST automatically play highlighted pronunciation when user navigates to it
- **FR-015**: CLI interface MUST select highlighted pronunciation when user presses Enter key
- **FR-016**: CLI interface MUST replay current pronunciation when user presses Space key
- **FR-017**: CLI interface MUST skip selection when user presses 's' or ESC key
- **FR-018**: CLI interface MUST prevent navigation beyond first and last pronunciation options
- **FR-019**: System MUST display error messages for audio playback failures while maintaining current selection for retry
- **FR-020**: Audio quality standards are not specified and implementation may use reasonable defaults
- **FR-021**: System MUST persist cached audio in user-specific application data storage and retain it across sessions
- **FR-027**: System MUST cache pronunciations immediately upon first download or generation, regardless of user selection
- **FR-028**: System MUST maintain cached pronunciations even when user skips selection
- **FR-029**: System MUST integrate as an AudioProvider within existing zh-learn functionality (e.g., parse-pleco)
- **FR-030**: System MUST provide specific function trigger for pronunciation requests within zh-learn application
- **FR-031**: System MUST allow Forvo known good users to be configured as an ordered list in user configuration
- **FR-041**: When no TTS voice configuration exists, the system MUST provide default voices: Qwen TTS (Chelsie, Cherry, Ethan, Serena) and Tencent TTS (zhiwei, zhifang, zhiyou, zhiyu, zhiling)
- **FR-042**: System MUST use an existing TTS voice configuration when present
- **FR-043**: Configuration MUST be stored persistently in a user-editable form; storage format and location are implementation details
- **FR-044**: Cached audio entries MUST follow a consistent, human-readable naming scheme that encodes source and guarantees uniqueness (exact pattern is implementation detail)
- **FR-045**: System MUST support using existing Anki pronunciations from a user-provided Anki export
- **FR-046**: When no auto-selection provider works, system MUST show all available pronunciations for manual user selection
- **FR-047**: System MUST convert provider audio to the chosen common audio format before caching
- **FR-048**: System MUST enforce timeouts for external service requests; the timeout threshold MUST be configurable
- **FR-049**: System MUST copy selected pronunciation files to the configured Anki media directory
- **FR-050**: System MUST NOT copy existing Anki pronunciations to the Anki media directory as they are already present there
- **FR-051**: Forvo known good users MUST be stored in user configuration as an ordered, persistently configurable list
- **FR-052**: TTS voice configuration MUST be stored in user configuration as persistently configurable provider-to-voice mappings
- **FR-053**: TTS providers (Qwen, Tencent) MUST NOT auto-select and always require manual user selection
- **FR-054**: System MUST skip file copying when target file exists in Anki media directory and print warning message
- **FR-055**: System MUST ensure the application's user data storage is created when needed
- **FR-056**: System MUST crash if Anki media directory doesn't exist or write permissions are denied
- **FR-057**: Anki media directory path MUST be configurable; OS-specific defaults MAY be provided and documented separately
- **FR-058**: System MUST display status bar with progress feedback: request sent, receiving data with percentage, normalizing audio, etc.
- **FR-059**: All Chinese text processing MUST correctly handle Chinese characters for filenames and API requests
- **FR-032**: System MUST access credentials for external services via environment variables or application configuration (exact mechanism is an implementation detail)
- **FR-033**: System MUST interrupt previous audio playback when user rapidly navigates to next pronunciation
- **FR-034**: System MUST integrate with existing Anki AudioProvider functionality for existing pronunciations
- **FR-035**: System MUST fail clearly when required credentials are missing from configuration rather than failing silently
- **FR-036**: System MUST prioritize pronunciation sources in this order: Existing pronunciation, Forvo, Qwen TTS, Tencent TTS
- **FR-037**: System MUST support Simplified Hanzi text format only
- **FR-038**: Forvo and Anki providers MUST support auto-selection mode to bypass user interaction, TTS providers do not auto-select
- **FR-039**: Forvo provider MUST base auto-selection on a configured list of known good users
- **FR-040**: When auto-selection is enabled, system MUST automatically choose pronunciation without displaying selection interface

### Key Entities *(include if feature involves data)*
- **Chinese Word/Phrase**: Text input requiring pronunciation, includes metadata about character type and source
- **Pronunciation Source**: Provider of audio content (Forvo, Anki, TTS services), includes source identification and capabilities
- **Audio Cache Entry**: Stored pronunciation file with metadata including source, creation date, and location in the application's user data storage, using a consistent naming scheme that encodes source and ensures uniqueness
- **User Preference**: Selected pronunciation choice for specific words, linked to user and word combination
- **Audio File**: Pronunciation audio stored in a single common format with normalized volume and source-descriptive naming
- **Forvo Known Good Users Config**: User configuration containing an ordered list of trusted Forvo user identifiers
- **TTS Users Config**: User configuration containing provider-to-voice mappings for Qwen and Tencent TTS services
- **Anki Export**: A user-provided export containing existing pronunciations from Anki
- **Anki Media Directory**: Configurable path for where selected pronunciations are copied for Anki integration (OS-specific defaults may apply)
- **Status Progress Bar**: User interface element displaying current operation status and progress percentage for pronunciation operations
- **API Configuration**: External service authentication keys supplied via environment variables or application configuration

---

## Review & Acceptance Checklist
*GATE: Automated checks run during main() execution*

### Content Quality
- [ ] No implementation details (languages, frameworks, APIs)
- [ ] Focused on user value and business needs
- [ ] Written for non-technical stakeholders
- [ ] All mandatory sections completed

### Requirement Completeness
- [ ] No [NEEDS CLARIFICATION] markers remain
- [ ] Requirements are testable and unambiguous
- [ ] Success criteria are measurable
- [ ] Scope is clearly bounded
- [ ] Dependencies and assumptions identified

---

## Execution Status
*Updated by main() during processing*

- [x] User description parsed
- [x] Key concepts extracted
- [x] Ambiguities marked
- [x] User scenarios defined
- [x] Requirements generated
- [x] Entities identified
- [ ] Review checklist passed

---
