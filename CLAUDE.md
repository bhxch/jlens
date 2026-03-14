# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

JLens MCP Server is a Model Context Protocol (MCP) server that provides AI agents with the ability to inspect Java classes, analyze Maven dependencies, and understand Java codebases. It implements 5 MCP tools using reflection, bytecode analysis, and multiple decompiler implementations.

## Build Commands

### Build the Project
```bash
# Recommended for JDK 25+ (skip tests due to JUnit 5 engine compatibility issues)
mvn clean package -DskipTests

# This creates: target/jlens-mcp-server-1.1.2.jar
```

### Running Tests
```bash
# Run all tests (may fail on JDK 25+ due to test engine compatibility)
mvn test

# Run a specific test class
mvn test -Dtest=ClassNameTest

# Run a specific test method
mvn test -Dtest=ClassNameTest#testMethodName

# Generate coverage report
mvn test jacoco:report
# Report available at: target/site/jacoco/index.html
```

### Running the Server
```bash
# Run directly with Java
java -jar target/jlens-mcp-server-1.1.2.jar

# Run with environment variables
JLENS_JAVA_HOME="/path/to/jdk-25" java -jar target/jlens-mcp-server-1.1.2.jar

# Run via npx (requires Node.js)
npx -y @bhxch/jlens-mcp-server

# Run via uvx (requires Python/uv)
uvx jlens-mcp-server
```

## Architecture

### High-Level Design

The server follows a **Handler Pattern** where each MCP tool has a dedicated handler class:

```
JavaClasspathServer (Main MCP Server)
├── MCP SDK Integration (io.modelcontextprotocol.sdk 0.17.2)
├── Tool Handlers (server/handlers/)
│   ├── InspectJavaClassHandler
│   ├── ListClassFieldsHandler
│   ├── ListModuleDependenciesHandler
│   ├── SearchJavaClassHandler
│   └── BuildModuleHandler
├── Core Services
│   ├── ClassInspector - Reflection-based class analysis with @since extraction
│   ├── DependencyManager - Maven dependency resolution
│   ├── MavenBuilder - Maven build execution
│   └── PackageMappingResolver - Package to module mapping
└── Supporting Services
    ├── CacheManager - GAV-based global caching (Caffeine)
    ├── ClassLoaderManager - Dynamic version isolation per module
    ├── DecompilerFactory - Pluggable decompiler selection
    └── JdkSourceService - JDK src.zip parsing for @since tags
```

### Key Components

**MCP Tools (5 total):**

1. **inspect_java_class** - Inspect any Java class with reflection, supports detail levels (skeleton/basic/full) and JDK @since extraction via javaHome
2. **list_class_fields** - List class fields with visibility filtering (supports interfaces)
3. **list_module_dependencies** - List Maven dependencies with property placeholder resolution
4. **search_java_class** - Search classes with cursor-based pagination across packages
5. **build_module** - Build Maven modules and download dependencies

**Decompilers (Strategy Pattern):**
- Vineflower (1.11.2) - Default
- CFR (0.152) - Alternative
- Fernflower (242.23655.110) - Alternative

**Maven Resolvers (Factory Pattern):**
- MavenInvokerResolver - Preferred, uses `mvn` command
- MavenDirectResolver - Fallback, parses POM directly with basic property support

### Critical Design Patterns

1. **Handler Pattern** - Each tool has a dedicated handler class in `server/handlers/`
2. **Version Isolation** - Dynamic URLClassLoader per ModuleContext to handle multi-version dependencies
3. **GAV-based Caching** - Global cache keyed by groupId:artifactId:version to avoid redundant inspection
4. **Cursor Pagination** - Stable search results using Base64-encoded cursors
5. **Virtual Threads** - Java 21+ concurrent processing for parallel operations

## Project Structure

```
jlens/
├── src/main/java/io/github/bhxch/mcp/jlens/
│   ├── Main.java                          # Entry point
│   ├── server/
│   │   ├── JavaClasspathServer.java       # MCP server setup & tool definitions
│   │   └── handlers/                      # Tool handlers (one per MCP tool)
│   ├── inspector/
│   │   ├── ClassInspector.java            # Core reflection-based inspector
│   │   ├── JdkSourceService.java          # JDK src.zip parser for @since
│   │   └── model/                         # Metadata models
│   ├── dependency/
│   │   ├── DependencyManager.java         # Dependency resolution orchestration
│   │   └── MavenBuilder.java              # Maven build execution
│   ├── maven/
│   │   ├── model/                         # Maven models (MavenProject, ModuleContext)
│   │   └── resolver/                      # Resolver implementations
│   ├── classpath/
│   │   ├── ClassLoaderManager.java        # Dynamic ClassLoader management
│   │   └── PackageMappingResolver.java    # Package-to-JAR mapping
│   ├── cache/                             # Caffeine-based caching
│   ├── decompiler/                        # Decompiler abstraction & implementations
│   └── config/                            # Configuration classes
├── src/test/java/                         # Test suites (unit, integration, MCP client)
├── docs/
│   ├── user-guide/                        # User documentation
│   ├── developer-docs/                    # Architecture & design docs
│   └── plans/                             # Design plans for enhancements
├── config/                                # MCP Inspector configs
├── scripts/                               # Test automation scripts (PowerShell)
├── jlens_mcp_server/                      # Python wrapper for PyPI distribution
└── bin/                                   # Node.js wrapper for npm distribution
```

## Development Workflow

### Requirements
- **Java**: JDK 25 or higher
- **Maven**: 3.9 or higher
- **Node.js**: For npm package testing and distribution
- **Python**: For PyPI package testing and distribution

### Code Standards
- **Indentation**: 4 spaces
- **Language**: English for code, comments, and documentation
- **Testing**: Maintain ≥85% code coverage (enforced by JaCoCo)
- **Commit messages**: Chinese language, Angular format (`<type>(<scope>): <subject>`)

### Commit Message Format
Follow Angular commit convention with Chinese messages:
- `feat(inspector): 添加 JDK @since 信息提取支持`
- `fix(maven): 修复属性占位符解析问题`
- `test(handlers): 增加集成测试覆盖`
- `docs(user-guide): 更新工具使用说明`

Types: `feat`, `fix`, `docs`, `ref`, `test`, `build`

### Testing Strategy

**Test Categories:**
- **Unit Tests** (`src/test/java/.../unit/`) - Test individual components in isolation
- **Integration Tests** (`src/test/java/.../integration/`) - Test MCP protocol compliance
- **Handler Tests** (`src/test/java/.../handlers/`) - Test tool handlers end-to-end
- **Server Tests** (`src/test/java/.../server/`) - Test server initialization and lifecycle

**Note on JDK 25**: Test execution may fail due to JUnit 5 test engine compatibility. Use `-DskipTests` for builds.

### Integration Testing with MCP Inspector
```bash
# Run MCP Inspector tests (PowerShell scripts in scripts/)
pwsh scripts/test_mcp_inspector_final.ps1
```

## Important Implementation Details

### Local Source Detection
When inspecting a class that exists in the local workspace (not a dependency), `ClassInspector` returns a `LOCAL_SOURCE` status with a suggestion to use `read_file` directly. This prevents stale decompiled output.

### JDK @since Extraction
To get accurate `@since` information for JDK classes:
1. Tool handler receives `javaHome` parameter
2. `JdkSourceService` locates `src.zip` in JDK installation
3. Uses `ZipFileSystem` to read source without extraction
4. Parses Javadoc comments with regex to extract `@since` tags
5. Populates `since` field in `MethodInfo` and `FieldInfo`

### Detail Levels
- **skeleton**: Class name, modifiers, superclass, interfaces only
- **basic**: Skeleton + public/protected members (no private, no body hints)
- **full**: Everything including private members and decompiler hints

### Maven Property Resolution
`MavenDirectResolver` handles basic property placeholders (e.g., `${project.version}`) defined in the same POM. For complex resolution, `MavenInvokerResolver` uses actual Maven execution.

### ClassLoader Isolation
Each `ModuleContext` gets its own `URLClassLoader` to handle scenarios where different modules depend on different versions of the same library.

## Documentation Locations

- **User Documentation**: `docs/user-guide/` - README, TESTING guides in EN/CN
- **Developer Documentation**: `docs/developer-docs/` - Architecture, integration guides
- **Design Plans**: `docs/plans/` - Enhancement designs and implementation plans
- **Test Reports**: `docs/developer-docs/reports/` - Detailed test results

## Common Development Tasks

### Adding a New MCP Tool

1. Create handler class in `server/handlers/` implementing the tool logic
2. Add tool definition method in `JavaClasspathServer.java` (e.g., `createNewTool()`)
3. Register handler in `JavaClasspathServer` constructor with `.toolCall()`
4. Add unit tests in `src/test/java/.../handlers/`
5. Update documentation in `docs/user-guide/` (EN and CN versions)

### Modifying Class Inspection Logic

- Core logic: `inspector/ClassInspector.java`
- Model classes: `inspector/model/` (ClassMetadata, MethodInfo, FieldInfo, ParameterInfo)
- JDK support: `inspector/JdkSourceService.java`

### Working with Maven Resolution

- Resolver factory: `maven/resolver/MavenResolverFactory.java`
- Direct resolver: `maven/resolver/MavenDirectResolver.java` (lightweight, basic properties)
- Invoker resolver: `maven/resolver/MavenInvokerResolver.java` (full Maven execution)

### Cache Management

- Global cache: `cache/CacheManager.java`
- Module-specific cache: `cache/ModuleCache.java`
- Class metadata cache: `cache/ClassMetadataCache.java`

All caches use Caffeine with configurable TTL and size limits.

## Distribution

The project is distributed through three channels:

1. **Maven Central**: Java JAR artifact
2. **npm**: `@bhxch/jlens-mcp-server` - Node.js wrapper
3. **PyPI**: `jlens-mcp-server` - Python wrapper

All wrappers delegate to the same Java JAR file, which must be rebuilt and included before publishing.
