# Highlighted Translation Card

## Name

**Highlighted Translation Card** (高亮翻译卡片)

## Definition

A flashcard-style exercise for passive grammar recognition where the learner is shown a sentence in the target language with the grammar point visually highlighted, then reveals a translation with the corresponding element also highlighted.

## Description

This exercise type focuses on **passive recognition** rather than active production. The learner:

1. Reads a complete sentence in Chinese
2. Identifies the highlighted grammar point in context
3. Flips the card to see the English translation
4. Observes how the highlighted Chinese maps to the highlighted English

The highlighting creates a visual anchor that reinforces the connection between the Chinese grammar pattern and its English equivalent, even when the English translation varies (e.g., 似乎 → "seems" / "appears" / "looks like").

## Format

### Front (Chinese)

```html
<div class="sentence-section">
  <div class="sentence-content chinese">
    我<span class="hl">几乎</span>每天都运动。
  </div>
</div>

<div class="word-hint">
  <span class="hanzi">几乎</span>
  <span class="pinyin">jīhū</span>
</div>
```

### Back (English)

```html
<div class="sentence-section">
  <div class="sentence-content chinese">
    我<span class="hl">几乎</span>每天都运动。
  </div>
</div>

<hr class="divider" />

<div class="sentence-section">
  <div class="sentence-content english">
    I exercise <span class="hl">almost</span> every day.
  </div>
</div>
```

## Styling

See `grammar-sentence-card.css` — designed to extend `word-memo.css` conventions.

Key classes:

- `.sentence-content.chinese` — red left border, CJK font stack
- `.sentence-content.english` — blue left border, italic
- `.hl` — yellow gradient highlight (Chinese) / green gradient (English)
- `.word-hint` — displays target word + pinyin
- `.grammar-content` — purple left border for grammar explanations
- `.characters-content` — orange left border for character breakdowns

## Example Cards

| Front (Chinese)          | Back (English)                         |
| ------------------------ | -------------------------------------- |
| 我**几乎**每天都运动。   | I exercise **almost** every day.       |
| 他**似乎**不太想去。     | He doesn't **seem** to want to go.     |
| 这道题**几乎**没人会做。 | **Almost** no one can do this problem. |
| 她**似乎**生气了。       | She **seems** to be angry.             |
| 天**似乎**要下雨了。     | It **looks like** it's going to rain.  |

## Use Cases

- Learning modal adverbs (几乎, 似乎, 居然, etc.)
- Distinguishing similar grammar patterns
- Building passive recognition before active production
- Vocabulary in context

## Spaced Repetition Integration

- **Correct**: Learner understood the sentence and the grammar point's function
- **Incorrect**: Learner misunderstood the meaning or couldn't parse the grammar

For grammar points with multiple words (e.g., 四11 covers both 几乎 and 似乎), consider:

- Treating each word as a separate card set
- Mixing cards from both to reinforce contrast
- Requiring N consecutive correct answers across the set before marking the grammar point as "learned"

## Advantages

- Simple to implement
- Low cognitive load per card
- Scales well (easy to generate many examples per grammar point)
- Highlighting prevents "translation without understanding"
- Works for any grammar point that can be isolated in a sentence

## Limitations

- Passive only — doesn't test production
- Learner might memorize specific sentences rather than the pattern
- Requires well-chosen example sentences that clearly demonstrate the grammar point

---

## Anki Implementation

### Note Type: Grammar Sentence

#### Fields

| Field                | Description                                                    | Example                                                |
| -------------------- | -------------------------------------------------------------- | ------------------------------------------------------ |
| `SentenceCN`         | Chinese sentence with `<span class="hl">` around target word   | `我<span class="hl">几乎</span>每天都运动。`           |
| `SentencePinyin`     | Full pinyin of the Chinese sentence (tone marks, no highlight) | `Wǒ jīhū měitiān dōu yùndòng.`                         |
| `SentenceEN`         | English translation with `<span class="hl">` around equivalent | `I exercise <span class="hl">almost</span> every day.` |
| `Word`               | The target word (for reference/search)                         | `几乎`                                                 |
| `Pinyin`             | Pinyin of the target word                                      | `jīhū`                                                 |
| `GrammarPoint`       | Grammar point ID                                               | `四11`                                                 |
| `GrammarExplanation` | Detailed explanation of the grammar point                      | (see below)                                            |
| `CharacterBreakdown` | Character-by-character breakdown of the word                   | (see below)                                            |

#### Tags

Each note receives two tags:

- `grammar::四11` — the grammar point
- `word::几乎` — the specific word

This allows filtering by either grammar point or word.

#### Card Template

**Required:** Add `_marked.min.js` to your Anki media collection folder. Download from [marked.js CDN](https://cdn.jsdelivr.net/npm/marked/marked.min.js) and rename with `_` prefix.

**Front Template:**

```html
<div class="sentence-section">
  <div class="sentence-content chinese">{{SentenceCN}}</div>
</div>
```

**Back Template:**

```html
<script src="_marked.min.js"></script>

<div class="sentence-section">
  <div class="sentence-content chinese">{{SentenceCN}}</div>
  <div class="sentence-pinyin">{{SentencePinyin}}</div>
</div>

<hr class="divider" />

<div class="sentence-section">
  <div class="sentence-content english">{{SentenceEN}}</div>
</div>

<div class="grammar-details">
  <div class="section-title">Grammar: {{GrammarPoint}} — {{Word}}</div>
  <div class="grammar-content md">{{GrammarExplanation}}</div>
</div>

<div class="characters-details">
  <div class="section-title">Character Breakdown</div>
  <div class="characters-content md">{{CharacterBreakdown}}</div>
</div>

<script>
  document.querySelectorAll(".md").forEach((el) => {
    // Decode HTML entities using browser's built-in decoder
    let decoder = document.createElement("textarea");
    decoder.innerHTML = el.innerHTML;
    el.innerHTML = marked.parse(decoder.value);
  });
</script>
```

### Example Note Data

#### Note 1

| Field              | Value                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| ------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| SentenceCN         | `我<span class="hl">几乎</span>每天都运动。`                                                                                                                                                                                                                                                                                                                                                                                             |
| SentencePinyin     | `Wǒ jīhū měitiān dōu yùndòng.`                                                                                                                                                                                                                                                                                                                                                                                                           |
| SentenceEN         | `I exercise <span class="hl">almost</span> every day.`                                                                                                                                                                                                                                                                                                                                                                                   |
| Word               | `几乎`                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| Pinyin             | `jīhū`                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| GrammarPoint       | `四11`                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| GrammarExplanation | `<p><strong>几乎</strong> (jīhū) expresses that something is very close to 100% but not quite. It indicates degree/extent, not uncertainty.</p><h3>Usage</h3><ul><li>Use before verbs or 都/没有</li><li>Common patterns: 几乎都…, 几乎没有…, 几乎每…</li></ul>`                                                                                                                                                                         |
| CharacterBreakdown | `<ul><li><span class="hanzi">几</span> <span class="pinyin">jǐ/jī</span> <span class="meaning">how many / nearly</span><span class="etymology">originally a picture of a small table, now means "how many" or "almost"</span></li><li><span class="hanzi">乎</span> <span class="pinyin">hū</span> <span class="meaning">particle</span><span class="etymology">classical question/exclamation particle, adds emphasis</span></li></ul>` |
| Tags               | `grammar::四11 word::几乎`                                                                                                                                                                                                                                                                                                                                                                                                               |

#### Note 2

| Field              | Value                                                                                                                                                                                                                                                                                                                                                                                                               |
| ------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| SentenceCN         | `他<span class="hl">似乎</span>不太想去。`                                                                                                                                                                                                                                                                                                                                                                          |
| SentencePinyin     | `Tā sìhū bù tài xiǎng qù.`                                                                                                                                                                                                                                                                                                                                                                                          |
| SentenceEN         | `He doesn't <span class="hl">seem</span> to want to go.`                                                                                                                                                                                                                                                                                                                                                            |
| Word               | `似乎`                                                                                                                                                                                                                                                                                                                                                                                                              |
| Pinyin             | `sìhū`                                                                                                                                                                                                                                                                                                                                                                                                              |
| GrammarPoint       | `四11`                                                                                                                                                                                                                                                                                                                                                                                                              |
| GrammarExplanation | `<p><strong>似乎</strong> (sìhū) expresses a subjective impression or uncertain judgment based on observation.</p><h3>Meaning</h3><p>It means "it seems/appears that..." Use when you're not sure but inferring from evidence.</p><h3>vs. 好像</h3><p>Often interchangeable with 好像 but slightly more formal.</p>`                                                                                                |
| CharacterBreakdown | `<ul><li><span class="hanzi">似</span> <span class="pinyin">sì</span> <span class="meaning">similar, seem</span><span class="etymology">left: person (亻), right: phonetic. Means "to resemble"</span></li><li><span class="hanzi">乎</span> <span class="pinyin">hū</span> <span class="meaning">particle</span><span class="etymology">same as in 几乎, adds the sense of "being in a state of"</span></li></ul>` |
| Tags               | `grammar::四11 word::似乎`                                                                                                                                                                                                                                                                                                                                                                                          |

### Anki Export TSV Format

The TSV file uses Anki's import directives to specify note type, field mapping, and tags.

#### Header Directives

```tsv
#separator:Tab
#html:true
#notetype:Grammar Sentence
#columns:SentenceCN SentencePinyin SentenceEN Word Pinyin GrammarPoint GrammarExplanation CharacterBreakdown Tags
```

| Directive        | Description                                      |
| ---------------- | ------------------------------------------------ |
| `#separator:Tab` | Fields are tab-separated                         |
| `#html:true`     | Fields contain HTML (enables `<span>` rendering) |
| `#notetype:`     | Target note type name in Anki                    |
| `#columns:`      | Tab-separated field names matching note type     |

#### Complete Example TSV

```tsv
#separator:Tab
#html:true
#notetype:Grammar Sentence
#columns:SentenceCN SentencePinyin SentenceEN Word Pinyin GrammarPoint GrammarExplanation CharacterBreakdown Tags
我<span class="hl">几乎</span>每天都运动。 Wǒ jīhū měitiān dōu yùndòng. I exercise <span class="hl">almost</span> every day. 几乎 jīhū 四11 <p><strong>几乎</strong> (jīhū) expresses that something is very close to 100% but not quite.</p><h3>Usage</h3><ul><li>Use before verbs or 都/没有</li><li>Common patterns: 几乎都…, 几乎没有…, 几乎每…</li></ul> <ul><li><span class="hanzi">几</span> <span class="pinyin">jǐ/jī</span> <span class="meaning">how many / nearly</span><span class="etymology">originally a picture of a small table</span></li><li><span class="hanzi">乎</span> <span class="pinyin">hū</span> <span class="meaning">particle</span><span class="etymology">classical question/exclamation particle</span></li></ul> grammar::四11 word::几乎
他<span class="hl">似乎</span>不太想去。 Tā sìhū bù tài xiǎng qù. He doesn't <span class="hl">seem</span> to want to go. 似乎 sìhū 四11 <p><strong>似乎</strong> (sìhū) expresses a subjective impression or uncertain judgment.</p><h3>Meaning</h3><p>"It seems/appears that..." — use when inferring from evidence.</p><h3>vs. 好像</h3><p>Often interchangeable, but 似乎 is more formal.</p> <ul><li><span class="hanzi">似</span> <span class="pinyin">sì</span> <span class="meaning">similar, seem</span><span class="etymology">left: person (亻), right: phonetic</span></li><li><span class="hanzi">乎</span> <span class="pinyin">hū</span> <span class="meaning">particle</span><span class="etymology">adds the sense of "being in a state of"</span></li></ul> grammar::四11 word::似乎
```

#### Field Notes

| Field                | Format                                                                         |
| -------------------- | ------------------------------------------------------------------------------ |
| `SentenceCN`         | HTML with `<span class="hl">` around target word                               |
| `SentencePinyin`     | Full pinyin of sentence with tone marks, no highlighting (e.g., `Wǒ jīhū...`)  |
| `SentenceEN`         | HTML with `<span class="hl">` around English equivalent                        |
| `Word`               | Plain text (target word)                                                       |
| `Pinyin`             | Tone marks (jīhū), not numbers (ji1hu1)                                        |
| `GrammarPoint`       | ID format: 四11, 五03, etc.                                                    |
| `GrammarExplanation` | **Markdown** with real newlines (quoted field in TSV); rendered via marked.js  |
| `CharacterBreakdown` | HTML list with semantic classes: `.hanzi`, `.pinyin`, `.meaning`, `.etymology` |
| `Tags`               | Space-separated, hierarchical with `::` (e.g., `grammar::四11 word::几乎`)     |

#### Import Instructions

1. In Anki, go to **File → Import**
2. Select the `.tsv` file
3. Anki will auto-detect the note type and field mapping from the header directives
4. Verify the preview shows correct field alignment
5. Click **Import**

If the note type doesn't exist, create it first with the fields listed above, then import.

### File Structure

Pre-generated content lives in the repository:

```text
linguistics/
├── grammar/
│   ├── explanations/
│   │   ├── 四11.md          # Grammar point explanation
│   │   ├── 四12.md
│   │   └── ...
│   ├── characters/
│   │   ├── 几乎.md          # Character breakdown
│   │   ├── 似乎.md
│   │   └── ...
│   └── sentences/
│       ├── 四11-几乎.tsv    # Example sentences for 几乎
│       ├── 四11-似乎.tsv    # Example sentences for 似乎
│       └── ...
└── exports/
    └── grammar-sentences.tsv  # Combined Anki-ready export
```

A build script combines the individual sentence files with their corresponding explanations and character breakdowns into the final `grammar-sentences.tsv` for Anki import.
