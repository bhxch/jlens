# Java Maven Classpath MCP 服务器

一个用于检查 Java 类和 Maven 依赖的模型上下文协议（MCP）服务器。该服务器提供用于分析 Java 字节码、反编译类和解析 Maven 项目依赖的工具。

## 功能特性

- **Java 类检查**：通过字节码分析、反射和反编译检查 Java 类
- **Maven 依赖解析**：列出和分析 Maven 模块依赖
- **虚拟线程支持**：使用 Java 21+ 虚拟线程实现高性能并发处理
- **多种反编译器**：支持 Fernflower、CFR 和 Vineflower 反编译器
- **缓存**：内置 Caffeine 缓存以提高性能
- **MCP 协议兼容**：完全符合 MCP 2024-11-05 规范
- **MCP Java SDK**：基于官方 MCP Java SDK 0.17.2 构建

## 系统要求

- Java 17 或更高版本
- Maven 3.9+（用于构建）
- Maven 可执行文件（可选，用于依赖解析）

## 构建

```bash
mvn clean package
```

这将创建一个可执行的 JAR 文件： arget/javastub-mcp-server-1.0.0-SNAPSHOT.jar

## 使用方法

### 运行服务器

MCP 服务器通过 stdin/stdout 使用 JSON-RPC 2.0 协议进行通信。

```bash
java -jar target/javastub-mcp-server-1.0.0-SNAPSHOT.jar
```

### 命令行选项

```
选项:
  -vt, --virtual-threads <count>    最大虚拟线程数（默认：1000）
  -me, --maven-executable <path>    Maven 可执行文件路径
  -ms, --maven-settings <path>       Maven settings.xml 文件路径
  -mr, --maven-repo <path>          Maven 本地仓库路径
  -d, --decompiler <type>           反编译器类型：fernflower、cfr、vineflower（默认：fernflower）
  -p, --port <port>                 服务器端口（默认：8080）
  -l, --log-level <level>           日志级别：ERROR、WARN、INFO、DEBUG（默认：INFO）
  -h, --help                        显示此帮助信息
```

### 使用 MCP 客户端示例

使用官方 MCP 客户端：

```bash
mcp-client exec java -jar target/javastub-mcp-server-1.0.0-SNAPSHOT.jar
```

## MCP 工具

### inspect_java_class

检查 Java 类并返回其元数据。

**参数：**

- className（字符串，必需）：要检查的完全限定类名
- sourceFilePath（字符串，可选）：源文件路径，用于上下文
- detailLevel（字符串，可选）：详细级别 - "skeleton"、"basic" 或 "full"（默认："basic"）

**示例请求：**

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

**示例响应：**
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

列出 Maven 模块的依赖项。

**参数：**

- sourceFilePath（字符串，可选）：源文件路径，用于定位模块
- pomFilePath（字符串，可选）：pom.xml 文件路径
- scope（字符串，可选）：依赖范围 - "compile"、"provided"、"runtime"、"test" 或 "system"（默认："compile"）

**示例请求：**

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

**示例响应：**

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

## MCP 协议流程

1. **初始化**：客户端发送 initialize 请求
2. **已初始化**：客户端发送
otifications/initialized 通知
3. **工具列表**：客户端发送  ools/list 请求以获取可用工具
4. **工具执行**：客户端发送  ools/call 请求以执行工具

### 初始化示例

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

### 工具列表示例

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/list",
  "params": {}
}
```

## 架构

```
io.github.bhxch.mcp.javastub/
├── Main.java                          # 应用程序入口
├── config/                            # 配置管理
│   ├── ServerConfig.java
│   ├── DecompilerConfig.java
│   └── MavenConfig.java
├── server/                            # MCP 服务器实现
│   ├── JavaClasspathServer.java       # 使用 MCP SDK 的主服务器类
│   └── handlers/                      # 工具处理器
│       ├── InspectJavaClassHandler.java
│       └── ListModuleDependenciesHandler.java
├── maven/                             # Maven 集成
│   ├── resolver/
│   │   ├── MavenResolverFactory.java
```

│   │   ├── MavenDirectResolver.java
│   │   └── MavenInvokerResolver.java
│   └── model/
│       ├── ModuleContext.java
│       └── DependencyInfo.java
├── decompiler/                        # 反编译模块
│   ├── DecompilerFactory.java
│   └── impl/
│       ├── FernflowerDecompiler.java
│       ├── CFRDecompiler.java
│       └── VineflowerDecompiler.java
├── inspector/                         # 代码检查模块
│   ├── ClassInspector.java
│   └── model/
│       ├── ClassMetadata.java
│       ├── MethodInfo.java
│       └── FieldInfo.java
└── cache/                             # 缓存模块
    └── CacheManager.java

```

## 测试

运行所有测试：

```bash
mvn test
```

运行测试并生成覆盖率报告：

```bash
mvn clean test jacoco:report
```

查看覆盖率报告：

```bash
open target/site/jacoco/index.html
```

## 与 iFlow CLI 集成

要将此 MCP 服务器添加到 iFlow CLI，请使用以下命令：

```bash
iflow mcp add-json javastub-mcp-server
```

完整的 JSON 配置请参见 iflow_mcp.md。

## 许可证

本项目采用 MIT 许可证。

## 贡献

欢迎贡献！请随时提交 Pull Request。
