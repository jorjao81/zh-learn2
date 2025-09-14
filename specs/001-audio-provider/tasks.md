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
11. Implement `AnkiProvider` (read-only): discover existing pronunciations from configured Anki export; no copying yet.
12. Make orchestrator prefer Anki first; feature-flag FixtureProvider off by default.

## 3) Forvo Provider (manual-only, no auto-select)
13. Contract tests: Forvo provider returns candidate(s) for queries when credentials are present; credentials missing → clear error; timeouts enforced.
14. Implement `ForvoProvider` (manual-only): fetch pronunciations from Forvo; do not implement auto-select or known-good users; validate provider priority (Anki → Forvo).
15. Acceptance: multi-provider selection shows both Anki and Forvo entries; manual selection works via the existing selection UI.

## 4) Basic Caching (no normalization)
16. Contract tests: cache hit returns previously produced mp3 without re-fetch.
17. Implement `FileSystemCacheStore` at `~/.zh-learn/cache/audio` with filename scheme; write minimal metadata sidecar.
18. Update orchestrator to consult cache before provider fetch and to store selections.

## 5) Copy to Anki (on selection)
19. Contract/integration tests: selected file is copied to Anki media dir; skip if exists; crash if dir missing.
20. Implement copy-on-select logic with existence check and warnings per spec.

## 6) Loudness Normalization + MP3 Transcode
21. Integration tests: normalized output meets pipeline expectations; fallback when ffmpeg missing is to fail fast with clear error.
22. Implement `FFmpegTranscoderAdapter` and `FFmpegNormalizerAdapter`; integrate into pipeline post-fetch/pre-cache.

## 7) Forvo Auto-Select & Known-Good Users
23. Contract tests: Forvo auto-select picks from a configured "known-good users" list; credentials required; timeouts enforced.
24. Implement auto-select behavior and configuration for known-good users; update orchestrator to apply auto-select per provider.

## 8) TTS Providers (manual only)
25. Contract tests: TTS never auto-selects; voices pulled from config; credentials required.
26. Implement `QwenTtsProvider` and `TencentTtsProvider` minimal adapters; manual-only path.

## 9) Expanded Acceptance Coverage & Status/Progress
27. Add remaining Cucumber scenarios from spec (navigation bounds, playback retry, error display, skip behavior, cache reuse).
28. Implement structured status events and progress bar in CLI; hook orchestrator events.

## 10) Config & Hardening
29. Implement config adapters (env + `~/.zh-learn/config/` mapping) and validation.
30. Service loader registration for providers; CLI flags for enabling/disabling providers.
31. Native build sanity check and end-to-end manual verification.
