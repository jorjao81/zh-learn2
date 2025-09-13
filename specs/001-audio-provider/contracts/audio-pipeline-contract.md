# Contract — Audio Pipeline (Transcode + Normalize + Play)

- Packages: `com.zhlearn.domain.audio` (ports), `com.zhlearn.infrastructure.audio` (adapters)

## Ports
```java
interface AudioTranscoder {
  Path toMp3(Path input, Mp3Options opts) throws TranscodeException;
}

record Mp3Options(int bitrateKbps /* default 128 */, int sampleRateHz /* 44100 or preserve */, boolean monoPreferred) {}

interface AudioNormalizer {
  Path normalizeLoudness(Path inputMp3, LoudnessTarget target) throws NormalizeException;
}

record LoudnessTarget(double iLufs /* -16 */, double lra /* 11 */, double truePeakDb /* -1.5 */) {}

interface AudioPlayer {
  void play(Path file) throws PlaybackException;
  void stop();
}
```

## Semantics
- Transcoder accepts any common input from providers; produces mp3.
- Normalizer operates on mp3, applies `loudnorm` equivalent to target.
- Player must interrupt previous playback on new request.

## Default Implementations (Infra)
- `FFmpegTranscoderAdapter` (executes `ffmpeg`): `libmp3lame`, CBR 128 kbps.
- `FFmpegNormalizerAdapter` (executes `ffmpeg -af loudnorm`): -16 LUFS, LRA 11, -1.5 dBTP.
- `JavaSoundMp3Player` with fallback to `ffplay`/`afplay` via process.

---
