# Confusing Card

A flashcard for distinguishing easily confused Chinese characters or words.

---

## Format

### Frontmatter (required)

Just enough to generate the card front:

```yaml
---
items:
  - 怨
  - 恕
  - 怒
---
```

For multi-reading (same character, different pronunciations):

```yaml
---
items:
  - hanzi: 了
    pinyin: le
  - hanzi: 了
    pinyin: liǎo
---
```

### Body (free-form)

Whatever best explains the difference. The AI or author chooses the format:
tables, prose, bullet points, callouts, comparisons, etymology, examples.

---

## Examples

### Example 1: 怨 vs 恕 vs 怒

```markdown
---
items:
  - 怨
  - 恕
  - 怒
---

All are phono-semantic characters with the 心 (heart) radical.

| Char | Pinyin | Phonetic | Meaning | Memory Hook |
|------|--------|----------|---------|-------------|
| 怨 | yuàn | 夗 (toss and turn) | resentment | tossing in bed with grudges |
| 恕 | shù | 如 (as if) | forgive | heart "as if" it were another's |
| 怒 | nù | 奴 (slave) | anger | treated like a slave → rage |
```

### Example 2: 己 vs 已 vs 巳

```markdown
---
items:
  - 己
  - 已
  - 巳
---

Three characters differing only in stroke closure at the top-right.

**己** (jǐ) — OPEN → oneself
- 自己, 知己
- Mnemonic: open to self-reflection

**已** (yǐ) — HALF-CLOSED → already
- 已经, 已知
- Mnemonic: the door is closing on that time

**巳** (sì) — FULLY CLOSED → snake hour (9-11am)
- 巳时, 巳蛇
- Mnemonic: snake curled in a ball

> 己开已半巳全封 (jǐ open, yǐ half, sì sealed)
```

### Example 3: 了 (le vs liǎo)

```markdown
---
items:
  - hanzi: 了
    pinyin: le
  - hanzi: 了
    pinyin: liǎo
---

Same character, two pronunciations.

**le** — grammatical particle
- Marks completed action: 我吃了
- Marks change of state: 下雨了
- Light, quick, no lexical meaning

**liǎo** — verb/complement meaning "finish" or "able to"
- V不了: 吃不了 (can't finish eating)
- 了不起: amazing
- 了解: to understand

**Rule:** If 得/不 comes before 了, it's always **liǎo**.
```

### Example 4: 在 vs 再

```markdown
---
items:
  - 在
  - 再
---

Both pronounced zài. Constantly confused in writing.

| | 在 | 再 |
|---|---|---|
| Meaning | at, in, -ing | again, then |
| Grammar | preposition | adverb |
| Question | Where? | Again? |
| Component | 土 (earth) | 一 on top |

**在** — WHERE you are: 我在家 (I'm at home)
**再** — do it AGAIN: 再说一遍 (say it again)

Memory: 在 has earth (土) = standing AT a place.
```

### Example 5: 打算 vs 计划

```markdown
---
items:
  - 打算
  - 计划
---

Both mean "plan" but differ in formality.

**打算** (dǎsuàn) — informal intention
- Verb only, must be followed by another verb
- 我打算去 (I'm planning to go)
- Etymology: 打 (hit) + 算 (calculate) = roughly calculate

**计划** (jìhuà) — formal plan
- Can be noun or verb
- 五年计划 (five-year plan)
- Etymology: 计 (count) + 划 (draw lines) = draw up details

Use 打算 for casual intentions, 计划 for serious plans.
```

---

## Directory

```text
linguistics/exercises/confusing/
├── 怨-恕-怒.md
├── 己-已-巳.md
├── 了-le-liǎo.md
├── 在-再.md
└── 打算-计划.md
```

Flat structure. Filename is the items joined by hyphens.
