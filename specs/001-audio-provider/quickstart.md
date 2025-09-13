# Quickstart — Audio Provider System

This feature enables pronunciation retrieval with multiple providers and a selection UI. Follow these steps to build, configure, and exercise the feature.

## Prerequisites
- Java 24 (preview), Maven, GraalVM if building native
- FFmpeg available on PATH (for transcode + loudness normalization)
- Forvo/TTS API keys if testing those providers
- An existing Anki media directory if testing copy integration

## Build
- Build all modules (enables preview features):
  - `mvn clean package`

## Run (JVM modules)
- Example (invoke word command and trigger audio flow):
  - `./zh-learn.sh word 学习`

## Configure
- Environment variables (secrets):
  - `FORVO_API_KEY=...`
  - `TENCENT_TTS_API_KEY=...`
  - `QWEN_API_KEY=...`
- App config (non-secrets): stored under `~/.zh-learn/config/`
  - `anki.media.dir=/path/to/Anki2/User 1/collection.media`
  - `audio.cache.dir=~/.zh-learn/cache/audio`
  - `forvo.known.users=["userA","userB"]`
  - `tts.voices={"qwen":{"zh-CN":"voice-id"},"tencent":{"zh-CN":"voice-id"}}`

## Expected Behavior (high level)
- Providers queried in priority: Anki → Forvo → Qwen TTS → Tencent TTS
- Auto-select for Anki/Forvo when enabled; TTS always manual
- Selection UI supports ↑/↓, Enter, Space, ESC/`s` per spec
- Audio normalized and stored as mp3; cache reused on repeat queries
- Selected audio copied to Anki media directory unless already present

## Native Image (optional)
- `cd zh-learn-cli && mvn native:compile-no-fork -Pnative`
- Run: `./target/zh-learn word 学习`

## Troubleshooting
- Missing FFmpeg: install and ensure `ffmpeg` is on PATH
- Anki media directory missing: set `anki.media.dir`; app fails fast if invalid
- Credentials missing: provider fails clearly; others continue per priority
