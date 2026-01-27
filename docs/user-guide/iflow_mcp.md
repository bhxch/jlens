# iFlow MCP Commands

This document contains the complete command content for the `iflow mcp add-json` command.

## MCP Server Configuration

### Server Information

- **Name**: jlens-mcp-server
- **Version**: 1.1.0
- **Description**: MCP server for inspecting Java classes and Maven dependencies

### Tools

#### 1. inspect_java_class

Inspect a Java class and return its metadata. Detects local workspace classes.

**JSON Schema**:

```json
{
  "name": "inspect_java_class",
  "description": "Inspect a Java class and return its metadata. If the class is in local workspace, it will return a hint to read source directly.",
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
      },
      "bypassCache": {
        "type": "boolean",
        "description": "Whether to bypass cache and re-inspect"
      }
    },
    "required": ["className"]
  }
}
```

#### 2. search_java_class

Search for Java classes across packages and dependencies with pagination.

**JSON Schema**:

```json
{
  "name": "search_java_class",
  "description": "Search for Java classes across packages and dependencies with pagination",
  "inputSchema": {
    "type": "object",
    "properties": {
      "classNamePattern": {
        "type": "string",
        "description": "Class name pattern (supports wildcards: *, ?)"
      },
      "sourceFilePath": {
        "type": "string",
        "description": "Source file path for context"
      },
      "searchType": {
        "type": "string",
        "description": "Search type: exact, prefix, suffix, contains, wildcard",
        "enum": ["exact", "prefix", "suffix", "contains", "wildcard"]
      },
      "limit": {
        "type": "integer",
        "description": "Maximum number of results to return per page"
      },
      "cursor": {
        "type": "string",
        "description": "Pagination cursor from previous request"
      }
    },
    "required": ["classNamePattern"]
  }
}
```

## iflow mcp add-json Command

To add this MCP server to iFlow, use the following command (all 5 tools included):

```bash
iflow mcp add-json --name jlens-mcp-server --command "npx -y @bhxch/jlens-mcp-server" --tools '[{"name":"inspect_java_class","description":"Inspect a Java class and return its metadata. If the class is in local workspace, it will return a hint to read source directly.","inputSchema":{"type":"object","properties":{"className":{"type":"string","description":"Fully qualified class name"},"sourceFilePath":{"type":"string","description":"Path to source file (optional)"},"detailLevel":{"type":"string","description":"Level of detail","enum":["skeleton","basic","full"]},"bypassCache":{"type":"boolean","description":"Whether to bypass cache"}},"required":["className"]}},{"name":"list_class_fields","description":"List fields of a Java class with visibility filtering","inputSchema":{"type":"object","properties":{"className":{"type":"string","description":"Fully qualified class name"},"visibility":{"type":"array","items":{"type":"string","enum":["public","protected","private","package-private"]}},"sourceFilePath":{"type":"string","description":"Path to source file"}},"required":["className"]}},{"name":"list_module_dependencies","description":"List dependencies of a Maven module","inputSchema":{"type":"object","properties":{"sourceFilePath":{"type":"string","description":"Path to source file"},"pomFilePath":{"type":"string","description":"Path to pom.xml"},"scope":{"type":"string","enum":["compile","provided","runtime","test","system"]}}}},{"name":"search_java_class","description":"Search for Java classes with pagination","inputSchema":{"type":"object","properties":{"classNamePattern":{"type":"string","description":"Class name pattern"},"sourceFilePath":{"type":"string"},"searchType":{"type":"string","enum":["exact","prefix","suffix","contains","wildcard"]},"limit":{"type":"integer"},"cursor":{"type":"string"}},"required":["classNamePattern"]}},{"name":"build_module","description":"Build Maven module","inputSchema":{"type":"object","properties":{"sourceFilePath":{"type":"string"},"goals":{"type":"array","items":{"type":"string"}},"downloadSources":{"type":"boolean"},"timeoutSeconds":{"type":"integer"}},"required":["sourceFilePath"]}}]'
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

## Server Instructions

This server provides tools for inspecting Java classes, listing class fields, listing Maven module dependencies, searching for classes, and building Maven modules. Use 'inspect_java_class' for deep inspection with version isolation, 'list_class_fields' for variable analysis, 'list_module_dependencies' for dependency management, 'search_java_class' for discovery with pagination, and 'build_module' for build automation.