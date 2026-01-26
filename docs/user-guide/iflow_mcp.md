# iFlow MCP Commands

This document contains the complete command content for the `iflow mcp add-json` command.

## MCP Server Configuration

### Server Information

- **Name**: jlens-mcp-server
- **Version**: 1.0.0
- **Description**: MCP server for inspecting Java classes and Maven dependencies

### Tools

#### 1. inspect_java_class

Inspect a Java class and return its metadata.

**JSON Schema**:

```json
{
  "name": "inspect_java_class",
  "description": "Inspect a Java class and return its metadata",
  "inputSchema": {
    "type": "object",
    "properties": {
      "className": {
        "type": "string",
        "description": "Fully qualified class name"
      },
      "sourceFilePath": {
        "type": "string",
        "description": "Path to source file (optional)"
      },
      "detailLevel": {
        "type": "string",
        "description": "Level of detail",
        "enum": ["skeleton", "basic", "full"]
      }
    },
    "required": ["className"]
  }
}
```

#### 2. list_module_dependencies

List dependencies of a Maven module.

**JSON Schema**:

```json
{
  "name": "list_module_dependencies",
  "description": "List dependencies of a Maven module",
  "inputSchema": {
    "type": "object",
    "properties": {
      "sourceFilePath": {
        "type": "string",
        "description": "Path to source file in the module"
      },
      "pomFilePath": {
        "type": "string",
        "description": "Path to pom.xml file"
      },
      "scope": {
        "type": "string",
        "description": "Dependency scope",
        "enum": ["compile", "provided", "runtime", "test", "system"]
      }
    }
  }
}
```

## iflow mcp add-json Command

To add this MCP server to iFlow, use the following command:

```bash
iflow mcp add-json --name jlens-mcp-server --command "java -jar target//jlens-mcp-server-1.1.0.jar" --tools '[{"name":"inspect_java_class","description":"Inspect a Java class and return its metadata","inputSchema":{"type":"object","properties":{"className":{"type":"string","description":"Fully qualified class name"},"sourceFilePath":{"type":"string","description":"Path to source file (optional)"},"detailLevel":{"type":"string","description":"Level of detail","enum":["skeleton","basic","full"]}},"required":["className"]}},{"name":"list_module_dependencies","description":"List dependencies of a Maven module","inputSchema":{"type":"object","properties":{"sourceFilePath":{"type":"string","description":"Path to source file in the module"},"pomFilePath":{"type":"string","description":"Path to pom.xml file"},"scope":{"type":"string","description":"Dependency scope","enum":["compile","provided","runtime","test","system"]}}}},{"name":"search_java_class","description":"Search for Java classes across packages and dependencies","inputSchema":{"type":"object","properties":{"classNamePattern":{"type":"string","description":"Class name pattern (supports wildcards: *, ?)"},"sourceFilePath":{"type":"string","description":"Source file path for context (optional)"},"searchType":{"type":"string","description":"Search type: exact, prefix, suffix, contains, wildcard","enum":["exact","prefix","suffix","contains","wildcard"]},"limit":{"type":"integer","description":"Maximum number of results to return"}},"required":["classNamePattern"]}},{"name":"build_module","description":"Build Maven module and download missing dependencies","inputSchema":{"type":"object","properties":{"sourceFilePath":{"type":"string","description":"Source file path for module context"},"goals":{"type":"array","description":"Maven goals to execute","items":{"type":"string"}},"downloadSources":{"type":"boolean","description":"Whether to download source JARs"},"timeoutSeconds":{"type":"integer","description":"Build timeout in seconds"}},"required":["sourceFilePath"]}}]'
```

## Building the JAR File

Before using the MCP server, you need to build the JAR file:

```bash
mvn clean package
```

This will create the JAR file at:

```
target/jlens-mcp-server-1.1.0.jar
```

## Testing

Test cases are available in the following JSON files:

- `src/test/testcases/inspect_java_class_testcases.json`
- `src/test/testcases/list_module_dependencies_testcases.json`

## Usage Examples

### Example 1: Inspect a Java Class

```bash
echo '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"inspect_java_class","arguments":{"className":"java.util.List","detailLevel":"basic"}}}' | java -jar target/jlens-mcp-server-1.1.0.jar
```

### Example 2: List Module Dependencies

```bash
echo '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"list_module_dependencies","arguments":{"pomFilePath":"pom.xml","scope":"compile"}}}' | java -jar target/jlens-mcp-server-1.1.0.jar
```

## Server Instructions

This server provides tools for inspecting Java classes and listing Maven module dependencies. Use 'inspect_java_class' to inspect a Java class and 'list_module_dependencies' to list Maven dependencies.


