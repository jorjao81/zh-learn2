# Audio Command Flow Documentation

## Overview

The `audio` command provides direct pronunciation lookup for Chinese words by pinyin from existing pronunciation collections or AI-powered text-to-speech services. It's optimized for quick audio file retrieval without the overhead of full linguistic analysis.

## Command Structure

```
./zh-learn.sh audio <chinese-word> <pinyin> [options]
```

### Parameters
- `chinese-word`: Chinese characters (used for context and filename generation)
- `pinyin`: Exact pinyin with tone marks to match against pronunciation data

### Key Options
- `--audio-provider`: Audio provider to use (default: anki)
  - `anki`: Local Anki collection media files
  - `forvo`: Community pronunciation database
  - `qwen-tts`: AI-generated Chinese speech
  - `tencent`: Professional TTS service

## Processing Flow

### High-Level Audio Command Flow

```mermaid
flowchart TD
    A[Start audio command] --> B[Parse CLI Arguments]
    B --> C[Validate Parameters]
    C --> D[Resolve Audio Provider]
    D --> E[Create Minimal WordAnalysisService]
    E --> F[Execute Audio Lookup]
    F --> G{Audio Found?}
    G -->|Yes| H[Print Audio File Path]
    G -->|No| I[Print No Pronunciation Message]
    I --> J[Check for Anki Collection]
    J --> K[Print Setup Hints]
    H --> L[Complete]
    K --> L
```

## Audio Provider Resolution

```mermaid
flowchart TD
    A[Provider Name] --> B{Provider Type Check}
    B -->|anki| C[AnkiPronunciationProvider]
    B -->|forvo| D[ForvoAudioProvider]
    B -->|qwen-tts| E[QwenAudioProvider]
    B -->|tencent| F[TencentAudioProvider]
    B -->|unknown| G[Throw IllegalArgumentException]

    C --> H[Scan Local Media Files]
    D --> I[Configure Forvo API Client]
    E --> J[Configure Qwen TTS Client]
    F --> K[Configure Tencent TTS Client]

    H --> L[Ready Audio Provider]
    I --> M[Check API Key]
    J --> N[Check DASHSCOPE_API_KEY]
    K --> O[Check Tencent Credentials]

    M --> P{API Key Valid?}
    N --> Q{API Key Valid?}
    O --> R{Credentials Valid?}

    P -->|Yes| L
    P -->|No| S[Runtime Exception]
    Q -->|Yes| L
    Q -->|No| S
    R -->|Yes| L
    R -->|No| S
```

## Audio Lookup Pipeline

### Provider-Specific Lookup Flows

#### Anki Provider Flow

```mermaid
sequenceDiagram
    participant Cmd as AudioCommand
    participant AP as AnkiPronunciationProvider
    participant FS as FileSystem
    participant Cache as AudioCache

    Cmd->>AP: getPronunciation(hanzi, pinyin)
    AP->>FS: Scan ~/.zh-learn/Chinese.txt
    FS-->>AP: TSV entries or file not found
    AP->>FS: Check Anki User collection
    FS-->>AP: Media files in collection

    Note over AP: Match pinyin patterns
    AP->>AP: fuzzyPinyinMatch(targetPinyin, candidatePinyin)
    AP->>FS: Find matching media files
    FS-->>AP: List of audio file paths

    alt Files found
        AP->>Cache: Normalize audio files
        Cache-->>AP: Normalized file paths
        AP-->>Cmd: First matching file path
    else No files found
        AP-->>Cmd: Optional.empty()
    end
```

#### TTS Provider Flow (Qwen/Tencent)

```mermaid
sequenceDiagram
    participant Cmd as AudioCommand
    participant TTS as TTS Provider
    participant API as TTS API
    participant Cache as AudioCache
    participant Retry as RetryMechanism

    Cmd->>TTS: getPronunciation(hanzi, pinyin)
    TTS->>Cache: Check existing cache
    Cache-->>TTS: Cached file or miss

    alt Cache hit
        TTS-->>Cmd: Cached audio path
    else Cache miss
        TTS->>Retry: synthesize(voice, text)
        Retry->>API: HTTP POST to TTS endpoint

        alt Success
            API-->>Retry: Audio data (base64 or URL)
            Retry-->>TTS: TTS result
            TTS->>Cache: Decode and normalize audio
            Cache-->>TTS: Normalized file path
            TTS-->>Cmd: Audio file path
        else Rate Limited (HTTP 429)
            API-->>Retry: Rate limit error
            Note over Retry: Exponential backoff<br/>5s initial, 3x multiplier
            Retry->>API: Retry request after delay
        else Other Error
            API-->>Retry: Error response
            Retry-->>TTS: Exception
            TTS-->>Cmd: Optional.empty()
        end
    end
```

#### Forvo Provider Flow

```mermaid
sequenceDiagram
    participant Cmd as AudioCommand
    participant FP as ForvoAudioProvider
    participant API as Forvo API
    participant DL as AudioDownloadExecutor
    participant Cache as AudioCache

    Cmd->>FP: getPronunciation(hanzi, pinyin)
    FP->>API: Search pronunciation(word=hanzi)
    API-->>FP: List of pronunciation entries

    alt Pronunciations found
        FP->>FP: Filter by language (zh-CN)
        FP->>DL: Download audio files in parallel

        par Audio Download 1
            DL->>API: Download MP3 file
            API-->>DL: Audio binary data
        and Audio Download 2
            DL->>API: Download MP3 file
            API-->>DL: Audio binary data
        end

        DL->>Cache: Normalize downloaded files
        Cache-->>DL: Normalized audio paths
        DL-->>FP: List of audio file paths
        FP-->>Cmd: First available file path
    else No pronunciations
        FP-->>Cmd: Optional.empty()
    end
```

## Audio Caching and Normalization

### Audio Processing Pipeline

```mermaid
flowchart TD
    A[Raw Audio Data] --> B{Source Type}
    B -->|Base64| C[Decode Base64]
    B -->|URL| D[Download File]
    B -->|Local File| E[Read File]

    C --> F[Write Temporary File]
    D --> F
    E --> F

    F --> G[AudioCache.ensureCachedNormalized]
    G --> H{FFmpeg Available?}
    H -->|Yes| I[Normalize with FFmpeg]
    H -->|No| J[Copy without normalization]

    I --> K[Generate Cache Key]
    J --> K
    K --> L[Move to Cache Directory]
    L --> M[Return Cached Path]

    I --> N{Normalization Failed?}
    N -->|Yes| O[Fallback to Copy]
    O --> K
```

### Cache Key Generation

```mermaid
flowchart LR
    A[Provider Name] --> B[Source Identifier]
    B --> C[Word Characters]
    C --> D[Voice/Model Info]
    D --> E[SHA-1 Hash]
    E --> F[Cache Filename]

    G[Example: qwen-tts_学习_cherry_A1B2C3D4E5.mp3]
```

**Cache Benefits:**
- Avoids repeated API calls for same pronunciations
- Consistent audio normalization across providers
- Faster subsequent lookups
- Reduces bandwidth and API quota usage

## Error Handling and Fallbacks

### Error Response Strategy

```mermaid
flowchart TD
    A[Audio Lookup Request] --> B{Provider Available?}
    B -->|No| C[Provider Not Found Error]
    B -->|Yes| D[Execute Lookup]

    D --> E{Authentication Valid?}
    E -->|No| F[API Key Missing/Invalid Error]
    E -->|Yes| G[Provider-Specific Lookup]

    G --> H{Result Found?}
    H -->|Yes| I[Return Audio Path]
    H -->|No| J[Print No Pronunciation Message]

    J --> K{Is Anki Provider?}
    K -->|Yes| L[Check Default Export Location]
    K -->|No| M[Generic Not Found Message]

    L --> N{Export File Exists?}
    N -->|No| O[Print Setup Instructions]
    N -->|Yes| P[Check File Format]

    C --> Q[Exit with Error]
    F --> Q
    I --> R[Success]
    M --> R
    O --> R
    P --> R
```

### Anki Setup Guidance

When audio lookup fails with the Anki provider, the command provides helpful guidance:

```mermaid
flowchart TD
    A[No Pronunciation Found] --> B[Check ~/.zh-learn/Chinese.txt]
    B --> C{File Exists?}
    C -->|No| D[Print Export Instructions]
    C -->|Yes| E[Check File Format]

    D --> F[Show Expected Path]
    F --> G[Explain Export Process]
    G --> H[Note Type Requirements]

    E --> I{Correct Format?}
    I -->|No| J[Format Error Message]
    I -->|Yes| K[Pronunciation Not in Collection]
```

**Setup Instructions Provided:**
- Export Anki collection as TSV named 'Chinese.txt'
- Place in `~/.zh-learn/` directory
- Use 'Chinese 2' note type format
- Column mapping: 1=simplified, 2=pinyin, 3=pronunciation

## Audio Provider Comparison

### Feature Matrix

```mermaid
flowchart LR
    subgraph "Provider Capabilities"
        A[anki<br/>Local Files]
        B[forvo<br/>Community DB]
        C[qwen-tts<br/>AI Generated]
        D[tencent<br/>Professional TTS]
    end

    subgraph "Requirements"
        E[No API Key]
        F[Forvo API Key]
        G[DASHSCOPE_API_KEY]
        H[Tencent Credentials]
    end

    subgraph "Characteristics"
        I[Fastest<br/>Offline]
        J[Native Speakers<br/>Variable Quality]
        K[Consistent AI<br/>Multiple Voices]
        L[Professional<br/>High Quality]
    end

    A --> E
    A --> I
    B --> F
    B --> J
    C --> G
    C --> K
    D --> H
    D --> L
```

### Performance Characteristics

```mermaid
gantt
    title Audio Lookup Performance Comparison
    dateFormat X
    axisFormat %s

    section Anki (Local)
    File System Scan    :0, 1
    Audio Normalization :1, 2

    section Forvo (API)
    API Request         :0, 2
    Download Audio      :2, 4
    Audio Normalization :4, 5

    section Qwen TTS (Synthesis)
    TTS API Request     :0, 3
    Audio Generation    :3, 6
    Download + Cache    :6, 8

    section Tencent TTS (Synthesis)
    TTS API Request     :0, 3
    Audio Generation    :3, 5
    Download + Cache    :5, 7
```

## Data Flow and Output

### Command Output Formats

```mermaid
flowchart TD
    A[Audio Lookup Result] --> B{Audio Found?}
    B -->|Yes| C[Print Absolute File Path]
    B -->|No| D[Print '(no pronunciation)']

    C --> E[/Users/.../audio/file.mp3]
    D --> F[Check for Setup Issues]
    F --> G[Print Helpful Hints]

    E --> H[Command Success]
    G --> H
```

### Integration with Other Commands

```mermaid
flowchart TD
    A[Audio Command] --> B[Direct CLI Usage]
    A --> C[Called by Word Command]
    A --> D[Used in Parse-Pleco]

    B --> E[Manual Pronunciation Lookup]
    C --> F[Single Word Analysis Audio]
    D --> G[Batch Audio Generation]

    E --> H[Terminal Output]
    F --> I[Integrated Analysis Display]
    G --> J[Interactive Audio Selection]
```

## Configuration and Environment

### Required Environment Variables

```mermaid
flowchart TD
    A[Audio Provider] --> B{Provider Type}
    B -->|anki| C[No configuration required]
    B -->|forvo| D[FORVO_API_KEY required]
    B -->|qwen-tts| E[DASHSCOPE_API_KEY required]
    B -->|tencent| F[Multiple credentials required]

    D --> G[Set via environment or .envrc]
    E --> H[Alibaba Cloud DashScope API]
    F --> I[TENCENT_SECRET_ID<br/>TENCENT_API_KEY<br/>TENCENT_REGION]

    C --> J[Ready to use]
    G --> K[API validation at runtime]
    H --> K
    I --> K
```

### File System Layout

```
~/.zh-learn/
├── Chinese.txt              # Anki export file
└── audio/                   # Cached audio files
    ├── anki/                # Anki audio files (normalized)
    ├── forvo/               # Forvo downloads
    ├── qwen-tts/            # Qwen TTS generated
    └── tencent-tts/         # Tencent TTS generated
```

## Usage Examples

### Basic Lookup
```bash
# Look up pronunciation in local Anki collection
./zh-learn.sh audio 学习 xuéxí

# Use specific provider
./zh-learn.sh audio 学习 xuéxí --audio-provider forvo
```

### TTS Generation
```bash
# Generate pronunciation with AI TTS
./zh-learn.sh audio 你好 nǐhǎo --audio-provider qwen-tts

# Professional TTS service
./zh-learn.sh audio 你好 nǐhǎo --audio-provider tencent
```

### Integration Patterns
```bash
# Find audio file for use in other applications
AUDIO_FILE=$(./zh-learn.sh audio 学习 xuéxí)
if [ "$AUDIO_FILE" != "(no pronunciation)" ]; then
    echo "Found audio: $AUDIO_FILE"
    # Use with audio player, Anki import, etc.
fi
```

This comprehensive flow demonstrates how the audio command provides fast, reliable pronunciation lookup through multiple sources, with intelligent caching and fallback strategies for robust audio file management.