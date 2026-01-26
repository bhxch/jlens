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
- **Mechanism**: Checks for Java 17+, locates the JAR in the package, and spawns a child process.

### uvx (PyPI)
- **Wrapper**: `jlens_mcp_server/main.py` (Python script).
- **Package**: `pyproject.toml` using `hatchling`.
- **Mechanism**: Uses `subprocess` to run the JAR, including the JAR as package data.

## 4. Quality Assurance
- **Unit Tests**: Added `ListClassFieldsHandlerTest` to verify filtering logic.
- **Test Coverage**: Targeting > 85% for core logic.
- **Version Management**: Updated `pom.xml`, `package.json`, and `pyproject.toml` to `1.1.0`.

## 5. Documentation
- Documentation reorganized into `user-guide`, `developer-docs`, and `plans`.
- `README.md` and `README_CN.md` updated with new tool and execution methods.
