# Tasks — Audio Provider System (MVP-first)

Goal: deliver a runnable end-to-end slice as early as possible, then iterate. Each slice remains test-first but minimal.

## 0) Branch & Minimal Scaffolding
1. Ensure on branch `001-audio-provider` [done]
2. Add a tiny sample mp3 fixture under test resources for dev E2E (e.g., `fixtures/audio/sample.mp3`).

## 1) MVP E2E (Single Local Fixture Provider, no cache/normalize)
3. Acceptance test (Cucumber): one scenario “play and select from single provider” with ↑/↓, Enter, Space, ESC.
4. Implement minimal Selection UI in CLI to satisfy the scenario (keyboard + auto-play on highlight, cancel previous).
5. Implement `AudioPlayer` with simple external fallback (`afplay`/`ffplay`), fail-fast if neither present.
6. Implement `FixtureProvider` (infrastructure) that returns the local sample.mp3 for any term.
7. Implement minimal `AudioOrchestrator` (application) that queries providers in order and returns candidates.
8. Wire CLI `word <term>` to invoke orchestrator and selection UI.
9. Manual check: `./zh-learn.sh word 学习` plays the sample and allows selection.

## 2) Replace Fixture with Real Anki Provider (existing pronunciations only)
10. Acceptance test: Anki provider returns existing audio when present; skip gracefully when absent.
11. Implement `AnkiProvider` (read-only): discover existing pronunciations from configured Anki media dir; no copying yet.
12. Make orchestrator prefer Anki first; feature-flag FixtureProvider off by default.

## 3) Basic Caching (no normalization)
13. Contract tests: cache hit returns previously produced mp3 without re-fetch.
14. Implement `FileSystemCacheStore` at `~/.zh-learn/cache/audio` with filename scheme; write minimal metadata sidecar.
15. Update orchestrator to consult cache before provider fetch and to store selections.

## 4) Copy to Anki (on selection)
16. Contract/integration tests: selected file is copied to Anki media dir; skip if exists; crash if dir missing.
17. Implement copy-on-select logic with existence check and warnings per spec.

## 5) Loudness Normalization + MP3 Transcode
18. Integration tests: normalized output meets pipeline expectations; fallback when ffmpeg missing is to fail fast with clear error.
19. Implement `FFmpegTranscoderAdapter` and `FFmpegNormalizerAdapter`; integrate into pipeline post-fetch/pre-cache.

## 6) Forvo Provider (auto-select allowed)
20. Contract tests: Forvo auto-select uses known-good users; timeouts enforced; credentials missing → fail clearly.
21. Implement `ForvoProvider` with auto-select; add config for known-good users; update orchestrator priority (Anki → Forvo → TTS).

## 7) TTS Providers (manual only)
22. Contract tests: TTS never auto-selects; voices pulled from config; credentials required.
23. Implement `QwenTtsProvider` and `TencentTtsProvider` minimal adapters; manual-only path.

## 8) Expanded Acceptance Coverage & Status/Progress
24. Add remaining Cucumber scenarios from spec (navigation bounds, playback retry, error display, skip behavior, cache reuse).
25. Implement structured status events and progress bar in CLI; hook orchestrator events.

## 9) Config & Hardening
26. Implement config adapters (env + `~/.zh-learn/config/` mapping) and validation.
27. Service loader registration for providers; CLI flags for enabling/disabling providers.
28. Native build sanity check (optional) and end-to-end manual verification.
