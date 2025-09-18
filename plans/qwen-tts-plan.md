# Qwen Text-to-Speech Provider Plan

## 1. Goal & Context
- Deliver a new audio provider that generates Mandarin pronunciation audio using Alibaba's Qwen-TTS service.
- Always synthesize Cherry, Serena, and Chelsie voices per request so learners can pick between consistent female voice variants.
- Preserve module boundaries (domain API unchanged, infrastructure owns network logic, application wires in provider, CLI surfaces selection) while complying with Constitution v1.0.0 and AGENTS.md.

## 2. Scope
- **In scope:** provider implementation, test-first workflow, ServiceLoader registration, CLI discoverability, documentation, and audio caching.
- **Out of scope:** additional voices, runtime configuration flags, streaming playback, or alternate caching strategies beyond existing helpers.

## 3. Functional Requirements
1. Given Hanzi + Pinyin, call Qwen-TTS to synthesize speech for Cherry, Serena, and Chelsie voices, normalize each result to MP3, and return all paths via `getPronunciations`.
2. Keep `getPronunciation` returning the first (Cherry) result for compatibility with existing single-voice consumers.
3. Respect fail-fast philosophy: missing API key, HTTP errors, or malformed responses must throw rather than returning empty optionals.
4. Reuse cached audio when the same (word, voice, text) combination is requested again.
5. Provider metadata must expose the voice list through the CLI `providers` command and documentation.

## 4. Non-Functional Requirements
- Depend on the latest DashScope Java SDK (verify current version before implementation).
- Require a single API key input (`DASHSCOPE_API_KEY`); no other runtime configuration knobs.
- Cap HTTP timeouts to ≤15 seconds per synthesis call.
- Leverage existing ffmpeg-based normalization pipeline; no new native dependencies.
- Execute the three synthesis requests sequentially for predictable behavior (future optimization optional).

## 5. Integration Details
- Use fixed constants for model (`qwen-tts-latest`), voices (Cherry, Serena, Chelsie), output format (`mp3`), and endpoint URL (`https://dashscope-intl.aliyuncs.com/api/v1/services/aigc/multimodal-generation/speech-synthesis`).
- Build request body with Hanzi text (`input`), ensuring pinyin is only used for cache naming.
- Parse the signed audio URL from each response, download via `HttpClient`, and hand off to `AudioCache.ensureCachedNormalized` keyed by voice + URL hash.
- Surface rate-limit or network errors directly so callers can react (per fail-fast principle).

## 6. Architecture Changes
### Domain (`zh-learn-domain`)
- No API changes anticipated. Update domain tests if default provider configuration expectations shift.

### Infrastructure (`zh-learn-infrastructure`)
- Add DashScope SDK dependency and the corresponding `requires` statement in `module-info.java`.
- Implement `com.zhlearn.infrastructure.qwen.QwenAudioProvider`:
  - Encapsulate a thin `QwenTtsClient` to isolate SDK usage and simplify testing.
  - For each voice (Cherry → Serena → Chelsie), perform synthesis, download, normalize, and collect absolute paths.
  - Override `getPronunciations` to return the ordered list; keep `getPronunciation` delegating to the first element.
  - Throw descriptive runtime exceptions on missing API key or failed HTTP responses.
- Extend `META-INF/services/com.zhlearn.domain.provider.AudioProvider` to register the provider.

### Application (`zh-learn-application`)
- Rely on ServiceLoader to surface the new provider; ensure metadata aggregation marks it as `ProviderClass.AUDIO` and `ProviderType.AI`.
- Verify existing audio selection flows correctly handle three returned pronunciations.

### CLI (`zh-learn-cli`)
- Update acceptance tests and provider listing output to mention the Qwen provider and enumerate its three voices.
- Document that lookups trigger three synthesis calls and may take longer than single-voice providers.

## 7. Test Strategy (Test-First)
1. **Cucumber acceptance** – add scenario ensuring the CLI lists Qwen as an audio provider with Cherry/Serena/Chelsie noted.
2. **Application tests** – verify `ProviderRegistry` exposes the provider and that the audio selection flow handles multiple pronunciations.
3. **Infrastructure unit tests**
   - Stub the `QwenTtsClient` to return deterministic URLs; assert three synthesis calls, distinct cached file paths, and `getPronunciation` returning only Cherry.
   - Confirm missing API key or HTTP failure throws immediately.
   - Verify caching prevents duplicate downloads when voices are requested again.
4. **Documentation validation** – update any snapshot/format tests impacted by provider listings.

## 8. Work Breakdown
1. Design `QwenTtsClient` interface and tests covering API key retrieval and request assembly with fixed constants.
2. Write failing unit tests for `QwenAudioProvider` covering multi-voice success, caching, and error scenarios.
3. Implement provider logic to satisfy tests; wire into ServiceLoader and module metadata.
4. Update CLI/application acceptance tests for provider visibility and multi-voice behavior.
5. Refresh documentation (README, AGENTS) with setup instructions limited to `DASHSCOPE_API_KEY` and the voice list.
6. Run full build (`mvn test`, `mvn -pl zh-learn-cli -am package`) to confirm integration.

## 9. Risks & Mitigations
- **Region limitations:** If the fixed international endpoint is inaccessible, document the limitation and note that future work may add override support.
- **Rate limiting:** Three sequential calls can hit quotas; failures will be visible per fail-fast requirements.
- **SDK JPMS compatibility:** Validate module naming; if conflicts arise, consider shading or custom module definitions.
- **Performance:** Triple synthesis increases latency; communicate expectation to users and evaluate parallelization later.

## 10. Open Questions
- Should CLI display per-voice metadata (duration, speaker info) to aid selection? (Future enhancement.)
- Is there a need for an offline fallback if DashScope is unreachable? (Currently out of scope.)
- Are there licensing constraints on distributing synthesized audio output? Confirm before release.

## 11. Constitution Check
- **Test-first:** Plan mandates new Cucumber + unit tests before implementation.
- **Fail-fast:** Provider throws on misconfiguration or HTTP failures; no silent fallbacks.
- **Modularity:** Implementation isolated to infrastructure; other modules consume interfaces only.
- **CLI-first:** Provider surfaced through CLI commands and documentation updates.

## 12. Follow-up Tasks
- Confirm current DashScope SDK version and its compatibility with GraalVM native builds (track adjustments if necessary).
- Evaluate need for optional real-API integration tests under manual gating.
- Monitor Qwen voice catalog for changes that might require plan adjustments.
