# Parse-Pleco Command Flow Documentation

## Overview

The `parse-pleco` command processes Pleco export files (TSV format) and analyzes each Chinese word through the complete zh-learn analysis pipeline. It supports both sequential and parallel processing modes, with configurable providers for different analysis components.

## Command Structure

```
./zh-learn.sh parse-pleco <file> [options]
```

### Key Options
- `--parallel-threads`: Number of threads for parallel processing (default: 10)
- `--disable-parallelism`: Force sequential processing
- `--limit`: Limit number of words to process
- `--export-anki`: Export results to Anki-compatible TSV file
- Various provider options for customizing analysis components

## Processing Flow

### High-Level Flow

```mermaid
flowchart TD
    A[Start parse-pleco] --> B[Parse Pleco TSV File]
    B --> C[Create Provider Instances]
    C --> D{Parallelism Enabled?}
    D -->|Yes| E[Parallel Processing Mode]
    D -->|No| F[Sequential Processing Mode]
    E --> G[Audio Selection Phase]
    F --> G
    G --> H{Export to Anki?}
    H -->|Yes| I[Export to TSV]
    H -->|No| J[Complete]
    I --> J
```

## File Parsing Phase

```mermaid
flowchart LR
    A[Pleco TSV File] --> B[PlecoExportParser]
    B --> C[Parse Each Line]
    C --> D[Extract Hanzi]
    C --> E[Convert Pinyin Tones]
    C --> F[Extract Definition]
    D --> G[PlecoEntry]
    E --> G
    F --> G
    G --> H[List of Entries]
```

**Components:**
- **PlecoExportParser**: Handles TSV parsing with tab delimiters
- **PinyinToneConverter**: Converts numbered pinyin (e.g., "ni3 hao3") to tone marks (e.g., "nǐ hǎo")
- **PlecoEntry**: Record containing hanzi, pinyin, and definition

## Provider Setup Phase

```mermaid
flowchart TD
    A[Provider Configuration] --> B[Create AI Providers]
    A --> C[Create Dictionary Providers]
    B --> D[ExampleProvider]
    B --> E[ExplanationProvider]
    B --> F[StructuralDecompositionProvider]
    B --> G[DefinitionFormatterProvider]
    C --> H[PinyinProvider]
    C --> I[DefinitionProvider]
    A --> J[AudioProvider]
    D --> K[WordAnalysisService]
    E --> K
    F --> K
    G --> K
    H --> K
    I --> K
    J --> K
```

**Special Handling:**
- Dictionary providers (`pleco-export`) use the parsed Pleco data directly
- AI providers require API keys and make external calls
- Audio providers handle pronunciation file retrieval

## Processing Modes

### Sequential Processing Mode

```mermaid
sequenceDiagram
    participant Main as Main Thread
    participant WAS as WordAnalysisService
    participant Providers as AI Providers
    participant Audio as AudioOrchestrator
    participant UI as InteractiveAudioUI

    loop For each word
        Main->>WAS: getCompleteAnalysis(word)
        WAS->>Providers: Call providers sequentially
        Providers-->>WAS: Return analysis components
        WAS-->>Main: Complete WordAnalysis
        Main->>Main: Print results immediately
        Main->>Audio: Get pronunciation candidates
        Main->>UI: Run audio selection
        UI-->>Main: Selected audio file
        Main->>Main: Update WordAnalysis with audio
    end
```

**Characteristics:**
- One word processed at a time
- Immediate display of results
- Lower memory usage
- Predictable resource consumption

### Parallel Processing Mode

```mermaid
sequenceDiagram
    participant Main as Main Thread
    participant Executor as Thread Pool
    participant WAS as WordAnalysisService
    participant Providers as AI Providers
    participant Audio as AudioOrchestrator

    Main->>Executor: Submit all words as CompletableFutures

    par Word 1
        Executor->>WAS: getCompleteAnalysis(word1)
        WAS->>Providers: Parallel provider calls
        Providers-->>WAS: Return components
        WAS-->>Executor: WordAnalysis1
        Executor-->>Main: Display result1 (when ready)
    and Word 2
        Executor->>WAS: getCompleteAnalysis(word2)
        WAS->>Providers: Parallel provider calls
        Providers-->>WAS: Return components
        WAS-->>Executor: WordAnalysis2
        Executor-->>Main: Display result2 (when ready)
    and Word N
        Executor->>WAS: getCompleteAnalysis(wordN)
        WAS->>Providers: Parallel provider calls
        Providers-->>WAS: Return components
        WAS-->>Executor: WordAnalysisN
        Executor-->>Main: Display resultN (when ready)
    end

    Main->>Main: All analysis complete
    Main->>Audio: Batch audio selection for all words
```

**Characteristics:**
- Multiple words processed simultaneously
- Results displayed as they complete (out of order)
- Higher throughput for large datasets
- Increased memory and API usage

## Word Analysis Pipeline Detail

### ParallelWordAnalysisService Internal Flow

```mermaid
flowchart TD
    A[getCompleteAnalysis called] --> B[Get Definition synchronously]
    A --> C[Get Pinyin synchronously]
    B --> D[Launch Parallel Tasks]
    C --> D

    D --> E[CompletableFuture: Structural Decomposition]
    D --> F[CompletableFuture: Examples with Definition]
    D --> G[CompletableFuture: Explanation]
    D --> H[CompletableFuture: Audio Pronunciation]

    E --> I[Wait for all futures]
    F --> I
    G --> I
    H --> I

    I --> J[Combine results into WordAnalysis]
```

**Why Definition and Pinyin are synchronous:**
- Other providers often depend on these results
- Examples provider needs definition text
- Audio provider needs pinyin for pronunciation lookup
- Fast providers (especially dictionary-based ones)

## Audio Selection Phase

```mermaid
flowchart TD
    A[Complete Word Analysis] --> B[AudioOrchestrator]
    B --> C[Get Pronunciation Candidates]
    C --> D{Candidates Available?}
    D -->|No| E[Skip Audio Selection]
    D -->|Yes| F[PrePlayback Processing]
    F --> G{Playable Candidates?}
    G -->|No| H[Skip Audio Selection]
    G -->|Yes| I[InteractiveAudioUI]
    I --> J[User Selects Audio]
    J --> K[Update WordAnalysis with Audio Path]
    E --> L[Original WordAnalysis]
    H --> L
    K --> L
```

**Interactive Audio UI Flow:**
1. Display available pronunciation options
2. Allow user to preview audio files
3. User makes selection or skips
4. Selected audio path added to WordAnalysis

## Data Structures

### Core Data Flow

```mermaid
flowchart LR
    A[PlecoEntry] --> B[Hanzi]
    B --> C[WordAnalysisService]
    C --> D[WordAnalysis]
    D --> E{Audio Selection}
    E --> F[Updated WordAnalysis]
    F --> G[Anki Export]
```

### WordAnalysis Components

```mermaid
classDiagram
    class WordAnalysis {
        +Hanzi word
        +Pinyin pinyin
        +Definition definition
        +StructuralDecomposition decomposition
        +Example examples
        +Explanation explanation
        +Optional~Path~ audioFile
    }

    class PlecoEntry {
        +String hanzi
        +String pinyin
        +String definitionText
    }

    class ProviderConfiguration {
        +String exampleProvider
        +String pinyinProvider
        +String definitionProvider
        +String definitionFormatterProvider
        +String decompositionProvider
        +String explanationProvider
        +String audioProvider
    }
```

## Performance Characteristics

### Parallel vs Sequential Comparison

```mermaid
gantt
    title Processing Time Comparison (100 words)
    dateFormat X
    axisFormat %s

    section Sequential
    Word 1     :0, 3
    Word 2     :3, 6
    Word 3     :6, 9
    Word 4     :9, 12
    Audio Selection :12, 15

    section Parallel (4 threads)
    Words 1-4 (Parallel)  :0, 3
    Words 5-8 (Parallel)  :3, 6
    Audio Selection       :6, 9
```

**Trade-offs:**
- **Sequential**: Lower resource usage, immediate feedback, predictable timing
- **Parallel**: Higher throughput, bursty resource usage, faster overall completion

## Error Handling

```mermaid
flowchart TD
    A[Word Processing] --> B{Provider Call Succeeds?}
    B -->|Yes| C[Continue to Next Provider]
    B -->|No| D[Log Error for Word]
    D --> E[Continue with Next Word]
    C --> F{All Providers Complete?}
    F -->|Yes| G[Word Successfully Analyzed]
    F -->|No| C
    G --> H[Add to Successful Results]
    E --> I[Skip Word in Results]
```

**Error Strategy:**
- Individual word failures don't stop processing
- Errors are logged with word context and timing
- Only successful analyses are included in export
- Final summary shows success/error counts

## Anki Export Process

```mermaid
flowchart LR
    A[Successful WordAnalysis List] --> B[AnkiExporter]
    B --> C[Convert to TSV Format]
    C --> D[Chinese 2 Anki Card Format]
    D --> E[Write to File]

    E --> F[Output: hanzi, pinyin, definition, examples, etc.]
```

**Export Format:**
- Compatible with Anki's "Chinese 2" note type
- Includes all analysis components
- Handles audio file paths if selected
- TSV format for easy import

## Configuration and Provider System

### Provider Selection Logic

```mermaid
flowchart TD
    A[Provider Name] --> B{Is 'pleco-export'?}
    B -->|Yes| C[Create Dictionary Provider]
    B -->|No| D[Create AI Provider]
    C --> E[Use Parsed Pleco Data]
    D --> F[Require API Key]
    F --> G[Create HTTP Client]
    E --> H[Provider Instance]
    G --> H
```

**Provider Types:**
- **Dictionary Providers**: Use local Pleco data (fast, no API calls)
- **AI Providers**: External API calls (slower, require authentication)
- **Audio Providers**: File-based or API-based pronunciation

This comprehensive flow shows how the parse-pleco command efficiently processes large vocabularies while maintaining modularity and user control over the analysis pipeline.