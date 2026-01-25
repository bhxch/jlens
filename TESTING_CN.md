# 测试指南

## 测试覆盖率

项目包含所有主要组件的综合单元测试：

### 单元测试

- **配置模块**（ServerConfigTest）
  - 默认配置值
  - 命令行参数解析
  - Maven 配置创建

- **Maven 模块**（MavenDirectResolverTest）
  - POM 文件解析
  - 依赖提取
  - 模块上下文解析

- **缓存模块**（CacheManagerTest）
  - 模块上下文缓存
  - 类元数据缓存
  - 并发访问处理
  - 缓存失效

- **并发模块**（VirtualThreadExecutorTest）
  - I/O 密集型任务执行
  - CPU 密集型任务执行
  - 超时处理
  - 关闭行为

- **MCP 模块**（ToolRegistryTest）
  - 工具注册和检索
  - 工具执行
  - 禁用工具处理

- **检查器模块**（ClassInspectorTest）
  - 简单类名
  - 完全限定类名
  - 嵌套类
  - 不同详细级别

## 运行测试

### 先决条件

- Java 25+
- Maven 3.9+

### 运行所有测试

\\\ash
mvn test
\\\

### 运行特定测试类

\\\ash
mvn test -Dtest=ServerConfigTest
\\\

### 运行测试并生成覆盖率报告

\\\ash
mvn clean test jacoco:report
\\\

覆盖率报告将生成在  arget/site/jacoco/index.html。

### 运行集成测试

\\\ash
mvn verify
\\\

## 测试覆盖率要求

项目要求以下各项的最低 80% 代码覆盖率：

- 指令覆盖率
- 分支覆盖率

要检查覆盖率：

\\\ash
mvn jacoco:check
\\\

## 测试结构

\\\
src/test/java/io/github/bhxch/mcp/javastub/
├── unit/
│   ├── config/
│   │   └── ServerConfigTest.java
│   ├── maven/
│   │   └── MavenDirectResolverTest.java
│   ├── cache/
│   │   └── CacheManagerTest.java
│   ├── concurrent/
│   │   └── VirtualThreadExecutorTest.java
│   ├── inspector/
│   │   └── ClassInspectorTest.java
│   └── mcp/
│       └── ToolRegistryTest.java
├── integration/
│   └── (集成测试待添加)
└── performance/
    └── (性能测试待添加)
\\\

## 添加新测试

1. 在 src/test/java 下的适当包中创建测试类
2. 使用 JUnit 5 注解（@Test、@DisplayName、@BeforeEach）
3. 遵循命名约定：*Test.java
4. 使用 @TempDir 进行临时文件操作
5. 使用 @Timeout 处理有时间限制的测试

## CI/CD 集成

项目包含 GitHub Actions 工作流程，用于：

- 构建项目
- 运行测试
- 检查代码覆盖率
- 运行 SonarQube 分析

详情请参阅 .github/workflows/ci.yml。
