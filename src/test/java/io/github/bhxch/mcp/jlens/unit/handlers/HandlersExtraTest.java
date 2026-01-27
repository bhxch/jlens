package io.github.bhxch.mcp.jlens.unit.handlers;

import io.github.bhxch.mcp.jlens.server.handlers.InspectJavaClassHandler;
import io.github.bhxch.mcp.jlens.inspector.ClassInspector;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolverFactory;
import io.github.bhxch.mcp.jlens.cache.CacheManager;
import io.github.bhxch.mcp.jlens.config.ServerConfig;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Handlers Extra Unit Tests")
class HandlersExtraTest {

    private InspectJavaClassHandler inspectHandler;
    private ClassInspector mockInspector;
    private MavenResolverFactory mockResolverFactory;
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        mockInspector = mock(ClassInspector.class);
        mockResolverFactory = mock(MavenResolverFactory.class);
        cacheManager = new CacheManager(new ServerConfig());
        inspectHandler = new InspectJavaClassHandler(mockInspector, mockResolverFactory, cacheManager);
    }

    @Test
    @DisplayName("Should handle missing className in inspect_java_class")
    void testInspectMissingClassName() {
        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest("inspect_java_class", Map.of());
        
        McpSchema.CallToolResult result = inspectHandler.handle(null, request);
        
        assertTrue(result.isError());
        assertTrue(result.content().get(0) instanceof McpSchema.TextContent);
        assertTrue(((McpSchema.TextContent)result.content().get(0)).text().contains("className"));
    }
}
