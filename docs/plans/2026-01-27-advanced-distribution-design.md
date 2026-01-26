# Design: Advanced Distribution and Contributor Onboarding (2026-01-27)

## 1. Professional Naming
To align with industry standards, the NPM package is moved to a scoped name: `@bhxch/jlens-mcp-server`. This ensures brand consistency and prevents naming collisions in the public registry.

## 2. Dynamic Execution Engine (Node.js & Python)
The distribution wrappers are enhanced to handle scenarios where the core JAR is missing or environment paths are non-standard.

### Key Logic
- **Explicit Paths**: Users can override defaults via `--java-path` and `--jar-path`.
- **Three-Stage Resolution**:
    1. **Local Search**: Look in the installed package directory or development `target/` folder.
    2. **Cache Lookup**: Check `~/.jlens/cache/` for the specific version.
    3. **Cloud Fallback**: Asynchronously download the version-matched JAR from GitHub Releases.
- **Benefits**: Simplifies the first-run experience; users no longer need to manually download JARs when using `npx` or `uvx`.

## 3. Contributor Onboarding
Standardized `CONTRIBUTING.md` and `CONTRIBUTING_CN.md` are added to lower the barrier for new developers.

### Standards Established
- **Technical Baseline**: Java 17+, Maven 3.9+.
- **Commit Rigor**: Mandarin Chinese messages following Sentry/Conventional Commit formats.
- **Quality Gates**: Mandatory 85% test coverage for core logic.
- **Structure**: Clear separation between user guides, technical documentation, and design plans.

## 4. Documentation Hub
The root `README.md` acts as a central portal, pointing users to classified documentation subdirectories, ensuring discoverability.
