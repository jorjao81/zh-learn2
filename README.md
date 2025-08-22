# ZH Learn - Chinese Learning Tool

Experimenting how to ensure that AI agentic coding results into reasonably maintainable
code.

## Prerequisites

- Java 24+ with GraalVM (for native compilation)
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