package io.github.bhxch.mcp.jlens.server.handlers;

import io.github.bhxch.mcp.jlens.config.DecompilerConfig;
import io.github.bhxch.mcp.jlens.config.ServerConfig;
import io.github.bhxch.mcp.jlens.decompiler.DecompilerFactory;
import io.github.bhxch.mcp.jlens.inspector.ClassInspector;
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

/**
 * Extended tests for ListClassFieldsHandler covering edge cases
 */
@DisplayName("ListClassFieldsHandler Extended Tests")
class ListClassFieldsHandlerExtendedTest {

    private ListClassFieldsHandler handler;
    private McpSyncServerExchange exchange;

    @BeforeEach
    void setUp() {
        ServerConfig config = new ServerConfig();

        DecompilerConfig decompilerConfig = new DecompilerConfig();
        decompilerConfig.setDecompilerType(DecompilerConfig.DecompilerType.VINEFLOWER);
        config.setDecompilerConfig(decompilerConfig);

        ClassInspector inspector = new ClassInspector(
            DecompilerFactory.createDecompiler(config.getDecompilerConfig())
        );

        MavenResolverFactory resolverFactory = new MavenResolverFactory(config);

        handler = new ListClassFieldsHandler(inspector, resolverFactory);
        exchange = null;
    }

    @Test
    @DisplayName("Should list interface fields")
    void testInterfaceFields() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.sql.Connection");
        arguments.put("pomFilePath", "pom.xml");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_class_fields",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        // Connection interface has fields like TRANSACTION_NONE, TRANSACTION_READ_UNCOMMITTED
        assertTrue(content.contains("TRANSACTION"));
    }

    @Test
    @DisplayName("Should handle enum fields")
    void testEnumFields() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.lang.Thread$State");
        arguments.put("pomFilePath", "pom.xml");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_class_fields",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        // Enums have enum constants which are fields
    }

    @Test
    @DisplayName("Should handle non-existent class")
    void testNonExistentClass() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "com.nonexistent.Class");
        arguments.put("pomFilePath", "pom.xml");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_class_fields",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        // Should handle gracefully
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should filter by protected visibility")
    void testProtectedVisibility() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.util.AbstractList");
        arguments.put("pomFilePath", "pom.xml");
        arguments.put("visibility", List.of("protected"));

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_class_fields",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        // AbstractList has protected fields
        assertNotNull(content);
    }

    @Test
    @DisplayName("Should filter by package-private visibility")
    void testPackagePrivateVisibility() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.lang.String");
        arguments.put("pomFilePath", "pom.xml");
        arguments.put("visibility", List.of("package-private"));

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_class_fields",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should handle all visibility filters combined")
    void testAllVisibilityFilters() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.util.HashMap");
        arguments.put("pomFilePath", "pom.xml");
        arguments.put("visibility", List.of("public", "protected", "private", "package-private"));

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_class_fields",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertNotNull(content);
    }

    @Test
    @DisplayName("Should handle empty visibility filter")
    void testEmptyVisibilityFilter() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.util.ArrayList");
        arguments.put("pomFilePath", "pom.xml");
        arguments.put("visibility", List.of());

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_class_fields",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        // Empty filter should return all fields
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertNotNull(content);
    }

    @Test
    @DisplayName("Should handle classes with no fields")
    void testClassWithNoFields() {
        Map<String, Object> arguments = new HashMap<>();
        // Some interfaces or abstract classes may have no declared fields
        arguments.put("className", "java.lang.Runnable");
        arguments.put("pomFilePath", "pom.xml");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_class_fields",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        // Should handle gracefully even if no fields
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should handle profiles parameter")
    void testProfilesParameter() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.util.List");
        arguments.put("pomFilePath", "pom.xml");
        arguments.put("profiles", List.of("dev", "test"));

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_class_fields",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertNotNull(content);
    }

    @Test
    @DisplayName("Should handle annotation types")
    void testAnnotationFields() {
        Map<String, Object> arguments = new HashMap<>();
        // Some annotations may have fields (methods with default values)
        arguments.put("className", "java.lang.SuppressWarnings");
        arguments.put("pomFilePath", "pom.xml");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_class_fields",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        // Annotations don't typically have fields, should handle gracefully
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should return error for missing pomFilePath")
    void testMissingPomFilePath() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.util.List");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_class_fields",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertTrue(result.isError());
        assertTrue(((McpSchema.TextContent) result.content().get(0)).text().contains("pomFilePath is required"));
    }
}
