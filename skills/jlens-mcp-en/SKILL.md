---
name: jlens-mcp-en
description: Professional-grade Java codebase analysis and Maven dependency management skill. Triggers when: (1) Analyzing Java class structures with multi-version isolation in multi-module projects, (2) Deep parsing Maven dependency trees and troubleshooting version conflicts, (3) Searching for classes in massive Jar files using cursor-based pagination, (4) Automating Maven build tasks, (5) Intelligently identifying local source code vs. third-party libraries. This skill guides the Agent to prefer registered MCP services and provides a command-line Fallback mechanism.
---

# JLens MCP Expert Navigation Guide (V1.1.0)

JLens is a Model Context Protocol (MCP) server designed for AI Agents. It provides deep understanding of Java projects through real reflection analysis and bytecode parsing. Unlike simple text search, JLens can identify class inheritance, method signatures, visibility modifiers, and complex Maven dependency topologies.

## 1. Core Tools and Interaction Details

### 1.1 Class Inspection (inspect_java_class)
**Interaction Logic**:
- **State Machine Responses**:
    - `status: "SUCCESS"`: Parsing successful. Read `decompiledSource` for logic, and `methods`/`fields` for structure.
    - `status: "LOCAL_SOURCE"`: **Important!** The target class belongs to the current workspace. Ignore metadata and directly read the source code using the `read_file` tool via the provided `sourceFile` path.
    - `status: "NOT_FOUND"`: Class does not exist. Check the `suggestion` field; you might need to run `build_module` first.
- **Key Parameters**:
    - `bypassCache`: If class structure seems outdated (e.g., after recent source changes), set to `true` to bypass GAV cache for real-time parsing.

### 1.2 Paginated Search (search_java_class)
**Pagination Protocol**:
- **Result Parsing**: If `hasMore` is `true` in the response, you must record the `nextCursor`.
- **Subsequent Requests**: Pass `cursor: "<nextCursor>"` in the next call until `hasMore` is `false`. Results are sorted by fully qualified name for stability.

### 1.3 Dependency Analysis (list_module_dependencies)
**Metadata Parsing**:
- Returns a standard Maven dependency model. Focus on the `scope` field (e.g., `test`, `provided`) to determine if a class is available at runtime.

---

## 2. Task-Driven Workflows

### Scenario A: Troubleshooting NoClassDefFoundError or NoSuchMethodError
1.  **Locate Dependencies**: Call `list_module_dependencies` to check if multiple versions of Jars contain conflicting classes.
2.  **Version-Isolated Inspection**: Use JLens's multi-version isolation feature to load the class for different module contexts respectively. Compare `methods` lists to find missing methods.
3.  **Build Verification**: After modifying pom.xml, call `build_module` to refresh the local repository and verify the build passes.

### Scenario B: Massive Code Refactoring Support
1.  **Global Search**: Use `search_java_class` with wildcards (e.g., `com.api.*Service`) to find all relevant interfaces.
2.  **Batch Inspection**: Use `list_class_fields` to quickly extract private fields of all implementation classes to evaluate state storage.
3.  **Source Navigation**: When `LOCAL_SOURCE` is detected, automatically switch to `read_file` to modify code.

---

## 3. Execution Strategy

### 3.1 Protocol Priority
1.  **Probe Registered Service**: First check if `jlens-mcp-server` exists in the environment.
2.  **Direct Request**: If present, use standard MCP `call_tool` requests.

### 3.2 Command-Line Fallback (When no registered service)
If no service is found, the Agent must autonomously use shell tools to execute:

**NPM Mode (Preferred)**:
```powershell
npx -y @bhxch/jlens-mcp-server --tool <tool_name> --args '<json_arguments>'
```

**Python Mode**:
```bash
uvx jlens-mcp-server --tool <tool_name> --args '<json_arguments>'
```

---

## 4. Performance and Limitations
- **GAV Cache**: JLens shares cache by `GroupId:ArtifactId:Version`. Parsing results for third-party libraries (like Spring) are shared across projects, usually responding within 100ms.
- **Initial Indexing**: The first run of `search_java_class` on large projects may trigger full indexing (~60s). Subsequent requests are millisecond-level.
- **JDK Requirement**: Underlying environment must be Java 25+.
