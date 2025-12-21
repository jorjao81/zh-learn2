# ZH Learn - Chinese Learning Tool

Experimenting how to ensure that AI agentic coding results into reasonably maintainable
code.

## Prerequisites

- Java 25+ with GraalVM (for native compilation)
- Maven 3.8+

## Build

```bash
# Build all modules
mvn clean package

# Build native executable (requires GraalVM)
cd zh-learn-cli && mvn native:compile-no-fork -Pnative
```

## Run

### Modular JVM execution
```bash
./zh-learn.sh word 学习
```

### Audio lookup (Existing Anki Pronunciation)
- Default location: export your Anki collection (TSV) as `Chinese.txt` to `~/.zh-learn/Chinese.txt`.
- The file must have the first column as the Note Type. Currently the parser supports the `Chinese 2` note type with the following columns:
  - `Chinese 2`: 0 Note Type, 1 Simplified, 2 Pinyin, 3 Pronunciation
  - Other note types are ignored by the current parser.

```bash
# Query an existing pronunciation by exact pinyin match
./zh-learn.sh audio 学习 xuéxí
# Output (if present in your collection):
[sound:xuéxí.mp3]

# Specify provider explicitly (optional)
./zh-learn.sh audio 学 xué --audio-provider anki

# Optional: configure Anki media directory (for playback)
# Use either a system property or environment variable:
#   -Danki.media.dir="/path/to/Anki2/User 1/collection.media"
#   ANKI_MEDIA_DIR=/path/to/Anki2/User\ 1/collection.media
```

### Parse Pleco export with interactive audio
- `./zh-learn.sh parse-pleco Chinese.txt`
- Requires an interactive terminal (JLine raw mode). Run from a real TTY; piping or redirecting output will exit immediately.
- Each analyzed word launches the audio selector (arrow keys navigate, Space replays, Enter selects, Esc skips).
- Selected audio is normalized into `~/.zh-learn/audio/<provider>/...` and copied into the configured Anki media directory before TSV export.
- Configure the Anki media directory via `ZHLEARN_ANKI_MEDIA_DIR` or `-Dzhlearn.anki.media.dir=/path/to/collection.media` so the exported `[sound:...]` references resolve inside Anki.

### Native executable
```bash
./zh-learn-cli/target/zh-learn word 学习
```


## Performance

- **Native executable**: 27ms startup time
- **Modular JVM**: 144ms startup time
- **Native size**: 30.89MB (standalone)

## Test

```bash
mvn test
```

## Configure AI Providers

Set API keys and optional base URLs via environment variables (or JVM `-D` properties). All AI providers use OpenAI-compatible endpoints through LangChain4j.

- OpenAI GPT-5 Nano
  - `OPENAI_API_KEY`
  - `OPENAI_BASE_URL` (default: `https://api.openai.com/v1`)
  - Provider name: `gpt-5-nano`

- DeepSeek
  - `DEEPSEEK_API_KEY`
  - `DEEPSEEK_BASE_URL` (default: `https://api.deepseek.com/v1`)
  - Provider name: `deepseek-chat`

- ChatGLM via z.ai
  - `ZAI_API_KEY`
  - `ZAI_BASE_URL` (default: `https://api.z.ai/openai/v1`; override if your workspace uses a different gateway)
  - Provider names: `glm-4-flash`, `glm-4.5`

Select providers at runtime:
```bash
./zh-learn.sh providers -d
./zh-learn.sh word 学习 --provider glm-4.5
./zh-learn.sh word 学习 --explanation-provider glm-4.5 --example-provider glm-4.5
```

## Providers: Audio (Pronunciation)

- Audio providers return the path to an mp3 file; conversion to `[sound:filename.mp3]` happens during Anki export.
- Current providers:
  - `anki`: scans the Anki export at `~/.zh-learn/Chinese.txt` and reuses the pronunciation of any card with the exact same pinyin (tone marks included). If nothing is found, it returns no pronunciation.
  - `forvo`: fetches pronunciations from Forvo (manual selection only). Requires `FORVO_API_KEY` in the environment or `-Dforvo.api.key=...`.
  - `qwen-tts`: calls Alibaba Qwen text-to-speech and normalizes three voices per lookup (`Cherry`, `Serena`, `Chelsie`). Requires `DASHSCOPE_API_KEY` with access to the international DashScope endpoint hosted in the Singapore region. Each request downloads and caches all three voices so they remain available offline.

Audio playback file resolution:
- Default Anki media directory on macOS: `~/Library/Application Support/Anki2/User 1/collection.media/`
- Override with `ZHLEARN_ANKI_MEDIA_DIR` or system property `-Dzhlearn.anki.media.dir=/path/to/collection.media`

Playback resolution order:
- Absolute/relative path if it exists
- `anki.media.dir` or `ANKI_MEDIA_DIR` joined with the filename
- CLI fixtures (sample audio) for development

List providers and capabilities:
```bash
./zh-learn.sh providers -d

# Qwen TTS (generates three voices per lookup)
./zh-learn.sh audio 学习 xuéxí --audio-provider qwen-tts
# → returns three absolute paths (Cherry, Serena, Chelsie) once `DASHSCOPE_API_KEY` is configured
```

### Providers: Qwen3 (DashScope)

- Requires `DASHSCOPE_API_KEY` (China Mainland region). Optional `DASHSCOPE_BASE_URL` defaults to `https://dashscope.aliyuncs.com/compatible-mode/v1`.
- Supported models (provider names): `qwen3-max`, `qwen3-plus`, `qwen3-flash`.
- Internally, these map to current DashScope model IDs:
  - `qwen3-max` -> `qwen3-max-preview`
  - `qwen3-plus` -> `qwen-plus-latest`
  - `qwen3-flash` -> `qwen-turbo-latest`

Examples:
```bash
# Analyze a single word with Qwen3 Flash
./zh-learn.sh word 学习 --provider qwen3-flash

# Parse TSV and analyze with Qwen3 Plus
./zh-learn.sh parse-pleco flash-test.txt \
  --decomposition-provider qwen3-plus \
  --example-provider qwen3-plus \
  --explanation-provider qwen3-plus --limit 1
```
