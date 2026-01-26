# JavaStub MCP Server

A Model Context Protocol (MCP) server for inspecting Java classes and Maven dependencies.

## Quick Links

- **Documentation**: [docs/](docs/) - All project documentation
- **Test Reports**: [eports/](reports/) - Test results and reports
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

## Build & Run

### Build
`ash
mvn clean package
`

### Run
`ash
java -jar target/javastub-mcp-server-1.0.0-SNAPSHOT.jar
`

## Test Coverage

- **Total Tests**: 71/71 (100%)
- End-to-End Tests: 25/25 (100%)
- Integration Tests: 19/19 (100%)
- MCP Client Tests: 19/19 (100%)
- MCP Inspector Tests: 8/8 (100%)

See [eports/](reports/) for detailed test results.

## License

MIT License
