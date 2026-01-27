# JLens v1.1.1 Upgrade Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Upgrade JLens to v1.1.1 with JDK 25+ support, bundled distribution (no GitHub downloads), and explicit Maven context for all tools.

**Architecture:**
- **Java Core:** Update `pom.xml` for JDK 25 and update dependencies. Refactor all Handlers to accept `pomFilePath` and `mavenProfile`.
- **Wrappers (JS/Python):** Remove download logic. Expect JAR to be present relative to the script. Add Env Var support (`JLENS_JAVA_HOME`, `JLENS_ARGS`).
- **Distribution:** Update build process to copy the uber-JAR into the wrapper directories before packaging.

**Tech Stack:** Java 25, Maven, Node.js, Python, JUnit 5.

---

### Task 1: Foundation - POM & Dependencies

**Files:**
- Modify: `pom.xml`

**Step 1: Update POM properties**
Update `maven.compiler.release` to 25.
Update Mockito, ByteBuddy, JaCoCo to latest versions known (checking Context7 or common knowledge if needed, or just bump to latest stable).
*Note: If local JDK < 25, the build might fail. We will attempt to use the highest available.*

**Step 2: Verify Dependencies**
Run `mvn clean validate` to check dependency resolution.

**Step 3: Commit**
`build(deps): Upgrade to JDK 25 and update testing dependencies`

---

### Task 2: Core Logic - Tool Arguments

**Files:**
- Modify: `src/main/java/io/github/bhxch/mcp/jlens/server/handlers/InspectJavaClassHandler.java`
- Modify: `src/main/java/io/github/bhxch/mcp/jlens/server/handlers/ListClassFieldsHandler.java`
- Modify: `src/main/java/io/github/bhxch/mcp/jlens/server/handlers/ListModuleDependenciesHandler.java`
- Modify: `src/main/java/io/github/bhxch/mcp/jlens/server/handlers/SearchJavaClassHandler.java`
- Modify: `src/main/java/io/github/bhxch/mcp/jlens/server/handlers/BuildModuleHandler.java`

**Step 1: Refactor Handlers**
Add `pomFilePath` (String, optional) and `mavenProfile` (String, optional) to `CallToolRequest` argument parsing in all handlers.
Pass these values to the `MavenResolver` or `ModuleContext`.

**Step 2: Update Unit Tests**
Update corresponding tests in `src/test/java/...` to include these new parameters (even if null).

**Step 3: Commit**
`feat(core): Add pomFilePath and mavenProfile arguments to all tools`

---

### Task 3: Distribution - Node.js Wrapper

**Files:**
- Modify: `bin/jlens-mcp-server.js`
- Modify: `package.json`

**Step 1: Remove Download Logic**
Remove `downloadJar` and `https` dependency.
Change `findLocalJar` to look strictly in `__dirname` (same folder as script) or `../target` (for dev).

**Step 2: Add Env Var Support**
Read `process.env.JLENS_JAVA_HOME` -> `javaPath`.
Read `process.env.JLENS_ARGS` -> Parse into `mcpArgs` (handling quotes/spaces if possible, or just split).
Pass-through args: Ensure `npx jlens-mcp-server -Dfoo=bar` works.

**Step 3: Update package.json**
Add the JAR to the `files` array (assuming it will be copied there during build).
Update version to `1.1.1`.

**Step 4: Commit**
`feat(cli): Update Node.js wrapper for bundling and env vars`

---

### Task 4: Distribution - Python Wrapper

**Files:**
- Modify: `jlens_mcp_server/main.py`
- Modify: `pyproject.toml`

**Step 1: Remove Download Logic**
Remove `urllib` download logic.
Look for jar in `Path(__file__).parent`.

**Step 2: Add Env Var Support**
`os.environ.get("JLENS_JAVA_HOME")`.
`os.environ.get("JLENS_ARGS")`.

**Step 3: Update pyproject.toml**
Ensure the JAR is included in the package data.
Update version to `1.1.1`.

**Step 4: Commit**
`feat(cli): Update Python wrapper for bundling and env vars`

---

### Task 5: Integration Testing & Verification

**Files:**
- Create: `scripts/test_integration_advanced.ps1`
- Create: `config/test_config.json` (optional)

**Step 1: Create Advanced Test Script**
Script should:
1. `mvn clean package` (Build the JAR).
2. Copy JAR to `bin/` and `jlens_mcp_server/`.
3. Download a small demo Maven project (e.g., a simple creating one) to `.temp/`.
4. Run the Node wrapper against it.
5. Run the Python wrapper against it.
6. Verify tools accept `pomFilePath`.

**Step 2: Run Verification**
Execute the script.

**Step 3: Commit**
`test(integration): Add advanced integration test suite`

---

### Task 6: Documentation & Cleanup

**Files:**
- Modify: `README.md`
- Modify: `docs/user-guide/README.md`
- Modify: `docs/developer-docs/PROJECT_SUMMARY.md`

**Step 1: Update Docs**
Document the new `pomFilePath` arguments.
Document `JLENS_JAVA_HOME` and `JLENS_ARGS`.
Update version references.

**Step 2: Organize Files**
Run `file-organizer`.

**Step 3: Final Commit**
`docs: Update documentation for v1.1.1 release`

---

### Task 7: Release Prep

**Files:**
- Modify: `pom.xml` (Ensure version is 1.1.1)

**Step 1: Tag**
(Will be done via git command manually after plan execution).

