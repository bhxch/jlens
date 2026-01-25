# Testing Guide

## Test Coverage

The project includes comprehensive unit tests for all major components:

### Unit Tests

- **Config Module** (`ServerConfigTest`)
  - Default configuration values
  - Command line argument parsing
  - Maven config creation

- **Maven Module** (`MavenDirectResolverTest`)
  - POM file parsing
  - Dependency extraction
  - Module context resolution

- **Cache Module** (`CacheManagerTest`)
  - Module context caching
  - Class metadata caching
  - Concurrent access handling
  - Cache invalidation

- **Concurrent Module** (`VirtualThreadExecutorTest`)
  - I/O-bound task execution
  - CPU-bound task execution
  - Timeout handling
  - Shutdown behavior

- **MCP Module** (`ToolRegistryTest`)
  - Tool registration and retrieval
  - Tool execution
  - Disabled tool handling

- **Inspector Module** (`ClassInspectorTest`)
  - Simple class names
  - Fully qualified class names
  - Nested classes
  - Different detail levels

## Running Tests

### Prerequisites

- Java 25+
- Maven 3.9+

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=ServerConfigTest
```

### Run Tests with Coverage Report

```bash
mvn clean test jacoco:report
```

Coverage report will be generated in `target/site/jacoco/index.html`.

### Run Integration Tests

```bash
mvn verify
```

## Test Coverage Requirements

The project requires a minimum of 80% code coverage for both:

- Instruction coverage
- Branch coverage

To check coverage:

```bash
mvn jacoco:check
```

## Test Structure

```
src/test/java/io/github/bhxch/mcp/javastub/
├── unit/
│   ├── config/
│   │   └── ServerConfigTest.java
│   ├── maven/
│   │   └── MavenDirectResolverTest.java
│   ├── cache/
│   │   └── CacheManagerTest.java
│   ├── concurrent/
│   │   └── VirtualThreadExecutorTest.java
│   ├── inspector/
│   │   └── ClassInspectorTest.java
│   └── mcp/
│       └── ToolRegistryTest.java
├── integration/
│   └── (Integration tests to be added)
└── performance/
    └── (Performance tests to be added)
```

## Adding New Tests

1. Create test class in appropriate package under `src/test/java`
2. Use JUnit 5 annotations (`@Test`, `@DisplayName`, `@BeforeEach`)
3. Follow naming convention: `*Test.java`
4. Use `@TempDir` for temporary file operations
5. Use `@Timeout` for tests with time constraints

## CI/CD Integration

The project includes GitHub Actions workflow for:

- Building the project
- Running tests
- Checking code coverage
- Running SonarQube analysis

See `.github/workflows/ci.yml` for details.
