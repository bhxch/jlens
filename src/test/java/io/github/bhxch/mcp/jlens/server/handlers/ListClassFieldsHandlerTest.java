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
 * Unit tests for ListClassFieldsHandler
 */
@DisplayName("ListClassFieldsHandler Tests")
class ListClassFieldsHandlerTest {

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
    @DisplayName("Should list all fields when visibility is empty")
    void testListAllFields() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.lang.String");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_class_fields",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        // java.lang.String has value field (private)
        assertTrue(content.contains("value"));
    }

    @Test
    @DisplayName("Should filter fields by visibility")
    void testFilterByVisibility() {
        // Test with public visibility for a class that has public fields
        // java.lang.reflect.Modifier has many public static final int fields
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.lang.reflect.Modifier");
        arguments.put("visibility", List.of("public"));

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_class_fields",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("PUBLIC"));
        assertTrue(content.contains("PRIVATE"));
        
        // Ensure no private fields are present if we only asked for public
        // Modifier doesn't have private fields usually, let's use String for private
        
        arguments = new HashMap<>();
        arguments.put("className", "java.lang.String");
        arguments.put("visibility", List.of("public"));
        
        request = new McpSchema.CallToolRequest("list_class_fields", arguments);
        result = handler.handle(exchange, request);
        content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertFalse(content.contains("value")); // value is private in String
    }

    @Test
    @DisplayName("Should handle multiple visibility filters")
    void testMultipleVisibilityFilters() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.lang.String");
        arguments.put("visibility", List.of("private", "public"));

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_class_fields",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("value")); // value is private
        assertTrue(content.contains("CASE_INSENSITIVE_ORDER")); // CASE_INSENSITIVE_ORDER is public
    }

    @Test
    @DisplayName("Should return error for missing className")
    void testMissingClassName() {
        Map<String, Object> arguments = new HashMap<>();

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_class_fields",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertTrue(result.isError());
        assertTrue(((McpSchema.TextContent) result.content().get(0)).text().contains("className is required"));
    }
}
