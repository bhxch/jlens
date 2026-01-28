# JLens MCP Server Enhancement Design - 2026-01-28

## 1. JDK Class Inspection Enhancement

### Architecture & Approach
We will enhance `ClassInspector` to support an optional `javaHome` parameter. When provided, the inspector will look for the JDK's source archive (`lib/src.zip` for JDK 9+ or `src.zip` for JDK 8) to extract Javadoc and specifically the `@since` tag.

- **Component**: `ClassInspector` will be updated to include a method `extractJdkSource(String className, Path javaHome)`.
- **Component**: `ClassMetadata`, `MethodInfo`, and `FieldInfo` models will be updated to include a `since` field (String).
- **Data Flow**: `InspectJavaClassHandler` receives `javaHome` -> passes to `ClassInspector` -> `ClassInspector` resolves source -> parses `@since` -> populates models.

### Implementation Details
- Use `ZipFileSystem` to read `src.zip` without full extraction.
- A simple regex-based Javadoc parser will be used to find `@since` tags.
- Update `JavaClasspathServer` tool definition to include `javaHome` and a detailed description prompting the AI to use it for JDK classes.

---

## 2. Maven Dependency Resolution Fix

### Architecture & Approach
The current `MavenDirectResolver` is too simplistic. We will improve Maven resolution by:
1. Ensuring `MavenInvokerResolver` is preferred and correctly extracts project GAV.
2. Improving `MavenDirectResolver` to at least handle properties defined in the same POM.
3. Adding a new `MavenEffectivePomResolver` (or upgrading `MavenInvokerResolver`) that uses `mvn help:effective-pom` to get a fully resolved POM.

### Implementation Details
- `MavenInvokerResolver`: Update `parseModuleContext` to extract `<groupId>`, `<artifactId>`, and `<version>` from the Maven output or the POM file directly after resolution.
- `MavenProject`: Add support for property interpolation.
- Resolve the "unknown" GAV issue by parsing the POM using a more robust method (e.g., `MavenXpp3Reader` from Maven Model if available, or better regex).

---

## 3. `inspect_java_class` `detailLevel` Support

### Architecture & Approach
We will fully implement the `skeleton`, `basic`, and `full` detail levels in `ClassInspector`.

- **Skeleton**: Basic class info (name, modifiers, superclass, interfaces).
- **Basic**: Skeleton + Public/Protected members signatures. No private members, no method body hints.
- **Full**: Everything, including private members, method body hints (if decompiler is active), and `@since` info.

### Implementation Details
- Refactor `inspectClass` in `ClassInspector` to use a switch/if based on `DetailLevel`.
- Add unit tests for each level ensuring the amount of data returned is correct.

---

## 4. `list_class_fields` Interface Handling

### Architecture & Approach
Interfaces in Java can have fields (always `public static final`). The current implementation might be failing if it expects only classes. We will ensure `ClassInspector` handles `clazz.isInterface()` correctly when listing fields.

### Implementation Details
- Check for `clazz.isInterface()` and allow field listing.
- Ensure the error reported by the user is caught and handled (e.g., if `getDeclaredFields()` behaves unexpectedly on some proxy classes or interfaces).

---

## 5. MCP Inspector Integration Tests

### Architecture & Approach
Create a comprehensive test suite that validates `jlens` output against "Ground Truth" obtained from source files.

1. **Source Provider**: A utility to download/read source files for specific GAVs or JDK classes.
2. **Metadata Extractor**: A simple source-based parser (using regex or a lightweight parser) to extract expected methods/fields/since tags.
3. **Validator**: Runs `jlens`, compares output with Ground Truth, and generates a report in `.temp`.

---

## 6. Documentation & Commit Strategy

- **Docs**: Update `README.md`, `PROJECT_SUMMARY.md`, and tool descriptions in `JavaClasspathServer.java`.
- **Cleanup**: Use `file-organizer` to ensure consistent formatting and structure.
- **Commits**: Atomic commits for:
  - Model updates (`since` field).
  - `ClassInspector` JDK enhancement.
  - Maven resolution fixes.
  - `detailLevel` implementation.
  - Interface field listing fix.
  - Integration tests.
  - Documentation updates.
