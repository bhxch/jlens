# Java Maven Classpath MCP Server

A Model Context Protocol (MCP) server for inspecting Java classes and Maven dependencies. This server provides tools to analyze Java bytecode, decompile classes, and resolve Maven project dependencies.

## Features

- **Java Class Inspection**: Inspect Java classes with bytecode analysis, reflection, and decompilation
- **Maven Dependency Resolution**: List and analyze Maven module dependencies
- **Virtual Thread Support**: High-performance concurrent processing using Java 21+ virtual threads
- **Multiple Decompilers**: Support for Fernflower, CFR, and Vineflower decompilers
- **Caching**: Built-in Caffeine caching for improved performance
- **MCP Protocol Compliant**: Fully compliant with MCP 2024-11-05 specification
- **MCP Java SDK**: Built on official MCP Java SDK 0.17.2

## Requirements

- Java 17 or higher
- Maven 3.9+ (for building)
- Maven executable (optional, for dependency resolution)

## Building

```bash
mvn clean package
```

This will create an executable JAR file:  arget/javastub-mcp-server-1.0.0-SNAPSHOT.jar

## Usage

### Running the Server

The MCP server communicates via stdin/stdout using JSON-RPC 2.0 protocol.

```bash
java -jar target/javastub-mcp-server-1.0.0-SNAPSHOT.jar
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

### Example with MCP Client

Using the official MCP client:

```bash
mcp-client exec java -jar target/javastub-mcp-server-1.0.0-SNAPSHOT.jar
```

## MCP Tools

### inspect_java_class

Inspect a Java class and return its metadata.

**Parameters:**

- className (string, required): The fully qualified class name to inspect
- sourceFilePath (string, optional): Path to source file for context
- detailLevel (string, optional): Level of detail - "skeleton", "basic", or "full" (default: "basic")

**Example Request:**

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "inspect_java_class",
    "arguments": {
      "className": "java.util.ArrayList",
      "detailLevel": "basic"
    }
  }
}
```

**Example Response:**
`json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "ClassMetadata{className='java.util.ArrayList', packageName='java.util', isInterface=false, isEnum=false, methods=0, fields=0}"
      }
    ],
    "isError": false
  }
}
`

### list_module_dependencies

List dependencies for a Maven module.

**Parameters:**

- sourceFilePath (string, optional): Path to source file to locate module
- pomFilePath (string, optional): Path to pom.xml file
- scope (string, optional): Dependency scope - "compile", "provided", "runtime", "test", or "system" (default: "compile")

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

**Example Response:**

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Module: io.github.bhxch:javastub-mcp-server:1.0.0-SNAPSHOT\n\nDependencies (COMPILE):\n  - org.slf4j:slf4j-api:2.0.9 [COMPILE]\n  - ch.qos.logback:logback-classic:1.4.11 [COMPILE]\n  - org.vineflower:vineflower:1.10.1 [COMPILE]\n  - org.benf:cfr:0.152 [COMPILE]\n  - org.apache.maven.shared:maven-invoker:3.3.0 [COMPILE]\n"
      }
    ],
    "isError": false
  }
}
```

## MCP Protocol Flow

1. **Initialize**: Client sends initialize request
2. **Initialized**: Client sends
otifications/initialized notification
3. **Tool Listing**: Client sends  ools/list request to get available tools
4. **Tool Execution**: Client sends  ools/call requests to execute tools

### Example Initialization

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "initialize",
  "params": {
    "protocolVersion": "2024-11-05",
    "capabilities": {},
    "clientInfo": {
      "name": "test-client",
      "version": "1.0.0"
    }
  }
}
```

### Example Tool Listing

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/list",
  "params": {}
}
```

## Architecture

```
io.github.bhxch.mcp.javastub/
├── Main.java                          # Application entry point
├── config/                            # Configuration management
│   ├── ServerConfig.java
│   ├── DecompilerConfig.java
│   └── MavenConfig.java
├── server/                            # MCP Server implementation
│   ├── JavaClasspathServer.java       # Main server class using MCP SDK
│   └── handlers/                      # Tool handlers
│       ├── InspectJavaClassHandler.java
│       └── ListModuleDependenciesHandler.java
├── maven/                             # Maven integration
│   ├── resolver/
│   │   ├── MavenResolverFactory.java
```

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
└── cache/                             # Caching module
    └── CacheManager.java

```

## Testing

Run all tests:

```bash
mvn test
```

Run tests with coverage:

```bash
mvn clean test jacoco:report
```

View coverage report:

```bash
open target/site/jacoco/index.html
```

## Integration with iFlow CLI

To add this MCP server to iFlow CLI, use the following command:

```bash
iflow mcp add-json javastub-mcp-server
```

See iflow_mcp.md for the complete JSON configuration.

## License

This project is licensed under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
