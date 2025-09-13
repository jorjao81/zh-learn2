# Contract — Audio Providers (Domain Port)

- Package: `com.zhlearn.domain.audio`
- Purpose: Retrieve pronunciation candidates for a Chinese term from a specific source.

## Interfaces

```java
interface AudioProvider {
  ProviderId id();
  boolean supportsAutoSelect();

  List<PronunciationCandidate> findCandidates(ChineseTerm term, ProviderContext ctx)
      throws ProviderException, TimeoutException;

  Optional<PronunciationCandidate> autoSelect(ChineseTerm term, ProviderContext ctx)
      throws ProviderException, TimeoutException;
}

record ProviderId(String name, Optional<String> version) {}

record ChineseTerm(String text, String language /* zh-Hans */, Optional<String> pinyin) {}

record PronunciationCandidate(
  ProviderId provider,
  ChineseTerm term,
  String variant, // forvoUser | ttsVoice | ankiSource
  URI source,     // may be remote or file
  Optional<Long> durationMs,
  boolean cached
) {}

record ProviderContext(Duration timeout, Map<String,String> config) {}
```

## Semantics
- `findCandidates` may return zero or more items; never throws for “not found”.
- `autoSelect` returns a single best candidate iff `supportsAutoSelect()` is true; must not prompt user.
- Errors (auth, quota, invalid config) → `ProviderException` (unchecked).
- Timeouts → `TimeoutException` (checked) and are handled by the orchestrator.

## Constraints
- Do not perform UI in providers.
- Do not normalize or transcode here; providers return raw sources (mp3 preferred if available).
- All Chinese text must be handled as UTF-8.

---
