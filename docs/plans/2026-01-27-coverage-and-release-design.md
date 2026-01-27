# JLens MCP Server Coverage Improvement and Release 1.1.1 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Increase unit test line coverage to 85%+, resolve Java 25 compatibility issues, and re-tag version 1.1.1.

**Architecture:** Use Mockito for high-isolation unit testing of Handlers and core server logic. Maintain environment compatibility via Byte Buddy experimental flags in Maven configuration.

**Tech Stack:** Java 21/25, Maven, Mockito 5.15.2, Byte Buddy 1.15.11, JaCoCo.

---

### Task 1: Environment & Infrastructure Setup

**Files:**
- Modify: `pom.xml`

**Step 1: Update dependencies and add Byte Buddy experimental flag**

Update `mockito.version` to `5.15.2`.
Add `<byte-buddy.version>1.15.11</byte-buddy.version>` to properties.
Add `net.bytebuddy.experimental=true` to `maven-surefire-plugin` configuration.
Set `coverage.minimum` to `85`.

**Step 2: Verify environment with existing test**

Run: `mvn test -Dtest=HandlersExtraTest`
Expected: PASS (verifies Java 25 Mockito fix)

**Step 3: Commit infrastructure changes**

```bash
git add pom.xml
git commit -m "chore: update mockito to 5.15.2 and enable byte-buddy experimental for Java 25"
```

---

### Task 2: Implement Unit Tests for Handlers

**Files:**
- Create: `src/test/java/io/github/bhxch/mcp/jlens/server/handlers/SearchJavaClassHandlerUnitTest.java`
- Create: `src/test/java/io/github/bhxch/mcp/jlens/server/handlers/BuildModuleHandlerUnitTest.java`
- Create: `src/test/java/io/github/bhxch/mcp/jlens/server/handlers/ListModuleDependenciesHandlerUnitTest.java`

**Step 1: Implement SearchJavaClassHandler unit tests**
Cover indexing logic, result filtering, and error handling with mocked dependencies.

**Step 2: Implement BuildModuleHandler unit tests**
Cover goal parsing and MavenBuilder interaction.

**Step 3: Implement ListModuleDependenciesHandler unit tests**
Cover dependency resolution and scope filtering.

**Step 4: Verify coverage increment**
Run: `mvn clean test jacoco:report`
Expected: Line coverage significantly increased (Targeting >60% after this task).

---

### Task 3: Implement Core Server & Utils Tests

**Files:**
- Create: `src/test/java/io/github/bhxch/mcp/jlens/server/JavaClasspathServerUnitTest.java`
- Create: `src/test/java/io/github/bhxch/mcp/jlens/inspector/model/InspectorModelTest.java` (Covering MethodInfo, FieldInfo, etc.)

**Step 1: Implement JavaClasspathServer tests**
Mock `McpServer` and verify tool definitions and registration.

**Step 2: Implement model builder tests**
Cover 100% of Builder patterns in `inspector.model` package.

**Step 3: Final coverage verification**
Run: `mvn clean test jacoco:report`
Expected: Line coverage >= 85%.

---

### Task 4: File Organization & Versioning

**Files:**
- Modify: `pom.xml`, `package.json`
- Action: @file-organizer

**Step 1: Run file organization**
Execute @file-organizer to ensure project structure and documentation consistency.

**Step 2: Version alignment**
Ensure `pom.xml` and `package.json` are both at `1.1.1`.

**Step 3: Final check for absolute paths**
Search for any absolute paths in the codebase.
Expected: None.

---

### Task 5: Release Tagging

**Step 1: Delete old tag (if exists)**
`git tag -d v1.1.1`

**Step 2: Commit final changes**
`git add . && git commit -m "release: version 1.1.1 with 85%+ test coverage"`

**Step 3: Create new tag**
`git tag -a v1.1.1 -m "Release version 1.1.1 with improved test coverage (85%+)"`
