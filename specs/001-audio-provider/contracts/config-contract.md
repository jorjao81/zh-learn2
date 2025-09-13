# Contract — Configuration

## Required
- `anki.media.dir` (Path): Must exist and be writable. Crash if missing.
- `audio.cache.dir` (Path): Created if missing. Default: `~/.zh-learn/cache/audio`.

## Providers
- Forvo: `FORVO_API_KEY` (env) or `forvo.api.key` (config)
- Tencent TTS: `TENCENT_TTS_API_KEY` / secret vars or config equivalent
- Qwen TTS: `QWEN_API_KEY` / secret vars or config equivalent

## Behavioural
- `audio.bitrate.kbps` (int, default 128)
- `audio.loudness.lufs` (double, default -16)
- `provider.timeout.ms` (int, default 5000)
- `forvo.known.users` (ordered list in config)
- `tts.voices` (map: provider→locale→voice)

## Storage
- Persist non-secret preferences under `~/.zh-learn/config/`.
- Secrets must be read from environment or secure store adapters.

---
