# Research Summary — Audio Provider System

This document records decisions, rationale, and alternatives for the Audio Provider System. All unknowns from the feature spec are resolved here following the Constitution.

## Decisions

1) Provider Pattern and Module Boundaries
- Decision: Define domain ports for AudioProvider, AudioOrchestrator, AudioTranscoder, AudioNormalizer, AudioPlayer, CacheStore. Implement providers in `infrastructure`, orchestration in `application`, CLI-only selection UI in `cli`.
- Rationale: Honors modular architecture; keeps UI separate; allows adding providers independently.
- Alternatives: Single-module implementation (rejected: violates repo architecture and reduces testability).

2) Audio Format and Transcoding
- Decision: Standardize on mp3 as the only persisted/cache format.
- Rationale: Requirement mandates mp3; broad compatibility and small footprint.
- Transcoding: Use FFmpeg (system dependency) invoked via a thin adapter port `AudioTranscoder`. Encode using `libmp3lame`, CBR 128 kbps, 44.1 kHz, mono when input is mono, preserve channels when stereo.
- Alternatives: TarsosDSP + MP3 SPI (rejected: MP3 encoding reliability varies; FFmpeg provides consistent, high-quality results across platforms).

3) Loudness Normalization
- Decision: Normalize via FFmpeg `loudnorm` filter to target Integrated Loudness -15 LUFS, LRA 11 LU, True Peak -1.5 dBTP. Apply after transcode or in a single pass during transcode.
- Rationale: Stable listening experience across sources; requirement mandates normalized volume.
- Alternatives: Simple RMS normalization (rejected: inconsistent perceived loudness).

4) Caching Strategy
- Decision: Cache normalized mp3 files under `~/.zh-learn/cache/audio/`. Key by content hash of source identifier + parameters; maintain metadata sidecar JSON for provenance (provider, voice/user, timestamps, term, hash).
- Rationale: Deterministic reuse and traceability; supports revalidation and duplicate avoidance.
- Alternatives: Filename-only cache without metadata (rejected: ambiguous provenance, harder debugging).

5) Filename Scheme
- Decision: `{term}-{provider}-{variant}-{hash8}.mp3` where:
  - `term` may include Chinese characters; also store a slug in metadata for cross-platform safety
  - `provider` = `anki|forvo|qwen|tencent`
  - `variant` = provider-specific (e.g., forvoUser, ttsVoice)
  - `hash8` = first 8 chars of SHA-256 over canonical key (provider+term+variant+lang)
- Rationale: Human-readable and encodes source; uniqueness via hash.
- Alternatives: Opaque UUID (rejected: debugging usability), fully ASCII-only (rejected: spec requires Chinese text support).

6) Provider Priority and Auto-selection
- Decision: Priority: Existing Anki → Forvo → Qwen TTS → Tencent TTS. Auto-select allowed for Anki and Forvo; forbidden for TTS. Forvo auto-select chooses first hit from configured ordered list of known-good users; otherwise manual selection.
- Rationale: Mirrors spec; respects quality expectations.
- Alternatives: Score-based auto-select across all providers (rejected: not required; TTS must be manual per spec).

7) Selection UX (CLI)
- Decision: Application exposes a selection session with items: label (provider/user/voice), duration, source, cache status. CLI renders list with keyboard navigation (↑/↓), auto-play on highlight, Enter to select, Space to replay, ESC/`s` to skip. Playback cancels previous clip on navigation.
- Rationale: Direct mapping to acceptance scenarios and decouples UI from providers.
- Alternatives: Prompt-based selection without continuous playback (rejected: poorer experience; deviates from spec).

8) Playback Implementation
- Decision: Define `AudioPlayer` port. Infrastructure attempts JavaSound+MP3 SPI; if not available, use external player via process (`ffplay -nodisp -autoexit` or macOS `afplay`). Failure to play is surfaced (fail-fast) but does not crash orchestration unless required by spec.
- Rationale: Keeps CLI capable across environments with minimal new dependencies.
- Alternatives: Hard-require Java MP3 SPI (rejected: environment variability), embed a full audio engine (rejected: unnecessary complexity).

9) Anki Integration
- Decision: Two paths:
  - Existing pronunciations: read from user-provided Anki export (index/notes mapping) and/or detect files already present in configured Anki media directory. Do not copy already-present files.
  - Selected pronunciations: copy normalized mp3 into Anki media directory using configured path; skip if exists with identical hash; warn otherwise.
- Rationale: Matches FR-049 to FR-054 and FR-056/057.
- Alternatives: Always copy chosen audio (rejected: duplicates and violates FR-050).

10) Configuration
- Decision: Read credentials from env or app config. Persist non-secrets under `~/.zh-learn/config/` (e.g., known-good Forvo users and TTS voice mapping). Secrets via environment variables.
- Rationale: Matches security guidance and FR-051/052.
- Alternatives: Inline credentials (rejected: security risk).

11) Timeouts and Retries
- Decision: Configurable per provider (default 15s). No silent retry; providers fail fast on timeout; orchestration continues to next provider.
- Rationale: Prevents UI stall; explicit failure preferred per Constitution.
- Alternatives: Background retry (rejected: complexity, not required).

12) Internationalization & Text Handling
- Decision: Treat input as UTF-8; allow Chinese characters in filenames; ensure safe filesystem operations; provide slug fallback in metadata.
- Rationale: FR-059 and general usability.

## Open Questions and Resolutions
- Target loudness spec (LUFS): RESOLVED to -15 LUFS; adjustable via config.
- MP3 bitrate: RESOLVED to 128 kbps CBR; configurable.
- Default Anki media path: RESOLVED to configurable; detect OS-specific defaults; fail if not found.

## Testing Strategy
- Cucumber acceptance tests mirror acceptance scenarios (selection UI, caching behavior, error cases, navigation bounds, playback retry, auto-selection).
- Integration tests for provider orchestrator with local fixtures; skip network by injecting fixture-backed providers; fail-fast if real credentials missing when running provider ITs.
- Unit tests for domain types and filename/caching rules.

## Risks
- External tool dependency (FFmpeg) availability. Mitigation: detect on startup; instruct in quickstart; provide clear error.
- Provider API changes. Mitigation: thin adapters; strong contract tests with fixtures.

## Alternatives Considered
- TarsosDSP-only pipeline (encoding + normalization): simpler install, but mp3 encoding quality/consistency concerns.
- Always-ASCII filenames: avoids filesystem quirks but violates requirement to handle Chinese text.
- Single-pass UI with auto-select only: faster UX but loses manual control mandated by spec.

## TUI Selection — Research & Decision

### Options Evaluated
- JLine 3 (Terminal, BindingReader, KeyMap, Display)
  - Pros: battle-tested terminal handling, cross-platform, raw mode & arrow keys, works with Picocli, lightweight for list UI, easy to test with injected streams
  - Cons: adds dependency; verify GraalVM native compatibility (usually fine)
- Lanterna 3 (rich TUI toolkit)
  - Pros: widgets/layouts out-of-the-box
  - Cons: heavier; more structure than needed; higher integration cost; potential native-image wrinkles
- Raw ANSI + System.in (manual `stty` toggling)
  - Pros: zero dependencies beyond Jansi for colors
  - Cons: fragile across terminals/OSes; key sequences vary; more code; poorer testability

### Decision
- Use JLine 3 for the selection UI. Implement a lightweight list selector using:
  - `Terminal` for raw input/output
  - `BindingReader` + `KeyMap` to map arrow keys, Enter, Space, ESC/`s`
  - `Display` + `AttributedString` to render list with highlighted row and a status line
  - Simple event loop with immediate auto-play on highlight; `AudioPlayer.stop()` before new `play()` to meet FR-033

### Implementation Sketch (outline)
```java
Terminal terminal = TerminalBuilder.builder().system(true).build();
BindingReader reader = new BindingReader(terminal.reader());
KeyMap<String> keys = new KeyMap<>();
// Bind arrows and controls (also bind terminal capabilities if available)
keys.bind("up", "\033[A"); keys.bind("down", "\033[B");
keys.bind("enter", "\r"); keys.bind("space", " "); keys.bind("esc", "\033"); keys.bind("skip", "s");

Display display = new Display(terminal, false);
int idx = 0; PlaybackHandle handle = null;
Runnable render = () -> {
  List<AttributedString> lines = new ArrayList<>();
  for (int i = 0; i < items.size(); i++) {
    String prefix = (i == idx) ? "> " : "  ";
    AttributedString as = (i == idx)
      ? AttributedString.fromAnsi("\u001b[7m" + prefix + items.get(i).label() + "\u001b[0m")
      : new AttributedString(prefix + items.get(i).label());
    lines.add(as);
  }
  lines.add(new AttributedString("↑/↓ navigate · Enter select · Space replay · s/ESC skip"));
  display.update(lines, 0);
};

render.run();
// auto-play first item
handle = player.playAsync(items.get(idx).playbackRef());

for (;;) {
  String op = reader.readBinding(keys);
  switch (op) {
    case "up" -> { idx = Math.max(0, idx - 1); if (handle != null) handle.stop(); render.run(); handle = player.playAsync(items.get(idx).playbackRef()); }
    case "down" -> { idx = Math.min(items.size()-1, idx + 1); if (handle != null) handle.stop(); render.run(); handle = player.playAsync(items.get(idx).playbackRef()); }
    case "space" -> { if (handle != null) handle.stop(); handle = player.playAsync(items.get(idx).playbackRef()); }
    case "enter" -> { if (handle != null) handle.stop(); return selection(items.get(idx)); }
    case "esc", "skip" -> { if (handle != null) handle.stop(); return skipped(); }
    default -> { /* ignore */ }
  }
}
```

Notes:
- Use `KeyMap.key(terminal, Capability.key_up)` etc. in addition to ANSI sequences for broader terminal support.
- Ensure terminal is restored on exit; consider try-with-resources for `Terminal` or capture/restore `Attributes`.
- Render uses reverse video for highlight; may use colors if desired via Jansi/AttributedStyle.
- Playback should be asynchronous with a `PlaybackHandle` to support interrupt/stop.

### Testing Approach
- Inject a `Terminal` with custom input/output streams; feed key sequences (e.g., `\033[B`, `\r`) and assert player invocations via a spy.
- Verify: navigation clamps at boundaries; Space triggers replay; Enter returns selected; ESC/`s` returns skipped; fast navigation results in `stop()` calls between plays.

### Dependency
- Add JLine 3 (verify latest stable 3.x at implementation time). For Picocli integration, direct use of JLine is sufficient; `picocli-shell-jline3` is not required.

### Native Image Considerations
- JLine generally works under GraalVM; validate during native build. If necessary, add reflection config for terminal detection. Keep a simple code path (no dynamic classloading) to minimize config.
