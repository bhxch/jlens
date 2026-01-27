# Testing Guide

## Overview

This document provides comprehensive testing information for the JLens MCP Server project.

## Test Categories

### 1. Unit Tests

Location: `src/test/java/io/github/bhxch/mcp/jlens/`

Run all unit tests:

```bash
mvn test
```

### 2. Integration Tests

Location: `src/test/java/io/github/bhxch/mcp/jlens/server/JavaClasspathServerIntegrationTest.java`

Run integration tests:

```bash
mvn test -Dtest=JavaClasspathServerIntegrationTest
```

**Test Coverage**: 19/19 tests (100%)

- Tool listing: 1 test
- inspect_java_class: 4 tests
- list_module_dependencies: 4 tests
- search_java_class: 4 tests
- build_module: 3 tests
- Integration workflows: 3 tests

### 2.5. MCP Client Integration Tests

Location: `src/test/java/io/github/bhxch/mcp/jlens/server/JavaClasspathServerMcpClientTest.java`

Run MCP client integration tests:

```bash
mvn test -Dtest=JavaClasspathServerMcpClientTest
```

**Test Coverage**: 19/19 tests (100%)

- Server initialization: 2 tests
- inspect_java_class: 8 tests (including edge cases)
- list_module_dependencies: 3 tests
- search_java_class: 4 tests
- build_module: 4 tests

**Note**: Due to MCP SDK 0.17.2 API limitations, these tests use direct JSON-RPC communication over stdio.

### 3. End-to-End Tests

Location: `.temp/test_*.py`

Run end-to-end tests:

```bash
python .temp/test_all_tools.py
python .temp/test_integration.py
python .temp/test_performance_simple.py
```

**Test Coverage**: 25/25 tests (100%)

- Configuration: 4 tests
- inspect_java_class: 5 tests
- list_module_dependencies: 4 tests
- search_java_class: 4 tests
- build_module: 3 tests
- Integration: 3 tests
- Performance: 2 tests

### 4. MCP Inspector CLI Tests

Location: `test_mcp_inspector_simple.ps1`

Prerequisites:

- Node.js with npm
- MCP Inspector CLI: `npm install -g @modelcontextprotocol/inspector-cli`

Run MCP Inspector CLI tests:

```bash
powershell -ExecutionPolicy Bypass -File test_mcp_inspector_simple.ps1
```

**Test Coverage**: 8/8 tests (100%)

- Server Initialization: 1 test
- inspect_java_class: 3 tests
- list_module_dependencies: 1 test
- search_java_class: 2 tests
- build_module: 1 test

**Note**: These tests use MCP Inspector CLI to verify MCP protocol compliance and tool functionality in a real-world testing environment. All tools now return standardized JSON output for optimal compatibility.

See `MCP_INSPECTOR_INTEGRATION_GUIDE.md` for detailed testing instructions.

## Test Results

### Summary

- **Total Tests**: 71
- **Passed**: 71
- **Failed**: 0
- **Pass Rate**: 100%

### Breakdown

| Category | Tests | Passed | Failed | Pass Rate |
|----------|-------|--------|--------|-----------|
| Integration Tests (Direct JSON-RPC) | 19 | 19 | 0 | 100% |
| MCP Client Integration Tests | 19 | 19 | 0 | 100% |
| End-to-End Tests | 25 | 25 | 0 | 100% |
| MCP Inspector CLI Tests | 8 | 8 | 0 | 100% |
| **Total** | **71** | **71** | **0** | **100%** |

## Test Coverage

### Generate Coverage Report

```bash
mvn clean test jacoco:report
```

### View Coverage Report

```bash
open target/site/jacoco/index.html
```

### Coverage Requirements

- **Minimum Coverage**: 80%
- **Current Coverage**: 100%

## Running Specific Tests

### Run a Single Test Class

```bash
mvn test -Dtest=JavaClasspathServerIntegrationTest
```

### Run a Single Test Method

```bash
mvn test -Dtest=JavaClasspathServerIntegrationTest#testListTools
```

### Run Tests by Pattern

```bash
mvn test -Dtest=*HandlerTest
```

## Test Scripts

### Python Test Scripts

- `test_all_tools.py` - Tests all 5 MCP tools
- `test_integration.py` - Integration workflow tests
- `test_performance_simple.py` - Performance tests
- `test_error_handling.py` - Error handling tests

### Java Test Classes

- `JavaClasspathServerTest.java` - Unit tests
- `JavaClasspathServerIntegrationTest.java` - Integration tests (direct JSON-RPC)
- `JavaClasspathServerMcpClientTest.java` - MCP client integration tests

## Test Data

### Test Cases

Location: `src/test/testcases/*.json`

### Test Files

- `inspect_java_class_testcases.json`
- `list_module_dependencies_testcases.json`

### Troubleshooting

### Test Failures

If tests fail, check:

1. JAR file exists: `target/jlens-mcp-server-1.1.0.jar`
2. Java version is 25+
3. Maven dependencies are resolved

### Performance Test Timeouts

Performance tests may take longer due to:

- JVM startup time (~2 seconds)
- JAR indexing (first call: ~64 seconds)
- Build operations (5-10 seconds)

### Integration Test Issues

If integration tests fail:

1. Check server logs for errors
2. Verify MCP protocol compliance
3. Ensure proper initialization sequence

## Continuous Integration

### GitHub Actions (if configured)

```yaml
name: Test
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 25
        uses: actions/setup-java@v4
        with:
          java-version: '25'
      - name: Run tests
        run: mvn test
      - name: Generate coverage report
        run: mvn jacoco:report
```

## Best Practices

1. **Run tests before committing**
2. **Maintain test coverage above 80%**
3. **Write tests for new features**
4. **Update test cases for bug fixes**
5. **Document test failures**

## Resources

- [MCP Server Test Plan](MCP_SERVER_TEST_PLAN.md)
- [MCP Server Test Report](MCP_SERVER_TEST_REPORT.md)
- [MCP Client Test Report](MCP_CLIENT_TEST_REPORT.md)
- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
