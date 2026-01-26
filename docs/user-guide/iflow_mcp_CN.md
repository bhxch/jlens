# iFlow MCP 命令

本文档包含 iflow mcp add-json 命令的完整命令内容。

## MCP 服务器配置

### 服务器信息

- **名称**: jlens-mcp-server
- **版本**: 1.0.0
- **描述**: 用于检查 Java 类和 Maven 依赖的 MCP 服务器

### 工具

#### 1. inspect_java_class

检查 Java 类并返回其元数据。

**JSON 模式**:

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

列出 Maven 模块的依赖项。

**JSON 模式**:

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

## iflow mcp add-json 命令

要将此 MCP 服务器添加到 iFlow，请使用以下命令：

```bash
iflow mcp add-json --name jlens-mcp-server --command "java -jar target//jlens-mcp-server-1.1.0.jar" --tools '[{"name":"inspect_java_class","description":"Inspect a Java class and return its metadata","inputSchema":{"type":"object","properties":{"className":{"type":"string","description":"Fully qualified class name"},"sourceFilePath":{"type":"string","description":"Path to source file (optional)"},"detailLevel":{"type":"string","description":"Level of detail","enum":["skeleton","basic","full"]}},"required":["className"]}},{"name":"list_module_dependencies","description":"List dependencies of a Maven module","inputSchema":{"type":"object","properties":{"sourceFilePath":{"type":"string","description":"Path to source file in the module"},"pomFilePath":{"type":"string","description":"Path to pom.xml file"},"scope":{"type":"string","description":"Dependency scope","enum":["compile","provided","runtime","test","system"]}}}}]'
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

## 测试

测试用例可在以下 JSON 文件中找到：

- src/test/testcases/inspect_java_class_testcases.json
- src/test/testcases/list_module_dependencies_testcases.json

## 使用示例

### 示例 1: 检查 Java 类

```bash
echo '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"inspect_java_class","arguments":{"className":"java.util.List","detailLevel":"basic"}}}' | java -jar target/jlens-mcp-server-1.1.0.jar
```

### 示例 2: 列出模块依赖

```bash
echo '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"list_module_dependencies","arguments":{"pomFilePath":"pom.xml","scope":"compile"}}}' | java -jar target/jlens-mcp-server-1.1.0.jar
```

## 服务器说明

此服务器提供用于检查 Java 类和列出 Maven 模块依赖的工具。使用 'inspect_java_class' 检查 Java 类，使用 'list_module_dependencies' 列出 Maven 依赖。


