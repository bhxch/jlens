package io.github.bhxch.mcp.jlens.unit.config;

import io.github.bhxch.mcp.jlens.config.DecompilerConfig;
import io.github.bhxch.mcp.jlens.config.MavenConfig;
import io.github.bhxch.mcp.jlens.config.ServerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Config Unit Tests")
class ConfigTest {

    @Test
    @DisplayName("Should test ServerConfig methods")
    void testServerConfig() {
        ServerConfig config = new ServerConfig();
        config.setLogLevel("DEBUG");
        config.setMavenExecutable(Path.of("mvn"));
        config.setVirtualThreadCount(20);
        config.setCacheSize(500);
        config.setCacheTtlSeconds(7200);

        assertEquals("DEBUG", config.getLogLevel());
        assertEquals(Path.of("mvn"), config.getMavenExecutable());
        assertEquals(20, config.getVirtualThreadCount());
        assertEquals(500, config.getCacheSize());
        assertEquals(7200, config.getCacheTtlSeconds());
    }

    @Test
    @DisplayName("Should test DecompilerConfig methods")
    void testDecompilerConfig() {
        DecompilerConfig config = new DecompilerConfig();
        config.setDecompilerType(DecompilerConfig.DecompilerType.CFR);
        config.setIncludeLineNumbers(true);
        config.setShowSyntheticMembers(false);

        assertEquals(DecompilerConfig.DecompilerType.CFR, config.getDecompilerType());
        assertTrue(config.isIncludeLineNumbers());
        assertFalse(config.isShowSyntheticMembers());
    }

    @Test
    @DisplayName("Should test MavenConfig methods")
    void testMavenConfig() {
        MavenConfig config = new MavenConfig();
        config.setExecutable(Path.of("mvn"));
        config.setOfflineMode(true);
        config.setFailFast(true);

        assertEquals(Path.of("mvn"), config.getExecutable());
        assertTrue(config.isOfflineMode());
        assertTrue(config.isFailFast());
    }

    @Test
    @DisplayName("Should parse command line args")
    void testFromCommandLine() {
        String[] args = {"--virtual-threads", "15", "--port", "9090"};
        ServerConfig config = ServerConfig.fromCommandLine(args);
        assertEquals(15, config.getVirtualThreadCount());
        assertEquals(9090, config.getServerPort());
    }
}
