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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BuildModuleHandler Extended Tests")
class BuildModuleHandlerExtendedTest {

    private BuildModuleHandler handler;
    private McpSyncServerExchange exchange;

    @BeforeEach
    void setUp() {
        ServerConfig config = new ServerConfig();
        MavenBuilder mavenBuilder = new MavenBuilder();
        DependencyManager dependencyManager = new DependencyManager(mavenBuilder);
        MavenResolverFactory resolverFactory = new MavenResolverFactory(config);
        
        handler = new BuildModuleHandler(mavenBuilder, dependencyManager, resolverFactory);
        exchange = null;
    }

    @Test
    @DisplayName("Should handle missing pomFilePath parameter")
    void testMissingPomFilePath() {
        Map<String, Object> arguments = new HashMap<>();

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "build_module",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertTrue(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("pomFilePath is required"));
    }

    @Test
    @DisplayName("Should handle non-existent POM file")
    void testNonExistentPom() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("pomFilePath", "non_existent_pom.xml");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "build_module",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertTrue(result.isError());
    }

    @Test
    @DisplayName("Should handle valid POM file")
    void testValidPom() {
        Map<String, Object> arguments = new HashMap<>();
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", pomFile.toString());

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "build_module",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        // Build may succeed or fail, but should not crash
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should handle custom goals")
    void testCustomGoals() {
        Map<String, Object> arguments = new HashMap<>();
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", pomFile.toString());
        arguments.put("goals", List.of("clean", "compile"));

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "build_module",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should handle skipTests parameter")
    void testSkipTests() {
        Map<String, Object> arguments = new HashMap<>();
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", pomFile.toString());
        arguments.put("skipTests", true);

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "build_module",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should handle profiles parameter")
    void testProfilesParameter() {
        Map<String, Object> arguments = new HashMap<>();
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", pomFile.toString());
        arguments.put("profiles", List.of("dev"));

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "build_module",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should handle downloadSources parameter")
    void testDownloadSources() {
        Map<String, Object> arguments = new HashMap<>();
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", pomFile.toString());
        arguments.put("downloadSources", true);

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "build_module",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should handle absolute POM path")
    void testAbsolutePomPath() {
        Map<String, Object> arguments = new HashMap<>();
        Path absolutePom = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", absolutePom.toString());

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "build_module",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should handle empty goals list")
    void testEmptyGoals() {
        Map<String, Object> arguments = new HashMap<>();
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", pomFile.toString());
        arguments.put("goals", List.of());

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "build_module",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertNotNull(result.content());
    }
}
