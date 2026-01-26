package io.github.bhxch.mcp.jlens.server;

import io.github.bhxch.mcp.jlens.config.DecompilerConfig;
import io.github.bhxch.mcp.jlens.config.MavenConfig;
import io.github.bhxch.mcp.jlens.config.ServerConfig;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JavaClasspathServer
 */
@DisplayName("JavaClasspathServer Tests")
class JavaClasspathServerTest {

    @Test
    @DisplayName("Should create server with valid configuration")
    void testCreateServerWithValidConfig() {
        ServerConfig config = createTestConfig();
        
        assertDoesNotThrow(() -> {
            JavaClasspathServer server = new JavaClasspathServer(config);
            assertNotNull(server.getMcpServer());
        });
    }

    @Test
    @DisplayName("Should create server with null configuration")
    void testCreateServerWithNullConfig() {
        assertThrows(NullPointerException.class, () -> {
            new JavaClasspathServer(null);
        });
    }

    @Test
    @DisplayName("Should start server without errors")
    void testStartServer() {
        ServerConfig config = createTestConfig();
        JavaClasspathServer server = new JavaClasspathServer(config);
        
        assertDoesNotThrow(() -> {
            server.start();
        });
    }

    @Test
    @DisplayName("Should get MCP server instance")
    void testGetMcpServer() {
        ServerConfig config = createTestConfig();
        JavaClasspathServer server = new JavaClasspathServer(config);
        
        assertNotNull(server.getMcpServer());
    }

    private ServerConfig createTestConfig() {
        ServerConfig config = new ServerConfig();
        
        DecompilerConfig decompilerConfig = new DecompilerConfig();
        decompilerConfig.setDecompilerType(DecompilerConfig.DecompilerType.VINEFLOWER);
        config.setDecompilerConfig(decompilerConfig);
        
        return config;
    }
}



