# Contract — Caching

- Package: `com.zhlearn.domain.audio`
- Purpose: Reuse normalized mp3 assets across runs; encode provenance.

## Port
```java
interface AudioCacheStore {
  Optional<AudioCacheEntry> find(CacheKey key);
  AudioCacheEntry put(CacheKey key, Path normalizedMp3, CacheMetadata meta) throws IOException;
}

record CacheKey(String term, ProviderId provider, String variant, String lang /* zh-Hans */, int bitrateKbps, double iLufs) {}

record CacheMetadata(String hash8, Instant createdAt, Duration duration, Map<String, String> sourceMeta) {}
```

## Semantics
- `CacheKey` deterministic over inputs; `hash8` derived from SHA-256 of canonical key.
- `find` does not create; `put` writes file and sidecar metadata atomically (temp file → move).
- Must handle Chinese characters in filenames; store slug in metadata for portability.

## Paths
- Base: `~/.zh-learn/cache/audio/`
- File: `{term}.{provider}.{variant}.{hash8}.mp3`
- Sidecar: same stem with `.json`

---
