# Data Model — Audio Provider System

## Entities

1) ChineseTerm
- Fields: `text` (String, UTF-8), `language` (String = zh-Hans), `pinyin` (optional String)
- Notes: Accepts Chinese characters; normalization handled upstream.

2) ProviderId
- Fields: `name` (enum: ANKI, FORVO, QWEN_TTS, TENCENT_TTS), `version` (String optional)

3) PronunciationCandidate
- Fields: `provider` (ProviderId), `term` (ChineseTerm), `variant` (String: forvoUser | ttsVoice | ankiSource), `uri` (source URI or descriptor), `durationMs` (optional), `qualityScore` (optional), `cached` (boolean)
- Constraints: mp3 guaranteed only after pipeline; candidates may be wav/other prior to transcode.

4) AudioCacheEntry
- Fields: `key` (String canonical hash key), `filePath` (Path to mp3), `metadataPath` (Path to sidecar json), `createdAt`, `provider`, `variant`, `termText`, `hash8`
- Constraints: File exists and is readable; volume-normalized.

5) SelectionItem
- Fields: `label` (String), `provider`, `variant`, `durationMs` (optional), `cacheStatus` (HIT|MISS), `playbackRef` (link to cached or temp file for playback)

6) UserSelection
- Fields: `term` (ChineseTerm), `chosen` (optional SelectionItem), `skipped` (boolean), `timestamp`

7) ForvoKnownUsers
- Fields: `orderedUsers` (List<String>)

8) TtsVoiceMap
- Fields: `provider` (ProviderId), `voiceByLocale` (Map<String locale, String voiceId>)

9) AppConfig
- Fields: `ankiMediaDir` (Path), `cacheDir` (Path), `bitrateKbps` (int default 128), `loudnessLufs` (double default -15), `timeouts` (per-provider ms), `forvoKnownUsers` (ForvoKnownUsers), `ttsVoices` (TtsVoiceMap), credentials (env-backed)

## Relationships
- PronunciationCandidate references ChineseTerm and ProviderId.
- AudioCacheEntry derived from PronunciationCandidate and pipeline parameters.
- SelectionItem built from PronunciationCandidate and cache lookup results.
- UserSelection links a ChineseTerm to a SelectionItem (or skip).

## Validation Rules
- ChineseTerm.text non-empty; must be valid UTF-8.
- AppConfig.ankiMediaDir must exist and be writable (fail-fast otherwise).
- Cache entries must reference existing files; missing files invalidate cache entry.

## State Transitions
1. term → candidates (gathered by providers, possibly mixed formats)
2. candidate → normalized mp3 via pipeline → cache entry
3. cache entry → selection item (with cache HIT)
4. user selection → copy to Anki media (if chosen and not already present)

