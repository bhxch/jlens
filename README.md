# JLens MCP Server

A Model Context Protocol (MCP) server for inspecting Java classes and Maven dependencies.

## Key Features

- **Java Class Inspection**: Inspect any Java class in your project or dependencies.
- **JDK Visibility**: Support for inspecting JDK classes with `@since` information (requires `javaHome`).
- **Maven Integration**: Deep resolution of Maven dependency graphs, including property placeholders.
- **Granular Detail Levels**: Support for `skeleton`, `basic`, and `full` detail levels in class inspection.
- **Interface Support**: Fully supports listing fields and methods for Java interfaces.

### JDK Inspection with `@since`

To get accurate versioning information for JDK classes, provide the `javaHome` parameter to the `inspect_java_class` tool. JLens will automatically locate `src.zip` and extract Javadoc info.

## Quick Execution

You can run JLens instantly using `npx` or `uvx` (requires Java 25+ installed):

```bash
# Using npx
npx -y @bhxch/jlens-mcp-server

# Using uvx
uvx jlens-mcp-server
```

### Environment Variables

You can configure the server using environment variables:

- `JLENS_JAVA_HOME`: Path to Java installation (overrides JAVA_HOME)
- `JLENS_ARGS`: Additional JVM arguments or server options

```bash
# Example
JLENS_JAVA_HOME="/path/to/jdk-25" JLENS_ARGS="-Dfile.encoding=UTF-8" npx @bhxch/jlens-mcp-server
```

## Agent Skills

Install specialized AI agent skills to help your agent use JLens more effectively (requires `npx skills` support):

```bash
# Install Chinese Version
npx skills install https://github.com/bhxch/jlens/tree/main/skills/jlens-mcp

# Install English Version
npx skills install https://github.com/bhxch/jlens/tree/main/skills/jlens-mcp-en
```

## Documentation

- **User Guide**: [docs/user-guide/](docs/user-guide/)
  - [English README](docs/user-guide/README.md)
  - [中文 README](docs/user-guide/README_CN.md)
  - [Testing Guide](docs/user-guide/TESTING.md)
- **Developer Docs**: [docs/developer-docs/](docs/developer-docs/)
  - [Project Summary](docs/developer-docs/PROJECT_SUMMARY.md)
  - [Integration Guide](docs/developer-docs/MCP_INSPECTOR_INTEGRATION_GUIDE.md)
  - [Contributing Guide](CONTRIBUTING.md) ([中文版](CONTRIBUTING_CN.md))
- **Design Plans**: [docs/plans/](docs/plans/)

## Quick Links

- **Test Reports**: [docs/developer-docs/reports/](docs/developer-docs/reports/) - Test results and reports
- **Scripts**: [scripts/](scripts/) - Test and automation scripts
- **Configuration**: [config/](config/) - MCP Inspector and other configs
- **Logs**: [logs/](logs/) - Temporary logs and outputs

## Build & Run

### Build

```bash
# Recommended for JDK 25+ due to test engine compatibility
mvn clean package -DskipTests
```

This will create an executable JAR file: `target/jlens-mcp-server-1.1.2.jar`

## License

MIT
