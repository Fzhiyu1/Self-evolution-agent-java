# Self-Evolution Agent Java

> **Note**: This is an experimental project for validating the concept of "LLM-driven code self-evolution".
> Currently only implements sorting algorithm evolution scenarios. Not intended for production use.

An experimental Java self-evolution algorithm engine that uses LLM-driven code mutation and natural selection to automatically evolve sorting algorithms from O(n²) to O(n log n) at runtime.

## Overview

This project (codename: Project Quine) builds a Java runtime environment for code self-evolution. The system uses LLM to mutate source code, then automatically selects better implementations through dynamic compilation, hot-loading, and performance evaluation - all without restarting the JVM.

### Key Features

- **Dynamic Compilation**: Compiles Java source code in memory using `javax.tools.JavaCompiler` API
- **Hot-Loading**: Creates new `URLClassLoader` instances for each generation to enable class hot-swapping
- **LLM Mutation**: Integrates with Qwen API (OpenAI-compatible) for code mutation via large language models
- **Natural Selection**: Only mutations that pass correctness verification and show better performance are retained

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Evolution Loop                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌──────────┐    ┌──────────┐    ┌──────────┐             │
│   │ Mutator  │───>│ Compiler │───>│Evaluator │             │
│   │ (LLM)    │    │(Dynamic) │    │(Selector)│             │
│   └──────────┘    └──────────┘    └──────────┘             │
│        │                               │                    │
│        │         ┌──────────┐          │                    │
│        └────────>│  Source  │<─────────┘                    │
│                  │  Code    │   (Replace if better)         │
│                  │ (Genome) │                               │
│                  └──────────┘                               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Project Structure

```
Self-evolution-agent-java/
├── src/main/java/com/quine/
│   ├── Main.java                 # Entry point, evolution loop control
│   ├── ResetToOrigin.java        # Reset tool, restore initial state
│   ├── core/
│   │   ├── TaskSolver.java       # Genome interface (immutable)
│   │   └── Evaluator.java        # Evaluator: compile/verify/benchmark
│   ├── sandbox/
│   │   └── TargetSubject.java    # Target code being evolved
│   └── utils/
│       ├── CompilerUtils.java    # Dynamic compilation utilities
│       └── LLMClient.java        # LLM API client
├── target/
│   └── generations/              # Evolution code backups
├── pom.xml
└── README.md
```

## Core Modules

### 1. Genome Interface (TaskSolver)

The immutable interface that all mutated code must implement:

```java
public interface TaskSolver {
    int[] solve(int[] input);
}
```

### 2. Evaluator

Responsible for compilation, verification, and performance testing:

- **Forbidden API Check**: Blocks `Arrays.sort`, `Collections.sort`, and other standard library sorting methods
- **Correctness Verification**: 4 test cases to verify sorting results
- **Performance Testing**: 3 warmup runs + 5 measurement runs averaged
- **Timeout Protection**: 10-second forced timeout to prevent infinite loops

### 3. LLM Client

Calls OpenAI-compatible API for code mutation:

- Enforces `TaskSolver` interface implementation
- Class name must remain `TargetSubject`
- Prohibits standard library sorting methods
- Auto-cleans Markdown code blocks from responses

### 4. Dynamic Compiler (CompilerUtils)

Uses JDK built-in compiler API:

- Compiles source code strings in memory
- Outputs to `target/classes` directory
- Captures and reports compilation errors

## Quick Start

### Requirements

- Java 21+
- Maven 3.6+
- OpenAI-compatible API Key (e.g., DashScope, OpenAI, Ollama)

### Configure API Key

**Option 1: Configuration File (Recommended)**

```bash
# Copy example config file
cp config.properties.example config.properties

# Edit config.properties with your settings
API_URL=https://your-api-endpoint/v1/chat/completions
API_KEY=your-api-key-here
MODEL=your-model-name
```

**Option 2: Environment Variable**

Windows (PowerShell):
```powershell
$env:API_KEY="your-api-key-here"
```

Windows (CMD):
```cmd
set API_KEY=your-api-key-here
```

Linux/Mac:
```bash
export API_KEY="your-api-key-here"
```

### Build

```bash
mvn clean compile
```

### Run Evolution

```bash
mvn exec:java -Dexec.mainClass="com.quine.Main"
```

### Reset to Initial State

```bash
mvn exec:java -Dexec.mainClass="com.quine.ResetToOrigin"
```

## Configuration

### Evolution Parameters

Configure in `Main.java`:

| Parameter | Default | Description |
|-----------|---------|-------------|
| `TEST_DATA_SIZE` | 10000 | Test data size |
| `MAX_GENERATIONS` | 50 | Maximum evolution generations |

### LLM API Configuration

Configure in `config.properties`:

| Parameter | Description |
|-----------|-------------|
| `API_URL` | OpenAI-compatible API endpoint |
| `API_KEY` | Your API key |
| `MODEL` | Model name (e.g., `gpt-4`, `qwen3-coder-plus`) |

## Evolution Process

1. **Genesis (Initialization)**
   - Load initial bubble sort implementation
   - Generate random test data
   - Record baseline performance

2. **Mutation**
   - Read current source code
   - Call LLM to generate mutated code

3. **Compilation**
   - Dynamically compile mutated code
   - Skip this round if compilation fails

4. **Verification**
   - Correctness testing
   - Performance benchmarking

5. **Selection**
   - Replace source file if performance is better
   - Backup successful mutations to `target/generations/`

## Evolution Results Example

From a 200-generation experiment:

| Generation | Algorithm | Time Complexity | Performance |
|------------|-----------|-----------------|-------------|
| Initial | Bubble Sort | O(n²) | Baseline |
| Gen 1 | Quick Sort | O(n log n) | +25% |
| Gen 14 | Heap Sort | O(n log n) | +30% |
| Gen 145 | Introsort | O(n log n) | +34.2% |

**Introsort** is a hybrid sorting algorithm that combines:
- QuickSort for average cases
- HeapSort when recursion depth exceeds threshold
- InsertionSort for small subarrays

## Safety Measures

- **Infinite Loop Protection**: `ExecutorService` + `Future.get(timeout)` for forced timeout
- **Hallucination Prevention**: Prompt restricts to JDK standard library only
- **ClassLoader Leak Prevention**: Clean up ClassLoader after each generation test
- **Code Format Protection**: Regex cleaning of Markdown blocks from LLM responses

## Tech Stack

- Java 21+
- OkHttp 4.12.0 (HTTP client)
- Gson 2.10.1 (JSON parsing)
- javax.tools (Dynamic compilation)


## Theoretical Background

This project stands on the shoulders of giants:

| Source | Core Idea | Application |
|--------|-----------|-------------|
| **Hofstadter** "I Am a Strange Loop" | Self-referential systems | Code evolving code |
| **Sakana AI** Evolutionary Model Merging | Evolution as efficient search | LLM-guided mutation |
| **Genetic Improvement (GI)** | Automated program repair | Code mutation strategies |

## License

MIT License

---

> *"The compiler is a zero-cost critic - it provides instant, deterministic, high-density feedback signals."*
