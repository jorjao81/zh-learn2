# Contract — Selection UI (CLI)

- Package: `com.zhlearn.cli.audio`
- Purpose: Provide keyboard-driven selection for pronunciation candidates without embedding provider logic.

## Behavior
- Input: `SelectionSession` from application with ordered `SelectionItem`s.
- Keys:
  - Up/Down: move highlight; clamp at first/last
  - Enter: choose highlighted
  - Space: replay highlighted
  - `s` or ESC: skip selection (no audio chosen)
- Auto-play: highlighted item plays on navigation; cancel previous playback immediately on move.
- Error display: show concise error when playback fails; remain on current item and allow Space to retry.

## I/O Contract
- In: `SelectionSession { items: List<SelectionItem>, term: ChineseTerm }`
- Out: `UserSelection { chosen?: SelectionItem, skipped: boolean }`
- Side effects: audio playback via `AudioPlayer` port; no file writes.

## Non-Goals
- No provider discovery or network I/O.
- No audio normalization/transcoding.

---
