# Contract — Progress & Status Events

- Package: `com.zhlearn.domain.audio`
- Purpose: Provide structured status updates for UI (CLI or otherwise).

## Events
- `REQUEST_SENT(provider, term)`
- `RECEIVING(provider, term, bytesReceived, totalBytes?)`
- `NORMALIZING(term, step)` // e.g., transcode, loudnorm
- `CACHE_HIT(term, provider)` / `CACHE_MISS(term, provider)`
- `PLAYBACK_START(item)` / `PLAYBACK_STOP(item)` / `PLAYBACK_ERROR(item, error)`

## API
```java
interface StatusSink {
  void on(StatusEvent event);
}

sealed interface StatusEvent permits ... { /* as above */ }
```

---
