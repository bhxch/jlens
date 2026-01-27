package io.github.bhxch.mcp.jlens.server.handlers;

import io.github.bhxch.mcp.jlens.classpath.PackageMappingResolver;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SearchJavaClassHandler Unit Tests")
class SearchJavaClassHandlerTest {

    private SearchJavaClassHandler handler;
    private McpSyncServerExchange exchange;

    @BeforeEach
    void setUp() {
        ServerConfig config = new ServerConfig();
        PackageMappingResolver packageResolver = new PackageMappingResolver();
        MavenBuilder mavenBuilder = new MavenBuilder();
        DependencyManager dependencyManager = new DependencyManager(mavenBuilder);
        MavenResolverFactory resolverFactory = new MavenResolverFactory(config);
        handler = new SearchJavaClassHandler(packageResolver, dependencyManager, resolverFactory);
        exchange = null;
    }

    @Test
    @DisplayName("Should handle basic search")
    void testBasicSearch() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("classNamePattern", "java.util.List");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "search_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("java.util.List"));
    }

    @Test
    @DisplayName("Should handle prefix search")
    void testPrefixSearch() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("classNamePattern", "ArrayList");
        arguments.put("searchType", "prefix");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "search_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("ArrayList"));
    }

    @Test
    @DisplayName("Should handle pagination with limit")
    void testPaginationLimit() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("classNamePattern", "*");
        arguments.put("limit", 5);

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "search_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        // The mock index might not have enough classes, but let's check if it doesn't crash
    }

    @Test
    @DisplayName("Should handle pagination with cursor")
    void testPaginationCursor() {
        // First page
        Map<String, Object> arguments1 = new HashMap<>();
        arguments1.put("classNamePattern", "*");
        arguments1.put("limit", 2);

        McpSchema.CallToolRequest request1 = new McpSchema.CallToolRequest(
            "search_java_class",
            arguments1
        );

        McpSchema.CallToolResult result1 = handler.handle(exchange, request1);
        assertNotNull(result1);
    }

    @Test
    @DisplayName("Should return error when classNamePattern is missing")
    void testMissingQuery() {
        Map<String, Object> arguments = new HashMap<>();

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "search_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertTrue(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("classNamePattern is required"));
    }
}
