# Contract — Filenames

## Scheme
- `{term}-{provider}-{variant}-{hash8}.mp3`
- Examples:
  - `学习-forvo-user_lihua-1a2b3c4d.mp3`
  - `学习-anki-deck_hsk4-9f8e7d6c.mp3`

## Rules
- `term`: may contain Chinese characters; do not escape; ensure filesystem supports UTF-8.
- `provider`: lowercase enum value (anki|forvo|qwen|tencent).
- `variant`: provider-specific token; replace spaces with `_`; strip path separators.
- `hash8`: first 8 hex chars of SHA-256 over canonical key.
- Total filename length ≤ 200 chars.

## Canonical Key
`{term}\u0000{lang}\u0000{provider}\u0000{variant}\u0000{bitrate}\u0000{iLufs}`

---
