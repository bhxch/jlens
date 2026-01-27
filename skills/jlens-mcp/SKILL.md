---
name: jlens-mcp
description: 专业级 Java 代码库分析与 Maven 依赖管理技能。当需要进行以下操作时触发：(1) 在多模块项目中使用多版本隔离技术分析 Java 类结构，(2) 深度解析 Maven 依赖树及排查版本冲突，(3) 利用游标分页在海量 Jar 包中搜索类，(4) 自动化执行 Maven 构建任务，(5) 智能识别本地源码与第三方库。本技能指导 Agent 优先使用已注册的 MCP 服务，并提供命令行模式的 Fallback 机制。
---

# JLens MCP 专家级导航指南 (V1.1.0)

JLens 是一个专门为 AI Agent 设计的 Model Context Protocol (MCP) 服务器，它通过真实的反射分析和字节码解析，赋予 Agent 深度理解 Java 工程的能力。相比于简单的文本搜索，JLens 能够识别类继承关系、方法签名、可见性修饰符以及复杂的 Maven 依赖拓扑。

## 1. 核心工具与交互细节 (Protocol Details)

### 1.1 类检查 (inspect_java_class)
**交互逻辑**：
- **状态机响应**：
    - `status: "SUCCESS"`：解析成功。查看 `decompiledSource` 字段获取逻辑，查看 `methods` 和 `fields` 获取结构。
    - `status: "LOCAL_SOURCE"`：**重要！** 目标类属于当前工作区。此时应忽略元数据，直接通过响应中的 `sourceFile` 路径使用 `read_file` 工具阅读源码。
    - `status: "NOT_FOUND"`：类不存在。检查 `suggestion` 字段，可能需要先执行 `build_module`。
- **关键参数**：
    - `bypassCache`: 当发现类结构与实际代码不符（可能刚修改过源码）时，设为 `true` 以绕过 GAV 缓存进行实时解析。

### 1.2 分页搜索 (search_java_class)
**分页协议**：
- **结果解析**：如果响应中 `hasMore` 为 `true`，则必须记录 `nextCursor`。
- **后续请求**：在下一次调用时传入 `cursor: "<nextCursor>"`，直到 `hasMore` 为 `false`。搜索结果已按全限定名字典序排列，确保分页稳定性。

### 1.3 依赖分析 (list_module_dependencies)
**元数据解析**：
- 返回标准的 Maven 依赖模型。Agent 应当关注 `scope` 字段（如 `test`, `provided`）以判断类在运行时是否可用。

---

## 2. 场景化任务流 (Task-Driven Workflows)

### 场景 A：排查 NoClassDefFoundError 或方法找不到错误
1.  **定位依赖**：调用 `list_module_dependencies` 检查是否有多个版本的 Jar 包包含冲突的类。
2.  **版本隔离检查**：利用 `inspect_java_class` 的版本隔离特性，分别为不同模块上下文加载该类，对比 `methods` 列表，找出缺失的方法。
3.  **构建验证**：修改 pom.xml 后，调用 `build_module` 刷新本地仓库并验证构建是否通过。

### 场景 B：大规模代码重构辅助
1.  **全局搜索**：使用 `search_java_class` 配合通配符（如 `com.api.*Service`）找到所有相关接口。
2.  **逐一检查**：利用 `list_class_fields` 快速提取所有实现类的私有字段，评估状态存储情况。
3.  **源码跳转**：发现 `LOCAL_SOURCE` 时，自动切换到 `read_file` 修改代码。

---

## 3. 执行策略 (Execution Strategy)

### 3.1 协议优先级
1.  **探测注册服务**：首先检查环境是否存在 `jlens-mcp-server`。
2.  **直接请求**：若存在，使用标准的 MCP `call_tool` 请求。

### 3.2 命令行 Fallback (无注册服务时)
若未找到注册服务，Agent 必须自主使用 shell 工具按以下格式执行：

**NPM 模式 (首选)**:
```powershell
npx -y @bhxch/jlens-mcp-server --tool <tool_name> --args '<json_arguments>'
```

**Python 模式**:
```bash
uvx jlens-mcp-server --tool <tool_name> --args '<json_arguments>'
```

---

## 4. 性能与限制提示
- **GAV 缓存**：JLens 会按 `GroupId:ArtifactId:Version` 共享缓存。对于第三方库（如 Spring），不同项目的解析结果是通用的，响应通常在 100ms 内。
- **首次索引**：在大型项目上首次运行 `search_java_class` 可能会触发全量索引（约 60s），后续请求将进入毫秒级。
- **JDK 要求**：底层运行环境必须为 Java 25+。
