# JLens MCP Server

A Model Context Protocol (MCP) server for inspecting Java classes and Maven dependencies.

## Quick Execution

You can run JLens instantly using `npx` or `uvx` (requires Java 17+ installed):

```bash
# Using npx
npx -y jlens-mcp-server

# Using uvx
uvx jlens-mcp-server
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

- **Test Reports**: [reports/](reports/) - Test results and reports
- **Scripts**: [scripts/](scripts/) - Test and automation scripts
- **Configuration**: [config/](config/) - MCP Inspector and other configs
- **Logs**: [logs/](logs/) - Temporary logs and outputs

## Build & Run

### Build

```bash
mvn clean package
```

This will create an executable JAR file: `target/jlens-mcp-server-1.1.0.jar`

## License

MIT