package io.github.bhxch.mcp.jlens.server.handlers;

import io.github.bhxch.mcp.jlens.cache.CacheManager;
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
 * Extended tests for InspectJavaClassHandler covering edge cases and error scenarios
 */
@DisplayName("InspectJavaClassHandler Extended Tests")
class InspectJavaClassHandlerExtendedTest {

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
        exchange = null;
    }

    @Test
    @DisplayName("Should handle non-existent class gracefully")
    void testNonExistentClass() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "com.nonexistent.Class");
        arguments.put("pomFilePath", "pom.xml");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "inspect_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        // Should not throw error, but return stub metadata
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("com.nonexistent.Class"));
    }

    @Test
    @DisplayName("Should handle non-existent pom file")
    void testNonExistentPomFile() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.util.List");
        arguments.put("pomFilePath", "non_existent_pom.xml");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "inspect_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        // May or may not error depending on implementation, but should not crash
        assertNotNull(result.content());
    }

    @Test
    @DisplayName("Should handle interface inspection")
    void testInterfaceInspection() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.util.Collection");
        arguments.put("pomFilePath", "pom.xml");
        arguments.put("detailLevel", "full");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "inspect_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("java.util.Collection"));
        assertTrue(content.contains("interface") || content.contains("Interface"));
    }

    @Test
    @DisplayName("Should handle annotation inspection")
    void testAnnotationInspection() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.lang.Override");
        arguments.put("pomFilePath", "pom.xml");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "inspect_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("Override"));
    }

    @Test
    @DisplayName("Should handle enum inspection")
    void testEnumInspection() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.lang.Thread$State");
        arguments.put("pomFilePath", "pom.xml");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "inspect_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("State"));
    }

    @Test
    @DisplayName("Should handle inspection with profiles")
    void testInspectionWithProfiles() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.util.List");
        arguments.put("pomFilePath", "pom.xml");
        arguments.put("profiles", List.of("dev", "test"));

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "inspect_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("java.util.List"));
    }

    @Test
    @DisplayName("Should handle bypass cache flag")
    void testBypassCache() {
        // First call to populate cache
        Map<String, Object> arguments1 = new HashMap<>();
        arguments1.put("className", "java.util.HashMap");
        arguments1.put("pomFilePath", "pom.xml");
        arguments1.put("bypassCache", false);

        McpSchema.CallToolRequest request1 = new McpSchema.CallToolRequest(
            "inspect_java_class",
            arguments1
        );

        McpSchema.CallToolResult result1 = handler.handle(exchange, request1);
        assertNotNull(result1);
        assertFalse(result1.isError());

        // Second call with bypass cache
        Map<String, Object> arguments2 = new HashMap<>();
        arguments2.put("className", "java.util.HashMap");
        arguments2.put("pomFilePath", "pom.xml");
        arguments2.put("bypassCache", true);

        McpSchema.CallToolRequest request2 = new McpSchema.CallToolRequest(
            "inspect_java_class",
            arguments2
        );

        McpSchema.CallToolResult result2 = handler.handle(exchange, request2);
        assertNotNull(result2);
        assertFalse(result2.isError());
        String content = ((McpSchema.TextContent) result2.content().get(0)).text();
        assertTrue(content.contains("java.util.HashMap"));
    }

    @Test
    @DisplayName("Should handle javaHome parameter (even if src.zip missing)")
    void testJavaHomeParameter() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.util.List");
        arguments.put("pomFilePath", "pom.xml");
        arguments.put("javaHome", System.getProperty("java.home"));

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "inspect_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("java.util.List"));
    }

    @Test
    @DisplayName("Should handle primitive wrapper classes")
    void testPrimitiveWrappers() {
        String[] primitives = {"java.lang.Integer", "java.lang.Boolean", "java.lang.Double"};

        for (String className : primitives) {
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("className", className);
            arguments.put("pomFilePath", "pom.xml");

            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
                "inspect_java_class",
                arguments
            );

            McpSchema.CallToolResult result = handler.handle(exchange, request);

            assertNotNull(result);
            assertFalse(result.isError());
            String content = ((McpSchema.TextContent) result.content().get(0)).text();
            assertTrue(content.contains(className.substring(className.lastIndexOf('.') + 1)));
        }
    }

    @Test
    @DisplayName("Should handle inner classes")
    void testInnerClasses() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "java.util.Map$Entry");
        arguments.put("pomFilePath", "pom.xml");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "inspect_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("Entry") || content.contains("Map"));
    }

    @Test
    @DisplayName("Should handle array classes")
    void testArrayClasses() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", "[Ljava.lang.String;");
        arguments.put("pomFilePath", "pom.xml");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "inspect_java_class",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        // Array classes may return error or special handling
        assertNotNull(result.content());
    }
}
