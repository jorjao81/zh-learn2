# Vocabulary Markdown Format

This directory contains format specifications for vocabulary markdown files.

## Format Types

| Format           | File                    | Use For                      |
|------------------|-------------------------|------------------------------|
| Single Character | `FORMAT-SINGLE-CHAR.md` | Individual characters (单字) |
| Multi-Character  | `FORMAT-MULTI-CHAR.md`  | Compounds, idioms, proverbs  |

## Directory Structure

```text
linguistics/vocabulary/
├── FORMAT.md                 # This file
├── FORMAT-SINGLE-CHAR.md     # Single character format
├── FORMAT-MULTI-CHAR.md      # Multi-character format
├── PROMPT.md                 # AI generation prompts
├── single/                   # Single character files
│   └── 晶.md
└── multi/                    # Multi-character files
    ├── 学习.md
    └── 生龙活虎.md
```

## Quick Reference

### Example Sentence Format

All examples use `==` markers in hanzi, pinyin, and English:

```markdown
- 我每天==学习==中文。
  - _Wǒ měitiān ==xuéxí== Zhōngwén._
  - I ==study== Chinese every day.
```

### Etymology Arrow Notation

For semantic evolution, use arrow notation:

```text
(orig.) stars → brilliant → crystal
(orig.) door bolt → linchpin ⇒ keyboard key
```

- `→` gradual shift
- `⇒` conceptual leap

### Notes Section (both formats)

```markdown
## Notes

- **Type**: {single-char / compound / idiom / proverb}
- **HSK Level**: {HSK 1-6 or "Above HSK 6"}
- **Frequency**: {very common / common / uncommon / rare}
- **Register**: {formal / informal / neutral / literary / colloquial}
```

## See Also

- `FORMAT-SINGLE-CHAR.md` - Full single character specification
- `FORMAT-MULTI-CHAR.md` - Full multi-character specification
- `PROMPT.md` - AI prompts for generating content
