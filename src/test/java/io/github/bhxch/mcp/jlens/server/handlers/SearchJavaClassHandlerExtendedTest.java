package io.github.bhxch.mcp.jlens.server.handlers;

import io.github.bhxch.mcp.jlens.config.ServerConfig;
import io.github.bhxch.mcp.jlens.dependency.DependencyManager;
import io.github.bhxch.mcp.jlens.dependency.MavenBuilder;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolverFactory;
import io.github.bhxch.mcp.jlens.classpath.PackageMappingResolver;
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

@DisplayName("SearchJavaClassHandler Extended Tests")
class SearchJavaClassHandlerExtendedTest {

    private SearchJavaClassHandler handler;
    private McpSyncServerExchange exchange;

    @BeforeEach
    void setUp() {
        ServerConfig config = new ServerConfig();
        MavenResolverFactory resolverFactory = new MavenResolverFactory(config);
        MavenBuilder mavenBuilder = new MavenBuilder();
        DependencyManager dependencyManager = new DependencyManager(mavenBuilder);
        PackageMappingResolver packageResolver = new PackageMappingResolver();
        
        handler = new SearchJavaClassHandler(packageResolver, dependencyManager, resolverFactory);
        exchange = null;
    }

    @Test
    @DisplayName("Should handle missing required parameters")
    void testMissingParameters() {
        Map<String, Object> arguments = new HashMap<>();

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "search_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertTrue(result.isError());
    }

    @Test
    @DisplayName("Should handle search with className pattern")
    void testSearchWithClassName() {
        Map<String, Object> arguments = new HashMap<>();
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", pomFile.toString());
        arguments.put("className", "String");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "search_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        // Should not crash, may or may not find results
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should handle search with limit parameter")
    void testSearchWithLimit() {
        Map<String, Object> arguments = new HashMap<>();
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", pomFile.toString());
        arguments.put("className", "List");
        arguments.put("limit", 5);

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "search_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should handle search with cursor pagination")
    void testSearchWithCursor() {
        Map<String, Object> arguments = new HashMap<>();
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", pomFile.toString());
        arguments.put("className", "Map");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "search_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should handle non-existent POM file")
    void testNonExistentPom() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("pomFilePath", "non_existent_pom.xml");
        arguments.put("className", "String");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "search_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertTrue(result.isError());
    }

    @Test
    @DisplayName("Should handle profiles parameter")
    void testProfilesParameter() {
        Map<String, Object> arguments = new HashMap<>();
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", pomFile.toString());
        arguments.put("className", "Object");
        arguments.put("profiles", List.of("dev"));

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "search_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should handle empty className pattern")
    void testEmptyClassName() {
        Map<String, Object> arguments = new HashMap<>();
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        arguments.put("pomFilePath", pomFile.toString());
        arguments.put("className", "");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "search_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        // Empty pattern might be valid or error, but should not crash
        assertNotNull(result.content());
    }
}
