# iFlow MCP 命令

本文档包含 `iflow mcp add-json` 命令的完整命令内容。

## MCP 服务器配置

### 服务器信息

- **名称**: jlens-mcp-server
- **版本**: 1.1.0
- **描述**: 用于检查 Java 类和 Maven 依赖的 MCP 服务器

### 工具

#### 1. inspect_java_class

检查 Java 类并返回其元数据。支持本地工作区检测和版本隔离。

**JSON 模式**:

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

在包和依赖中搜索 Java 类，支持分页。

**JSON 模式**:

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

## iflow mcp add-json 命令

要将此 MCP 服务器添加到 iFlow，请使用以下命令（包含所有 5 个工具）：

```bash
iflow mcp add-json --name jlens-mcp-server --command "npx -y @bhxch/jlens-mcp-server" --tools '[{"name":"inspect_java_class","description":"Inspect a Java class and return its metadata. If the class is in local workspace, it will return a hint to read source directly.","inputSchema":{"type":"object","properties":{"className":{"type":"string","description":"Fully qualified class name"},"sourceFilePath":{"type":"string","description":"Path to source file (optional)"},"detailLevel":{"type":"string","description":"Level of detail","enum":["skeleton","basic","full"]},"bypassCache":{"type":"boolean","description":"Whether to bypass cache"}},"required":["className"]}},{"name":"list_class_fields","description":"List fields of a Java class with visibility filtering","inputSchema":{"type":"object","properties":{"className":{"type":"string","description":"Fully qualified class name"},"visibility":{"type":"array","items":{"type":"string","enum":["public","protected","private","package-private"]}},"sourceFilePath":{"type":"string","description":"Path to source file"}},"required":["className"]}},{"name":"list_module_dependencies","description":"List dependencies of a Maven module","inputSchema":{"type":"object","properties":{"sourceFilePath":{"type":"string","description":"Path to source file"},"pomFilePath":{"type":"string","description":"Path to pom.xml"},"scope":{"type":"string","enum":["compile","provided","runtime","test","system"]}}}},{"name":"search_java_class","description":"Search for Java classes with pagination","inputSchema":{"type":"object","properties":{"classNamePattern":{"type":"string","description":"Class name pattern"},"sourceFilePath":{"type":"string"},"searchType":{"type":"string","enum":["exact","prefix","suffix","contains","wildcard"]},"limit":{"type":"integer"},"cursor":{"type":"string"}},"required":["classNamePattern"]}},{"name":"build_module","description":"Build Maven module","inputSchema":{"type":"object","properties":{"sourceFilePath":{"type":"string"},"goals":{"type":"array","items":{"type":"string"}},"downloadSources":{"type":"boolean"},"timeoutSeconds":{"type":"integer"}},"required":["sourceFilePath"]}}]'
```

## 构建 JAR 文件

在使用 MCP 服务器之前，您需要构建 JAR 文件：

```bash
mvn clean package
```

这将创建以下位置的 JAR 文件：

```
target/jlens-mcp-server-1.1.0.jar
```

## 服务器说明

此服务器提供用于检查 Java 类、列出类字段、列出 Maven 模块依赖、搜索类和构建 Maven 模块的工具。使用 'inspect_java_class' 进行具有版本隔离的深度检查，使用 'list_class_fields' 进行变量分析，使用 'list_module_dependencies' 进行依赖管理，使用 'search_java_class' 进行带分页的结果搜索，使用 'build_module' 进行构建自动化。