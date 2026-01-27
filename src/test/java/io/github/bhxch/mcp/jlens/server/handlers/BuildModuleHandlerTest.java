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
    @DisplayName("Should return error when pomFilePath is missing")
    void testMissingPomFilePath() {
        Map<String, Object> arguments = new HashMap<>();

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "build_module",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertTrue(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("pomFilePath is required"));
    }

    @Test
    @DisplayName("Should return error when pom file does not exist")
    void testPomFileNotFound() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("pomFilePath", "non-existent-pom.xml");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "build_module",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertTrue(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("pom.xml does not exist"));
    }

    @Test
    @DisplayName("Should handle build timeout")
    void testBuildTimeout() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("pomFilePath", "pom.xml");
        arguments.put("timeoutSeconds", 1);

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "build_module",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);
        assertNotNull(result);
    }
}
