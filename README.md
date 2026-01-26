# JLens MCP Server

A Model Context Protocol (MCP) server for inspecting Java classes and Maven dependencies.

## Quick Links

- **Documentation**: [docs/](docs/) - All project documentation
- **Test Reports**: [reports/](reports/) - Test results and reports
- **Scripts**: [scripts/](scripts/) - Test and automation scripts
- **Configuration**: [config/](config/) - MCP Inspector and other configs
- **Logs**: [logs/](logs/) - Temporary logs and outputs

## Main Documentation

### Getting Started

- [README (English)](docs/README.md)
- [README (中文)](docs/README_CN.md)

### Testing

- [Testing Guide](docs/TESTING.md)
- [Testing Guide (中文)](docs/TESTING_CN.md)

### Integration

- [iFlow CLI Integration](docs/iflow_mcp.md)
- [iFlow CLI Integration (中文)](docs/iflow_mcp_CN.md)
- [MCP Inspector Integration Guide](docs/MCP_INSPECTOR_INTEGRATION_GUIDE.md)

### Project Status

- [Project Summary](docs/PROJECT_SUMMARY.md)
- [Project Summary (中文)](docs/PROJECT_SUMMARY_CN.md)
- [Final Summary](docs/FINAL_SUMMARY.md)
- [Final Summary (中文)](docs/FINAL_SUMMARY_CN.md)

## Client Configuration

This MCP server can be used with various MCP-compatible clients.

### Claude Desktop

Add this to your `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "jlens": {
      "command": "java",
      "args": ["-jar", "/path/to/jlens/target/jlens-mcp-server-1.0.0-SNAPSHOT.jar"]
    }
  }
}
```

### Gemini CLI

Add this to your `.gemini/settings.json`:

```json
{
  "mcpServers": {
    "jlens": {
      "command": "java",
      "args": ["-jar", "/path/to/jlens/target/jlens-mcp-server-1.0.0-SNAPSHOT.jar"]
    }
  }
}
```

### Cursor

Settings -> Models -> MCP -> Add New MCP Server:

- **Name**: jlens
- **Type**: command
- **Command**: `java -jar /path/to/jlens/target/jlens-mcp-server-1.0.0-SNAPSHOT.jar`

### Cline (VS Code Extension)

MCP Settings -> Add Server:

- **Name**: jlens
- **Command**: `java`
- **Args**: `["-jar", "/path/to/jlens/target/jlens-mcp-server-1.0.0-SNAPSHOT.jar"]`

### iFlow CLI

```bash
iflow mcp add jlens-mcp-server "java -jar /path/to/jlens/target/jlens-mcp-server-1.0.0-SNAPSHOT.jar" --trust
```

## Build & Run

### Build

```bash
mvn clean package
```

### Run

```bash
java -jar target/jlens-mcp-server-1.0.0-SNAPSHOT.jar
```

## Test Coverage

- **Total Tests**: 71/71 (100%)
- End-to-End Tests: 25/25 (100%)
- Integration Tests: 19/19 (100%)
- MCP Client Tests: 19/19 (100%)
- MCP Inspector Tests: 8/8 (100%)

See [reports/](reports/) for detailed test results.

## License

MIT License
