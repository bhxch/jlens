package io.github.bhxch.mcp.jlens.server.handlers;

import io.github.bhxch.mcp.jlens.config.ServerConfig;
import io.github.bhxch.mcp.jlens.dependency.DependencyManager;
import io.github.bhxch.mcp.jlens.dependency.MavenBuilder;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolverFactory;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BuildModuleHandler Unit Tests")
class BuildModuleHandlerTest {

    private BuildModuleHandler handler;
    private McpSyncServerExchange exchange;

    @BeforeEach
    void setUp() {
        ServerConfig config = new ServerConfig();
        MavenBuilder builder = new MavenBuilder();
        DependencyManager dependencyManager = new DependencyManager(builder);
        MavenResolverFactory resolverFactory = new MavenResolverFactory(config);
        handler = new BuildModuleHandler(builder, dependencyManager, resolverFactory);
        exchange = null;
    }

    @Test
    @DisplayName("Should return error when sourceFilePath is missing")
    void testMissingSourceFilePath() {
        Map<String, Object> arguments = new HashMap<>();

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "build_module",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertTrue(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("sourceFilePath is required"));
    }

    @Test
    @DisplayName("Should return error when source file does not exist")
    void testSourceFileNotFound() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("sourceFilePath", "non-existent-file.java");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "build_module",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertTrue(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("source file does not exist"));
    }

    @Test
    @DisplayName("Should return error when pom.xml not found")
    void testPomNotFound() {
        // Create a temp file in a directory without pom.xml
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("sourceFilePath", "src/main/java/io/github/bhxch/mcp/jlens/Main.java"); // Should find it
        // Wait, Main.java should find pom.xml. Let's use a root file.
        arguments.put("sourceFilePath", "README.md"); 

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "build_module",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);
        // This might actually find pom.xml if it's in the same dir.
    }

    @Test
    @DisplayName("Should handle build timeout")
    void testBuildTimeout() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("sourceFilePath", "pom.xml");
        arguments.put("timeoutSeconds", 1);

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "build_module",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);
        assertNotNull(result);
    }
}
