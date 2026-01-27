package io.github.bhxch.mcp.jlens.server.handlers;

import io.github.bhxch.mcp.jlens.cache.CacheManager;
import io.github.bhxch.mcp.jlens.config.DecompilerConfig;
import io.github.bhxch.mcp.jlens.config.MavenConfig;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InspectJavaClassHandler
 */
@DisplayName("InspectJavaClassHandler Tests")
class InspectJavaClassHandlerTest {

    private InspectJavaClassHandler handler;
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
        CacheManager cacheManager = new CacheManager(config);
        
        handler = new InspectJavaClassHandler(inspector, resolverFactory, cacheManager);
        exchange = null; // Not used in current implementation
    }

    @Test
    @DisplayName("Should handle valid class inspection request")
    void testHandleValidClassInspection() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.util.List");
        arguments.put("detailLevel", "basic");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "inspect_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        assertEquals(1, result.content().size());
        
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("java.util.List"));
    }

    @Test
    @DisplayName("Should return error when className is missing")
    void testHandleMissingClassName() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("detailLevel", "basic");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "inspect_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertTrue(result.isError());
        
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("className is required"));
    }

    @Test
    @DisplayName("Should return error when className is empty")
    void testHandleEmptyClassName() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "");
        arguments.put("detailLevel", "basic");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "inspect_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertTrue(result.isError());
        
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("className is required"));
    }

    @Test
    @DisplayName("Should handle class inspection with different detail levels")
    void testHandleDifferentDetailLevels() {
        String[] detailLevels = {"skeleton", "basic", "full"};
        
        for (String detailLevel : detailLevels) {
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("className", "java.util.ArrayList");
            arguments.put("detailLevel", detailLevel);

            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
                "inspect_java_class",
                arguments
            );

            McpSchema.CallToolResult result = handler.handle(exchange, request);

            assertNotNull(result);
            assertFalse(result.isError());
            
            String content = ((McpSchema.TextContent) result.content().get(0)).text();
            assertTrue(content.contains("java.util.ArrayList"));
        }
    }

    @Test
    @DisplayName("Should handle class inspection with default detail level")
    void testHandleDefaultDetailLevel() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.util.Map");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "inspect_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("java.util.Map"));
    }

    @Test
    @DisplayName("Should handle class inspection with invalid detail level")
    void testHandleInvalidDetailLevel() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.util.Set");
        arguments.put("detailLevel", "invalid");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "inspect_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("java.util.Set"));
    }
}



