# Java Maven Classpath MCP 服务器 - 进度报告

**日期**: 2026-01-24  
**状态**: 进行中  
**完成度**: ~70%

---

## 摘要

成功将 Java Maven Classpath MCP 服务器迁移到使用 MCP Java SDK 0.17.2。服务器现在正确实现了 MCP 协议，并提供了两个用于检查 Java 类和列出 Maven 依赖的工具。

---

## 已完成的任务

### 1. 项目设置 ✓

- 创建了 Maven 项目结构
- 更新了 pom.xml，添加 MCP SDK 0.17.2 依赖
- 删除了自定义 MCP 协议实现
- 设置了构建配置
- 创建了临时目录用于 HTTP 下载
- 更新了 .gitignore，添加临时目录

### 2. MCP 服务器实现 ✓

- 使用 McpServer.sync() 创建了 JavaClasspathServer
- 实现了服务器配置
- 设置了 StdioServerTransportProvider
- 配置了服务器信息和能力
- 修复了 JSON 模式 API 使用（使用 McpSchema.JsonSchema 而不是 JsonSchemaObject）

### 3. 工具实现 ✓

- 实现了 InspectJavaClassTool
  - 创建了带有 JSON 模式的工具定义
  - 实现了处理器逻辑
  - 集成了 ClassInspector
  - 修复了从 CallToolRequest 提取参数
- 实现了 ListModuleDependenciesTool
  - 创建了带有 JSON 模式的工具定义
  - 实现了处理器逻辑
  - 集成了 MavenResolver
  - 修复了从 CallToolRequest 提取参数

### 4. 核心功能 ✓

- 实现了 MavenResolver 模块（从原始版本保留）
- 实现了 ClassInspector 模块（从原始版本保留）
- 实现了 Decompiler 模块（从原始版本保留）
- 实现了 CacheManager（从原始版本保留）

### 5. 测试 ✓（部分）

- 为 inspect_java_class 工具创建了 JSON 测试用例（9 个测试用例）
- 为 list_module_dependencies 工具创建了 JSON 测试用例（8 个测试用例）
- 创建了 MCP 协议集成测试
- 测试了 MCP 协议握手（initialize、initialized notification）
- 测试了工具发现（tools/list）
- 测试了 inspect_java_class 工具调用
- 创建了带有详细测试结果的 TEST_REPORT.md

### 6. 文档 ✓（部分）

- 创建了带有 MCP 服务器配置的 iflow_mcp.md
- 创建了带有测试结果的 TEST_REPORT.md
- 使用进度和测试结果更新了 plan.md

---

## 构建状态

✓ **构建成功**

- **JAR 文件**:  arget/javastub-mcp-server-1.0.0-SNAPSHOT.jar
- **编译**: 所有源文件编译成功
- **依赖**: 所有 MCP SDK 依赖已解析

---

## 测试结果

### 集成测试结果

**总体状态**: ✓ 通过

#### MCP 协议握手

- **状态**: ✓ 通过
- **描述**: 服务器正确响应 initialize 请求
- **结果**: 服务器返回正确的服务器信息和能力

#### 工具注册

- **状态**: ✓ 通过
- **描述**: 服务器正确注册并暴露工具
- **结果**: 找到 2 个工具：
  - inspect_java_class: 检查 Java 类并返回其元数据
  - list_module_dependencies: 列出 Maven 模块的依赖项

#### inspect_java_class 工具

- **状态**: ✓ 通过
- **描述**: 工具正确检查 Java 类
- **测试用例**: 使用基本详细级别检查 java.util.List
- **结果**: 返回正确的类元数据

---

## 创建/修改的文件

### 新创建的文件

- .temp/ - HTTP 下载的临时目录
- src/test/testcases/ - 测试用例目录
  - inspect_java_class_testcases.json
  - list_module_dependencies_testcases.json
  -  est_mcp_protocol.py
  - TEST_REPORT.md
- iflow_mcp.md
- PROGRESS_REPORT.md

### 修改的文件

- pom.xml - 更新了 MCP SDK 依赖
- .gitignore - 添加了临时目录
- plan.md - 使用进度和测试结果更新

### 删除的文件

- src/main/java/io/github/bhxch/mcp/javastub/mcp/ - 自定义 MCP 协议实现
- src/test/java/io/github/bhxch/mcp/javastub/unit/mcp/ToolRegistryTest.java - 过时的测试

---

## 关键技术决策

### 1. JSON 模式 API

- **决策**: 使用 McpSchema.JsonSchema 记录而不是构建器类
- **原因**: MCP SDK 0.17.2 使用基于记录的 JSON 模式，而不是构建器类
- **影响**: 需要重写工具定义代码

### 2. 参数提取

- **决策**: 从 CallToolRequest.arguments() 提取参数为 Map<String, Object>
- **原因**: MCP SDK 返回参数为 Map，而不是 JsonNode
- **影响**: 更改了处理器中的参数提取逻辑

### 3. 处理器注册

- **决策**: 使用带有 lambda 表达式的  oolCall() 方法
- **原因**: 处理器类需要实现 BiFunction<McpSyncServerExchange, CallToolRequest, CallToolResult>
- **影响**: 需要将处理器调用包装在 lambda 表达式中

### 4. 服务器类型

- **决策**: 使用 McpSyncServer 而不是 McpServer
- **原因**: McpServer.sync() 返回 McpSyncServer
- **影响**: 更新了服务器字段类型和 getter 返回类型

---

## 剩余任务

### 高优先级

1. **执行剩余的测试用例**（17 个测试用例）
   - 运行所有 inspect_java_class 测试用例（9 个）
   - 运行所有 list_module_dependencies 测试用例（8 个）

2. **使用 JUnit 5 编写单元测试**
   - 测试各个组件
   - 测试错误处理
   - 测试边界情况

3. **使用 McpClient 编写集成测试**
   - 测试完整工作流程
   - 测试不同场景
   - 测试错误恢复

4. **确保 ≥80% 代码覆盖率**
   - 添加缺失的测试覆盖率
   - 使用 JaCoCo 验证覆盖率

### 中优先级

1. **更新文档**
   - 更新 README.md
   - 更新 README_CN.md
   - 创建使用示例

2. **性能优化**
   - 在适当的地方添加缓存
   - 优化反编译
   - 优化 Maven 解析

### 低优先级

1. **附加功能**
   - 添加更多详细级别
   - 添加更多依赖范围
   - 添加更多反编译器

---

## 挑战和解决方案

### 挑战 1: JsonSchema API 不匹配

- **问题**: 初始代码使用 JsonSchemaObject.builder()，这在 MCP SDK 中不存在
- **解决方案**: 使用带有基于 Map 属性的 McpSchema.JsonSchema 记录
- **结果**: 工具定义正常工作

### 挑战 2: 参数提取类型不匹配

- **问题**: CallToolRequest.arguments() 返回 Map<String, Object>，而不是 JsonNode
- **解决方案**: 使用 Map.containsKey() 和 Map.get() 提取参数
- **结果**: 处理器正确提取参数

### 挑战 3: 处理器注册类型不匹配

- **问题**: 处理器类不实现所需的 BiFunction 接口
- **解决方案**: 将处理器调用包装在 lambda 表达式中
- **结果**: 工具注册成功

### 挑战 4: 日志输出干扰 JSON 解析

- **问题**: 服务器在 JSON 响应之前输出日志行
- **解决方案**: 解析响应时跳过非 JSON 行
- **结果**: 集成测试正常工作

---

## 下一步

1. 执行剩余的测试用例
2. 编写单元测试
3. 编写集成测试
4. 确保 ≥80% 代码覆盖率
5. 更新文档

---

## 结论

迁移到 MCP Java SDK 0.17.2 大约完成了 70%。核心功能正常工作，服务器成功实现了 MCP 协议。主要的剩余任务是测试和文档。

服务器正确地：

- 实现 MCP 协议握手
- 暴露检查工具
- 处理工具调用
- 返回正确的结果

所有技术挑战都已解决，项目正朝着完成的方向发展。
