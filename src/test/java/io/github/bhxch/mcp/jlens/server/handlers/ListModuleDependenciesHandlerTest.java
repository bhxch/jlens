package io.github.bhxch.mcp.jlens.server.handlers;

import io.github.bhxch.mcp.jlens.config.DecompilerConfig;
import io.github.bhxch.mcp.jlens.config.MavenConfig;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ListModuleDependenciesHandler
 */
@DisplayName("ListModuleDependenciesHandler Tests")
class ListModuleDependenciesHandlerTest {

    private ListModuleDependenciesHandler handler;
    private McpSyncServerExchange exchange;
    private Path testPomFile;

    @BeforeEach
    void setUp() {
        ServerConfig config = new ServerConfig();
        
        MavenResolverFactory resolverFactory = new MavenResolverFactory(config);
        
        handler = new ListModuleDependenciesHandler(resolverFactory);
        exchange = null; // Not used in current implementation
        
        // Use the actual pom.xml file for testing
        testPomFile = Paths.get("pom.xml");
    }

    @Test
    @DisplayName("Should handle valid dependency list request with pom file")
    void testHandleValidDependencyListWithPomFile() {
        if (!testPomFile.toFile().exists()) {
            return; // Skip test if pom.xml doesn't exist
        }

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("pomFilePath", testPomFile.toString());
        arguments.put("scope", "compile");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_module_dependencies",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        assertEquals(1, result.content().size());
        
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("\"module\""));
        assertTrue(content.contains("\"dependencies\""));
    }

    @Test
    @DisplayName("Should return error when neither path is provided")
    void testHandleNoPathProvided() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("scope", "compile");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_module_dependencies",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertTrue(result.isError());
        
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("Either sourceFilePath or pomFilePath must be provided"));
    }

    @Test
    @DisplayName("Should return error when pom file not found")
    void testHandlePomFileNotFound() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("pomFilePath", "invalid_pom.xml");
        arguments.put("scope", "compile");

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_module_dependencies",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertTrue(result.isError());
        
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("pom.xml file not found"));
    }

    @Test
    @DisplayName("Should handle dependency list with different scopes")
    void testHandleDifferentScopes() {
        if (!testPomFile.toFile().exists()) {
            return; // Skip test if pom.xml doesn't exist
        }

        String[] scopes = {"compile", "test", "provided", "runtime"};
        
        for (String scope : scopes) {
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("pomFilePath", testPomFile.toString());
            arguments.put("scope", scope);

            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
                "list_module_dependencies",
                arguments
            );

            McpSchema.CallToolResult result = handler.handle(exchange, request);

            assertNotNull(result);
            assertFalse(result.isError());
            
            String content = ((McpSchema.TextContent) result.content().get(0)).text();
            assertTrue(content.contains("\"module\""));
            assertTrue(content.contains("\"dependencies\""));
        }
    }

    @Test
    @DisplayName("Should handle dependency list with default scope")
    void testHandleDefaultScope() {
        if (!testPomFile.toFile().exists()) {
            return; // Skip test if pom.xml doesn't exist
        }

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("pomFilePath", testPomFile.toString());

        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "list_module_dependencies",
            arguments
        );

        McpSchema.CallToolResult result = handler.handle(exchange, request);

        assertNotNull(result);
        assertFalse(result.isError());
        
        String content = ((McpSchema.TextContent) result.content().get(0)).text();
        assertTrue(content.contains("\"module\""));
        assertTrue(content.contains("\"dependencies\""));
    }
}



