# Design: JLens Feature Enhancements (2026-01-27)

## Overview
This document outlines the design and implementation of new features for the JLens MCP Server, including automated CI/CD, class field inspection with filtering, and cross-platform distribution wrappers.

## 1. Automated CI/CD (GitHub Actions)
### Architecture
A GitHub Actions workflow is implemented to automate building and releasing.       

- **Workflow File**: `.github/workflows/build-and-release.yml`
- **Triggers**:
  - Push to `main`: Builds and validates the JAR (skipping tests for speed).       
  - Push tags `v*`: Builds, creates a GitHub Release, and uploads the shaded JAR.  
- **Permissions**: Requires `contents: write` for release creation.

## 2. Class Field Inspection (`list_class_fields`)
### Components
- **Handler**: `ListClassFieldsHandler`
- **Logic**:
  - Uses `ClassInspector` to perform a basic inspection of the target class.       
  - Filters the returned fields based on a list of requested visibility modifiers (`public`, `protected`, `private`, `package-private`).
  - Mapping:
    - `public`: `Modifier.isPublic`
    - `protected`: `Modifier.isProtected`
    - `private`: `Modifier.isPrivate`
    - `package-private`: None of the above.
- **Tool Definition**: Registered in `JavaClasspathServer` as `list_class_fields`. 

## 3. Cross-platform Distribution
### npx (NPM)
- **Wrapper**: `bin/jlens-mcp-server.js` (Node.js script).
- **Package**: `package.json` in root.
- **Mechanism**: Checks for Java 25+, locates the JAR in the package, and spawns a child process.

### uvx (PyPI)
- **Wrapper**: `jlens_mcp_server/main.py` (Python script).
- **Package**: `pyproject.toml` using `hatchling`.
- **Mechanism**: Uses `subprocess` to run the JAR, including the JAR as package data.

## 4. Quality Assurance
- **Unit Tests**: Added `ListClassFieldsHandlerTest` to verify filtering logic.    
- **Test Coverage**: Targeting > 85% for core logic.
- **Version Management**: Updated `pom.xml`, `package.json`, and `pyproject.toml` to `1.1.1`.

## 5. Documentation
- Documentation reorganized into `user-guide`, `developer-docs`, and `plans`.      
- `README.md` and `README_CN.md` updated with new tool and execution methods.      

## 6. inspect_java_class 增强方案 (2026-01-27)

### 6.1 背景与目标
目前 `inspect_java_class` 无法区分不同版本的相同类，且缓存机制较为简单。为了支持多模块项目的复杂依赖关系，引入了基于 ClassLoader 的隔离机制和基于 GAV (GroupId:ArtifactId:Version) 的智能缓存。

### 6.2 核心机制
- **工作空间识别**：如果被解析的类位于当前工作目录下的子模块中，返回 `status: LOCAL_SOURCE` 并提供 `sourcePath`，引导 Agent 使用 `read_file` 直接阅读源码。
- **ClassLoader 隔离**：为每个 `ModuleContext` 创建独立的 `URLClassLoader`，确保类加载的上下文与 Maven 依赖树一致。
- **GAV 共享缓存**：
    - 对于第三方依赖（位于 `.m2/repository`），使用 `gav:g:a:v:className` 作为缓存 Key。
    - 不同子模块如果引用了相同版本的依赖，将共享缓存结果。
- **缓存跳过**：支持 `bypassCache` 参数，允许强制重新解析。

## 7. search_java_class 游标分页方案 (2026-01-27)

### 7.1 背景与目标
目前的搜索仅支持 `limit`，在结果较多时会导致 Agent 无法获取后续内容。通过引入游标（Cursor），实现稳定、高效的分页。

### 7.2 核心机制
- **确定性排序**：搜索结果按 `className` 字典序进行全局排序。
- **游标定义**：游标是一个 Base64 编码的 JSON 字符串，包含 `lastClassName`（上一页最后一个类名）。
- **状态追踪**：每次搜索返回 `nextCursor`，Agent 可以将其传入下一次请求以获取下一页。
