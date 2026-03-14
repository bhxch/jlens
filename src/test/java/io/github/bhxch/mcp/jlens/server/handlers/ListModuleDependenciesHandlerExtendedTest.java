package io.github.bhxch.mcp.jlens.server.handlers;

import io.github.bhxch.mcp.jlens.config.ServerConfig;
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

/**
 * Extended tests for ListModuleDependenciesHandler covering edge cases
 */
@DisplayName("ListModuleDependenciesHandler Extended Tests")
class ListModuleDependenciesHandlerExtendedTest {

    private ListModuleDependenciesHandler handler;
    private McpSyncServerExchange exchange;

    @BeforeEach
    void setUp() {
        ServerConfig config = new ServerConfig();
        MavenResolverFactory resolverFactory = new MavenResolverFactory(config);
        handler = new ListModuleDependenciesHandler(resolverFactory);
        exchange = null;
    }

    @Test
    @DisplayName("Should list dependencies successfully")
    void testListDependencies() {
        Map<String, Object> arguments = new HashMap<>();
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", pomFile.toString());

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_module_dependencies",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertNotNull(content);
    }

    @Test
    @DisplayName("Should handle non-existent POM file")
    void testNonExistentPom() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("pomFilePath", "non_existent_pom.xml");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_module_dependencies",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertTrue(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("does not exist") || content.contains("error") || content.contains("Error"));
    }

    @Test
    @DisplayName("Should handle missing pomFilePath parameter")
    void testMissingPomFilePath() {
        Map<String, Object> arguments = new HashMap<>();

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_module_dependencies",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertTrue(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("pomFilePath is required"));
    }

    @Test
    @DisplayName("Should handle profiles parameter")
    void testProfilesParameter() {
        Map<String, Object> arguments = new HashMap<>();
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", pomFile.toString());
        arguments.put("profiles", List.of("dev", "test"));

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_module_dependencies",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        // Profile may or may not exist, but should not crash
        // The important thing is it handles the parameter gracefully
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should handle different scopes")
    void testDifferentScopes() {
        String[] scopes = {"compile", "test", "runtime", "provided"};
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();

        for (String scope : scopes) {
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("pomFilePath", pomFile.toString());
            arguments.put("scope", scope);

            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
                "list_module_dependencies",
                arguments
            );

            McpSchema.CallToolResult result = handler.handle(exchange, request);

            assertNotNull(result);
            assertFalse(result.isError(), "Should not error for scope: " + scope);
        }
    }

    @Test
    @DisplayName("Should handle empty profiles list")
    void testEmptyProfiles() {
        Map<String, Object> arguments = new HashMap<>();
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", pomFile.toString());
        arguments.put("profiles", List.of());

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_module_dependencies",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
    }

    @Test
    @DisplayName("Should handle relative POM path")
    void testRelativePomPath() {
        Map<String, Object> arguments = new HashMap<>();
        Path relativePom = Paths.get("pom.xml");
        arguments.put("pomFilePath", relativePom.toString());

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_module_dependencies",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        // May or may not error depending on implementation, but should not crash
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should handle absolute POM path")
    void testAbsolutePomPath() {
        Map<String, Object> arguments = new HashMap<>();
        Path absolutePom = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", absolutePom.toString());

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_module_dependencies",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
    }

    @Test
    @DisplayName("Should handle null profiles gracefully")
    void testNullProfiles() {
        Map<String, Object> arguments = new HashMap<>();
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", pomFile.toString());
        arguments.put("profiles", null);

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_module_dependencies",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        // Should handle null gracefully
        assertFalse(result.isError());
    }
}
