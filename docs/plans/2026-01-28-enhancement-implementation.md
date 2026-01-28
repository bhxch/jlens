# JLens MCP Server Enhancement Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Enhance JDK class inspection with `@since` tags and `javaHome` support, fix Maven dependency resolution for properties, implement granular `detailLevel` for class inspection, and improve interface field listing and integration testing.

**Architecture:** 
- Update models to support `@since` information.
- Enhance `ClassInspector` with JDK source reading (via `src.zip`) and granular `detailLevel` logic.
- Improve Maven resolvers to handle project GAV and property placeholders.
- Add robust integration tests using independent source verification.

**Tech Stack:** Java 21, Maven, ZipFileSystem, Reflection.

---

### Task 1: Update Models for `@since` Information

**Files:**
- Modify: `src/main/java/io/github/bhxch/mcp/jlens/inspector/model/ClassMetadata.java`
- Modify: `src/main/java/io/github/bhxch/mcp/jlens/inspector/model/MethodInfo.java`
- Modify: `src/main/java/io/github/bhxch/mcp/jlens/inspector/model/FieldInfo.java`

**Step 1: Add `since` field to `ClassMetadata`**
Add `private String since;` and update builder/getters.

**Step 2: Add `since` field to `MethodInfo`**
Add `private String since;` and update builder/getters.

**Step 3: Add `since` field to `FieldInfo`**
Add `private String since;` and update builder/getters.

**Step 4: Commit**
`git add src/main/java/io/github/bhxch/mcp/jlens/inspector/model/*.java`
`git commit -m "feat: 为 ClassMetadata, MethodInfo 和 FieldInfo 添加 since 字段以支持 Java 版本信息"`

---

### Task 2: Enhance `ClassInspector` for JDK Source and `@since`

**Files:**
- Modify: `src/main/java/io/github/bhxch/mcp/jlens/inspector/ClassInspector.java`
- Create: `src/main/java/io/github/bhxch/mcp/jlens/inspector/JdkSourceService.java`

**Step 1: Create `JdkSourceService`**
Implement logic to find `src.zip` in `javaHome` and extract `@since` using regex.

**Step 2: Update `ClassInspector.inspectClass`**
Incorporate `JdkSourceService` to populate `since` fields when `javaHome` is available.

**Step 3: Update `ClassInspector` method signatures**
Add `javaHome` to `inspect` methods.

**Step 4: Commit**
`git commit -m "feat: 实现 JdkSourceService 以从 JDK 源码中提取 @since 信息"`

---

### Task 3: Implement Granular `detailLevel` Support

**Files:**
- Modify: `src/main/java/io/github/bhxch/mcp/jlens/inspector/ClassInspector.java`

**Step 1: Refactor member extraction**
Implement `skeleton`, `basic`, and `full` logic in `inspectClass`.

**Step 2: Fix Interface field listing**
Ensure interfaces can list their static final fields.

**Step 3: Commit**
`git commit -m "feat: 完善 inspect_java_class 的 detailLevel 支持，并修复接口字段列表的问题"`

---

### Task 4: Fix Maven Dependency Resolution & Project GAV

**Files:**
- Modify: `src/main/java/io/github/bhxch/mcp/jlens/maven/resolver/MavenInvokerResolver.java`
- Modify: `src/main/java/io/github/bhxch/mcp/jlens/maven/resolver/MavenDirectResolver.java`

**Step 1: Improve `MavenInvokerResolver.parseModuleContext`**
Extract project GAV from Maven output or parse POM properly.

**Step 2: Improve `MavenDirectResolver`**
Add basic property placeholder support (e.g., `${project.version}`).

**Step 3: Commit**
`git commit -m "fix: 改进 Maven 依赖解析，修复项目 GAV 获取和属性占位符解析问题"`

---

### Task 5: Update Tool Definitions and Handlers

**Files:**
- Modify: `src/main/java/io/github/bhxch/mcp/jlens/server/JavaClasspathServer.java`
- Modify: `src/main/java/io/github/bhxch/mcp/jlens/server/handlers/InspectJavaClassHandler.java`

**Step 1: Add `javaHome` to tool definitions**
Update `createInspectJavaClassTool` and others.

**Step 2: Update `InspectJavaClassHandler` to handle `javaHome`**
Extract from arguments and pass to inspector.

**Step 3: Commit**
`git commit -m "feat: 在 inspect_java_class 工具中增加 javaHome 参数支持"`

---

### Task 6: Comprehensive Integration Testing

**Files:**
- Create: `src/test/java/io/github/bhxch/mcp/jlens/integration/McpInspectorIntegrationTest.java`
- Create: `src/test/java/io/github/bhxch/mcp/jlens/integration/GroundTruthProvider.java`

**Step 1: Implement `GroundTruthProvider`**
Utility to get expected metadata for testing.

**Step 2: Write Integration Tests**
Test with at least 10 classes from JDK and project.

**Step 3: Commit**
`git commit -m "test: 增加基于 MCP Inspector 的集成测试，验证 JDK 和项目模块的输出"`

---

### Task 7: Documentation and Final Polish

**Files:**
- Modify: `README.md`
- Modify: `docs/developer-docs/PROJECT_SUMMARY.md`

**Step 1: Update documentation**
Reflect all changes in Markdown files.

**Step 2: Run `file-organizer`**
Ensure all files are formatted correctly.

**Step 3: Final Commit**
`git commit -m "docs: 更新文档以反映 JDK 检查增强和 Maven 解析修复"`
