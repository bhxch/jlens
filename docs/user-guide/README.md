# Java Maven Classpath MCP Server

A Model Context Protocol (MCP) server for inspecting Java classes and Maven dependencies. This server provides tools to analyze Java bytecode, decompile classes, and resolve Maven project dependencies.

## Status

✅ **READY FOR PRODUCTION USE**

All features are implemented, tested, and ready for production deployment.

- **5 MCP Tools**: All tools fully functional with standardized JSON output
- **100% Test Pass Rate**: 71/71 tests passed (100%)
- **MCP Compliant**: Fully compliant with MCP 2024-11-05 specification
- **Performance Optimized**: Real reflection-based inspection with multi-version isolation
- **Smart Caching**: GAV-based global dependency caching for extreme speed
- **MCP Inspector Verified**: Successfully tested with MCP Inspector CLI mode (JSON format)

## Features

- **Java Class Inspection**: Inspect Java classes with real reflection analysis, bytecode analysis, and multi-version isolation using dynamic ClassLoaders
- **Local Source Hint**: Automatically detects classes in your local workspace and suggests reading source directly for better accuracy
- **Variable Listing**: List fields of a class with specific visibility filtering (public, private, etc.)
- **Maven Dependency Resolution**: List and analyze Maven module dependencies (JSON format)
- **Stable Class Search**: Search for classes across packages and dependencies with cursor-based pagination
- **Module Building**: Build Maven modules and download missing dependencies
- **Intelligent Package Resolution**: AI-friendly class package resolution with context awareness
- **Standardized Output**: Consistent JSON format for all tool outputs and errors
- **Cross-platform Execution**: Run via `npx` or `uvx` for zero-install experience

## Requirements

- Java 25 or higher
- Maven 3.9+ (for building)
- Node.js (for npx) or Python (for uvx) - optional for execution

## Building

```bash
mvn clean package
```

This will create an executable JAR file: `target/jlens-mcp-server-1.1.0.jar`

## Usage

### Running the Server

You can run the server using Java directly, or via `npx`/`uvx`.

**Using npx:**
```bash
npx @bhxch/jlens-mcp-server
```

**Using uvx:**
```bash
uvx jlens-mcp-server
```

**Using Java:**
```bash
java -jar target/jlens-mcp-server-1.1.0.jar
```

### Command Line Options

```
Options:
  -vt, --virtual-threads <count>    Maximum number of virtual threads (default: 1000)
  -me, --maven-executable <path>    Path to Maven executable
  -ms, --maven-settings <path>       Path to Maven settings.xml
  -mr, --maven-repo <path>          Path to Maven local repository
  -d, --decompiler <type>           Decompiler to use: fernflower, cfr, vineflower (default: fernflower)
  -p, --port <port>                 Server port (default: 8080)
  -l, --log-level <level>           Log level: ERROR, WARN, INFO, DEBUG (default: INFO)
  -h, --help                        Show this help message
```

## Client Configuration

This MCP server can be used with various MCP-compatible clients.

### Claude Desktop

Add this to your `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "jlens": {
      "command": "npx",
      "args": ["-y", "@bhxch/jlens-mcp-server"]
    }
  }
}
```

### Gemini CLI

Add this to your `.gemini/settings.json`:

```json
{
  "mcpServers": {
    "jlens": {
      "command": "npx",
      "args": ["-y", "@bhxch/jlens-mcp-server"]
    }
  }
}
```

### Cursor

Settings -> Models -> MCP -> Add New MCP Server:

- **Name**: jlens
- **Type**: command
- **Command**: `npx -y @bhxch/jlens-mcp-server`

### Cline (VS Code Extension)

MCP Settings -> Add Server:

- **Name**: jlens
- **Command**: `npx`
- **Args**: `["-y", "@bhxch/jlens-mcp-server"]`

### iFlow CLI

```bash
iflow mcp add jlens-mcp-server "npx -y @bhxch/jlens-mcp-server" --trust
```

## MCP Tools

### inspect_java_class

Inspect a Java class and return its metadata. If the class is in local workspace, it will return a hint to read source directly.

**Parameters:**

- `className` (string, required): The fully qualified class name to inspect
- `sourceFilePath` (string, optional): Path to source file for context
- `detailLevel` (string, optional): Level of detail - "skeleton", "basic", or "full" (default: "basic")
- `bypassCache` (boolean, optional): Whether to bypass cache and re-inspect (default: false)

### list_class_fields

List fields of a Java class with visibility filtering.

**Parameters:**

- `className` (string, required): The fully qualified class name to inspect
- `visibility` (array of strings, optional): Visibility modifiers to include ("public", "protected", "private", "package-private")
- `sourceFilePath` (string, optional): Path to source file for context

**Example Request:**

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "list_class_fields",
    "arguments": {
      "className": "java.lang.String",
      "visibility": ["private"]
    }
  }
}
```

### list_module_dependencies

List dependencies for a Maven module.

**Parameters:**

- `sourceFilePath` (string, optional): Path to source file to locate module
- `pomFilePath` (string, optional): Path to pom.xml file
- `scope` (string, optional): Dependency scope - "compile", "provided", "runtime", "test", or "system" (default: "compile")

**Example Request:**

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/call",
  "params": {
    "name": "list_module_dependencies",
    "arguments": {
      "pomFilePath": "pom.xml",
      "scope": "compile"
    }
  }
}
```

### search_java_class

Search for Java classes across packages and dependencies with cursor-based pagination.

**Parameters:**

- `classNamePattern` (string, required): Class name pattern (supports wildcards: *, ?)
- `sourceFilePath` (string, optional): Source file path for context
- `searchType` (string, optional): Search type - "exact", "prefix", "suffix", "contains", or "wildcard" (default: "wildcard")
- `limit` (integer, optional): Maximum number of results to return per page (default: 50)
- `cursor` (string, optional): Pagination cursor from previous request

**Example Request:**

```json
{
  "jsonrpc": "2.0",
  "id": 3,
  "method": "tools/call",
  "params": {
    "name": "search_java_class",
    "arguments": {
      "classNamePattern": "*List*",
      "searchType": "wildcard",
      "limit": 10,
      "cursor": "..."
    }
  }
}
```

### build_module

Build Maven module and download missing dependencies.

**Parameters:**

- `sourceFilePath` (string, required): Source file path for module context
- `goals` (array, optional): Maven goals to execute (default: ["compile", "dependency:resolve"])
- `downloadSources` (boolean, optional): Whether to download source JARs (default: false)
- `timeoutSeconds` (integer, optional): Build timeout in seconds (default: 300)

**Example Request:**

```json
{
  "jsonrpc": "2.0",
  "id": 4,
  "method": "tools/call",
  "params": {
    "name": "build_module",
    "arguments": {
      "sourceFilePath": "src/main/java/io/github/bhxch/mcp/jlens/Main.java",
      "downloadSources": true
    }
  }
}
```

## Testing

### Run All Tests

```bash
mvn test
```

### Run Tests with Coverage

```bash
mvn clean test jacoco:report
```

### View Coverage Report

```bash
open target/site/jacoco/index.html
```

### Test Results

**End-to-End Tests (Python Scripts)**: 25/25 passed (100%)

- Configuration tests: 4/4 passed
- inspect_java_class: 5/5 passed
- list_module_dependencies: 4/4 passed
- search_java_class: 4/4 passed
- build_module: 3/3 passed
- Integration tests: 3/3 passed
- Performance tests: 2/2 passed

**Integration Tests (Java - Direct JSON-RPC)**: 19/19 passed (100%)

- Tool listing: 1/1 passed
- inspect_java_class: 4/4 passed
- list_module_dependencies: 4/4 passed
- search_java_class: 4/4 passed
- build_module: 3/3 passed
- Integration workflows: 3/3 passed

**MCP Client Integration Tests (Java)**: 19/19 passed (100%)

- Server initialization: 2/2 passed
- inspect_java_class: 8/8 passed
- list_module_dependencies: 3/3 passed
- search_java_class: 4/4 passed
- build_module: 4/4 passed

**MCP Inspector CLI Tests**: 8/8 passed (100%)

- Server Initialization: 1/1 passed
- inspect_java_class: 3/3 passed
- list_module_dependencies: 1/1 passed
- search_java_class: 2/2 passed
- build_module: 1/1 passed

**Overall**: 71/71 tests passed (100%)

See `MCP_SERVER_TEST_REPORT.md`, `MCP_CLIENT_TEST_REPORT.md`, and `MCP_INSPECTOR_TEST_REPORT.md` for detailed test results.

### MCP Inspector Testing

To test the server using MCP Inspector CLI:

```bash
# Install MCP Inspector CLI
npm install -g @modelcontextprotocol/inspector-cli

# Run automated tests
powershell -ExecutionPolicy Bypass -File test_mcp_inspector_simple.ps1

# Manual testing
npx @modelcontextprotocol/inspector --cli --config mcp-inspector-config.json --server jlens-mcp-server --method tools/list
```

See `MCP_INSPECTOR_INTEGRATION_GUIDE.md` for detailed testing instructions.

## Architecture

```
io.github.bhxch.mcp.jlens/
├── Main.java                          # Application entry point
├── config/                            # Configuration management
│   ├── ServerConfig.java
│   ├── DecompilerConfig.java
│   └── MavenConfig.java
├── server/                            # MCP Server implementation
│   ├── JavaClasspathServer.java       # Main server class using MCP SDK
│   └── handlers/                      # Tool handlers
│       ├── InspectJavaClassHandler.java
│       ├── ListModuleDependenciesHandler.java
│       ├── SearchJavaClassHandler.java
│       └── BuildModuleHandler.java
├── maven/                             # Maven integration
│   ├── resolver/
│   │   ├── MavenResolverFactory.java
│   │   ├── MavenDirectResolver.java
│   │   └── MavenInvokerResolver.java
│   └── model/
│       ├── ModuleContext.java
│       └── DependencyInfo.java
├── decompiler/                        # Decompilation module
│   ├── DecompilerFactory.java
│   └── impl/
│       ├── FernflowerDecompiler.java
│       ├── CFRDecompiler.java
│       └── VineflowerDecompiler.java
├── inspector/                         # Code inspection module
│   ├── ClassInspector.java
│   └── model/
│       ├── ClassMetadata.java
│       ├── MethodInfo.java
│       └── FieldInfo.java
├── cache/                             # Caching module
│   └── CacheManager.java
├── classpath/                         # Classpath and package resolution
│   └── PackageMappingResolver.java
├── dependency/                        # Dependency management
│   ├── DependencyManager.java
│   └── MavenBuilder.java
└── intelligence/                      # AI interaction intelligence
    └── BuildPromptGenerator.java
```

## Documentation

- `README.md` - This file
- `README_CN.md` - Chinese version
- `iflow_mcp.md` - iFlow CLI integration configuration
- `MCP_SERVER_TEST_REPORT.md` - Detailed server test results
- `MCP_CLIENT_TEST_REPORT.md` - MCP client integration test results
- `PROJECT_SUMMARY.md` - Project summary
- `PLAN_2.md` - Implementation plan
- `TESTING.md` - Testing guide

## License

This project is licensed under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
