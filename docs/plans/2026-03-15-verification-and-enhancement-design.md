# JLens MCP Server 验证与增强设计 - 2026-03-15

## 概述

本设计文档描述了对 JLens MCP Server 进行全面验证、修复和测试增强的完整计划。

## 1. 验证策略与现状评估

### 1.1 核心功能验证点

**1. JDK @since 信息提取**
- **组件**: `JdkSourceService`, `ClassInspector`
- **验证方法**:
  - 下载 JDK 源码压缩包用于测试
  - 使用标准 JDK 类（`java.util.List`, `java.util.Stream`, `java.util.Optional` 等）
  - 验证正则表达式能否正确提取 `@since` 标签
- **预期行为**:
  - ✅ 有 `src.zip` 时：正确提取 @since 信息
  - ✅ 无 `src.zip` 时：优雅降级（since 字段为 null），不抛出错误
  - ✅ 非 JDK 类：since 字段为 null

**2. detailLevel 完整实现**
- **组件**: `ClassInspector.inspectClass()`
- **验证方法**:
  - 对同一个类（`java.lang.String`, `java.util.HashMap`）使用三种级别
  - 验证返回字段的数量差异和内容差异
- **预期行为**:
  - **skeleton**: 只返回类名、修饰符、父类、接口
  - **basic**: skeleton + public/protected 成员签名
  - **full**: 所有成员 + private + 方法体提示 + @since 信息

**3. list_class_fields 接口支持**
- **组件**: `ListClassFieldsHandler`, `ClassInspector`
- **验证方法**:
  - 测试有字段的接口（`java.sql.Connection`, `java.util.Collection`）
  - 测试无字段的接口（`java.lang.Runnable`）
- **预期行为**:
  - ✅ 正确列出接口的 `public static final` 字段
  - ✅ 无字段时返回空列表，不抛出错误

**4. Maven 属性占位符解析**
- **组件**: `MavenResolverFactory`, `MavenInvokerResolver`, `MavenDirectResolver`
- **验证方法**:
  - 使用包含 `${project.version}`, `${spring.version}` 等属性的 POM
  - 检查 `list_module_dependencies` 和 `search_java_class` 的输出
- **预期行为**:
  - ✅ 完全解析的依赖列表，无 `${...}` 字符串
  - ✅ 正确处理 BOM 依赖
  - ✅ 正确处理 parent 继承

## 2. Maven 解析器优化方案

### 2.1 MavenInvokerResolver 默认启用

**策略**: 优先使用 MavenInvokerResolver（方案 A）

**实施细节**:

1. **配置调整**
   - 修改 `MavenResolverFactory.createResolver()`
   - 移除条件检查，默认返回 `MavenInvokerResolver`
   - 仅在配置明确指定或 `mvn` 不可用时降级

2. **错误处理优化**
   ```java
   try {
       return new MavenInvokerResolver(config);
   } catch (Exception e) {
       logger.warn("MavenInvokerResolver failed, falling back to MavenDirectResolver", e);
       return new MavenDirectResolver(config);
   }
   ```

3. **降级检测**
   - 检查 `mvn` 命令是否可用：`mvn --version`
   - 捕获超时异常
   - 提供清晰的日志说明

4. **性能优化**
   - 添加简单的结果缓存（基于 POM 文件路径和最后修改时间）
   - 在 `ServerConfig` 中添加 `mavenTimeout` 配置（默认 60 秒）

### 2.2 MavenDirectResolver 增强

虽然不是主要方案，但作为后备仍需改进：

- 支持基本的属性占位符解析（同 POM 内定义）
- 支持常见的内置属性（`project.version`, `project.groupId` 等）
- 提供清晰的日志说明局限性

## 3. 测试策略与 MCP Inspector 集成

### 3.1 单元测试扩展

**覆盖范围**: 每个 Handler 至少 5-8 个测试用例

**新增测试文件**:
1. `InspectJavaClassHandlerExtendedTest.java` - 已完成（11 个测试）✅
2. `ListClassFieldsHandlerExtendedTest.java` - 已完成（11 个测试）✅
3. `ListModuleDependenciesHandlerExtendedTest.java` - 待创建
4. `SearchJavaClassHandlerExtendedTest.java` - 待创建
5. `BuildModuleHandlerExtendedTest.java` - 待创建

**测试场景**:
- 不存在的类/POM
- 接口、注解、枚举类型
- 内部类、数组类、匿名类
- 空值、无效参数
- profiles 参数
- bypassCache 参数
- 不同 scope 的依赖

### 3.2 MCP Inspector 集成测试（双重验证）

**阶段 A：独立脚本验证**

目录结构：
```
.temp/verification/
├── jdk-sources/              # 解压的 JDK 源码（不提交）
├── local-maven-sources/      # 本地 Maven 仓库中的源码 JAR（不提交）
├── expected-outputs/         # 从源码提取的预期输出（JSON）
├── actual-outputs/           # MCP Inspector 实际输出（JSON）
└── comparison-report.md      # 对比报告
```

流程：
1. 下载/解压 JDK 源码到 `.temp/verification/jdk-sources/`
2. 从本地 Maven 仓库提取源码 JAR
3. 手动解析源码生成预期输出（JSON 格式）
4. 使用 MCP Inspector 调用工具获取实际输出
5. 对比并生成报告

**阶段 B：自动化测试集成**

创建 `McpInspectorComprehensiveTest.java`：
- 使用 `ProcessBuilder` 调用 MCP Inspector CLI
- 读取预期输出 JSON 文件
- 自动对比关键字段
- 生成 JUnit 测试结果

**测试数据选择**:

JDK 类（10 个）：
1. `java.util.List` - 接口，有 @since 1.2
2. `java.util.Map` - 接口
3. `java.util.Stream` - 接口，@since 1.8
4. `java.util.Optional` - 类，@since 1.8
5. `java.lang.String` - 类，大量方法
6. `java.lang.Integer` - 包装类
7. `java.lang.Class` - 核心类
8. `java.lang.Object` - 基类
9. `java.lang.Thread.State` - 枚举
10. `java.lang.Runnable` - 函数式接口

项目依赖类（5 个）：
1. `io.modelcontextprotocol.spec.McpSchema` - MCP SDK 核心类
2. `com.fasterxml.jackson.databind.ObjectMapper` - Jackson
3. `com.github.benmanes.caffeine.cache.Cache` - Caffeine
4. `org.slf4j.Logger` - SLF4J
5. `reactor.core.publisher.Mono` - Reactor

### 3.3 .gitignore 更新

确保不提交二进制文件：
```
.temp/
*.jar
*.class
target/
```

## 4. 实施步骤与提交计划

### 阶段 1：代码修复与优化（3 个 commits）

**Commit 1: ref(maven): 优化 Maven 解析器默认使用 MavenInvokerResolver**
- 修改 `MavenResolverFactory.java`
- 增强错误处理
- 添加配置支持

**Commit 2: fix(inspector): 完善 detailLevel 实现确保返回数据差异**
- 检查并修复 `ClassInspector.java` 的 detailLevel 实现
- 确保 skeleton/basic/full 返回不同详细程度
- 添加验证测试

**Commit 3: fix(handlers): 增强边界情况处理和错误恢复**
- 验证接口字段处理
- 验证 @since 降级逻辑
- 修复发现的问题

### 阶段 2：扩展单元测试（3 个 commits）

**Commit 4-6: test(handlers): 为 XXXHandler 增加扩展测试覆盖边界场景**
- `ListModuleDependenciesHandlerExtendedTest.java`
- `SearchJavaClassHandlerExtendedTest.java`
- `BuildModuleHandlerExtendedTest.java`

### 阶段 3：MCP Inspector 集成测试（2 个 commits）

**Commit 7: test(verification): 添加 MCP Inspector 验证脚本和测试数据**
- 创建 `.temp/verification/` 目录结构
- 编写验证脚本 `scripts/verify_with_mcp_inspector.py`
- 准备测试数据和预期输出

**Commit 8: test(integration): 集成 MCP Inspector 综合测试到测试套件**
- 创建 `McpInspectorComprehensiveTest.java`
- 集成到 Maven 测试生命周期
- 生成测试报告

### 阶段 4：文档更新（1 个 commit）

**Commit 9: docs(verification): 更新文档反映验证结果和增强功能**
- 更新 `README.md`
- 更新 `PROJECT_SUMMARY.md`
- 更新 `JavaClasspathServer.java` 工具描述
- 添加验证报告到 `docs/developer-docs/reports/`
- 更新 `CLAUDE.md` 反映最新变更

## 5. 验收标准

完成所有实施后，必须满足以下标准：

✅ **编译**: `mvn clean package -DskipTests` 成功
✅ **单元测试**: 所有 Handler 测试通过（预计 50+ 测试用例）
✅ **集成测试**: MCP Inspector 测试通过（15 个测试用例）
✅ **功能验证**:
   - @since 信息正确提取（有 src.zip 时）
   - detailLevel 返回不同详细程度的数据
   - 接口字段正确列出
   - Maven 属性占位符完全解析
✅ **文档**: 所有相关文档已更新
✅ **提交**: 所有变更已提交，共 9 个 atomic commits

## 6. 风险与缓解

**风险 1**: JDK src.zip 下载失败
- **缓解**: 提供手动下载说明，使用环境变量指定路径

**风险 2**: MavenInvokerResolver 在某些环境不可用
- **缓解**: 确保降级到 MavenDirectResolver 工作正常

**风险 3**: 测试数据准备耗时
- **缓解**: 优先使用本地 Maven 仓库，分批准备

**风险 4**: 测试用例数量过多导致测试时间长
- **缓解**: 使用 JUnit `@Tag` 分类，支持选择性运行

---

**计划创建时间**: 2026-03-15
**预计完成时间**: 同一天
**负责人**: Claude Code
