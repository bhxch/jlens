# Java Maven Classpath MCP 服务器 - 项目摘要

## 项目概述

本项目实现了一个用于检查 Java 类和 Maven 依赖的模型上下文协议（MCP）服务器。服务器为 AI 助手理解 Java 代码库提供了工具，通过解析 Maven 依赖和检查类元数据。

## 实现状态

### 已完成的组件

#### 1. 配置模块 ✓

- ServerConfig.java - 带有虚拟线程设置的服务器配置
- DecompilerConfig.java - 反编译器配置（Fernflower/CFR/Vineflower）
- MavenConfig.java - Maven 解析器配置

#### 2. MCP 协议核心 ✓

- RequestHandler.java - 带有虚拟线程支持的 JSON-RPC 请求处理器
- VirtualThreadExecutor.java - 为虚拟线程优化的执行器
- ToolRegistry.java - MCP 工具注册表
- MCPTool.java - MCP 工具接口
- InspectJavaClassTool.java - 用于检查 Java 类的工具
- ListModuleDependenciesTool.java - 用于列出 Maven 依赖的工具

#### 3. 虚拟线程处理 ✓

- VirtualThreadExecutor.java - I/O 和 CPU 密集型任务执行
- ParallelProcessor.java - 并行处理工具
- AsyncTaskManager.java - 带有取消功能的异步任务管理

#### 4. Maven 集成 ✓

- MavenResolver.java - Maven 解析器接口
- MavenDirectResolver.java - 直接 POM 解析器（后备）
- MavenInvokerResolver.java - 基于 Maven Invoker 的解析器
- MavenResolverFactory.java - 创建解析器的工厂
- ModuleContext.java - Maven 模块上下文模型
- DependencyInfo.java - 依赖信息模型
- Scope.java - 依赖范围枚举

#### 5. 类检查 ✓

- ClassInspector.java - Java 类检查器
- ClassMetadata.java - 类元数据模型
- MethodInfo.java - 方法信息模型
- FieldInfo.java - 字段信息模型
- ParameterInfo.java - 参数信息模型

#### 6. 反编译 ✓

- DecompilerAdapter.java - 反编译器接口
- DecompilerFactory.java - 反编译器工厂
- FernflowerDecompiler.java - Fernflower 实现
- CFRDecompiler.java - CFR 实现
- VineflowerDecompiler.java - Vineflower 实现（占位符）

#### 7. 缓存 ✓

- CacheManager.java - 中央缓存管理器
- ModuleCache.java - 模块上下文缓存
- ClassMetadataCache.java - 类元数据缓存

#### 8. 主应用程序 ✓

- Main.java - 带有 stdin/stdout 通信的应用程序入口

## 测试

### 单元测试（6 个测试类）

- ServerConfigTest.java - 配置测试
- MavenDirectResolverTest.java - Maven 解析器测试
- CacheManagerTest.java - 缓存测试
- VirtualThreadExecutorTest.java - 虚拟线程测试
- ToolRegistryTest.java - 工具注册表测试
- ClassInspectorTest.java - 类检查器测试

### 测试覆盖率目标: ≥80%

## 项目结构

```
.\
├── pom.xml                              # Maven 配置
├── README.md                            # 项目文档
├── TESTING.md                           # 测试指南
├── PROJECT_SUMMARY.md                   # 本文件
├── plan.md                              # 原始开发计划
└── src/
    ├── main/java/io/github/bhxch/mcp/javastub/
    │   ├── Main.java
    │   ├── config/
    │   │   ├── ServerConfig.java
    │   │   ├── DecompilerConfig.java
    │   │   └── MavenConfig.java
    │   ├── mcp/
    │   │   ├── protocol/
    │   │   │   ├── RequestHandler.java
    │   │   │   └── VirtualThreadExecutor.java
    │   │   └── tools/
    │   │       ├── MCPTool.java
    │   │       ├── ToolRegistry.java
    │   │       ├── InspectJavaClassTool.java
    │   │       └── ListModuleDependenciesTool.java
    │   ├── maven/
    │   │   ├── resolver/
    │   │   │   ├── MavenResolver.java
    │   │   │   ├── MavenDirectResolver.java
    │   │   │   ├── MavenInvokerResolver.java
    │   │   │   └── MavenResolverFactory.java
    │   │   └── model/
    │   │       ├── Scope.java
    │   │       ├── DependencyInfo.java
    │   │       ├── ModuleContext.java
    │   │       └── MavenProject.java
    │   ├── decompiler/
    │   │   ├── DecompilerAdapter.java
    │   │   ├── DecompilerFactory.java
    │   │   └── impl/
    │   │       ├── FernflowerDecompiler.java
    │   │       ├── CFRDecompiler.java
    │   │       └── VineflowerDecompiler.java
    │   ├── inspector/
    │   │   ├── ClassInspector.java
    │   │   └── model/
    │   │       ├── ClassMetadata.java
    │   │       ├── FieldInfo.java
    │   │       ├── MethodInfo.java
    │   │       └── ParameterInfo.java
    │   ├── cache/
    │   │   ├── CacheManager.java
    │   │   ├── ModuleCache.java
    │   │   └── ClassMetadataCache.java
    │   └── concurrent/
    │       ├── VirtualThreadExecutor.java
    │       ├── ParallelProcessor.java
    │       └── AsyncTaskManager.java
    └── test/java/io/github/bhxch/mcp/javastub/
        └── unit/
            ├── config/
            │   └── ServerConfigTest.java
            ├── maven/
            │   └── MavenDirectResolverTest.java
            ├── cache/
            │   └── CacheManagerTest.java
            ├── concurrent/
            │   └── VirtualThreadExecutorTest.java
            ├── inspector/
            │   └── ClassInspectorTest.java
            └── mcp/
                └── ToolRegistryTest.java
\\\

## 主要功能

1. **虚拟线程支持**: 利用 Java 25 的虚拟线程实现高性能并发处理
2. **Maven 集成**: 解析并列出 Maven 模块依赖
3. **类检查**: 检查 Java 类并检索元数据
4. **缓存**: 使用 Caffeine 的智能缓存以提高性能
5. **多种反编译器**: 支持 Fernflower 和 CFR 反编译器
6. **MCP 协议**: 实现模型上下文协议以进行 AI 助手集成

## 构建和运行

### 构建
\\\ash
mvn clean install
\\\

### 运行
\\\ash
java -jar target/javastub-mcp-server-1.0.0-SNAPSHOT.jar
\\\

### 测试
\\\ash
mvn test
\\\

## 依赖项

- **MCP SDK**: io.modelcontextprotocol.sdk:mcp-java-sdk:0.1.0
- **Caffeine**: com.github.ben-manes.caffeine:caffeine:3.1.8
- **ASM**: org.ow2.asm:asm:9.7
- **Jackson**: com.fasterxml.jackson.core:jackson-databind:2.17.0
- **Fernflower**: org.jetbrains.intellij.deps:fernflower:242.23655.110
- **CFR**: org.benf:cfr:0.152
- **JUnit 5**: org.junit.jupiter:junit-jupiter:5.10.2
- **Mockito**: org.mockito:mockito-core:5.11.0

## 注意事项

- 项目需要 Java 25+ 以支持虚拟线程
- 需要 Maven 3.9+ 进行构建
- MCP SDK 依赖版本可能需要根据实际 SDK 可用性进行调整
- 某些反编译器实现可能需要额外配置

## 下一步

1. 安装 Maven 以构建和测试项目
2. 运行 mvn clean install 构建项目
3. 运行 mvn test 执行单元测试
4. 运行 mvn jacoco:report 生成覆盖率报告
5. 使用实际的 MCP 客户端测试 MCP 服务器
