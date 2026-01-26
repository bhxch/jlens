package io.github.bhxch.mcp.jlens.unit.config;

import io.github.bhxch.mcp.jlens.config.ServerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ServerConfig Unit Tests")
class ServerConfigTest {

    @Test
    @DisplayName("Should create default config with correct defaults")
    void testDefaultConfig() {
        ServerConfig config = new ServerConfig();

        assertEquals(1000, config.getVirtualThreadCount());
        assertEquals(Runtime.getRuntime().availableProcessors(), config.getPlatformThreadCount());
        assertTrue(config.isEnableVirtualThreads());
        assertEquals(30, config.getRequestTimeoutSeconds());
        assertEquals(60, config.getParallelTaskTimeoutSeconds());
        assertEquals(80, config.getMinTestCoverage());
        assertTrue(config.isEnforceCoverage());
        assertEquals("INFO", config.getLogLevel());
        assertEquals(8080, config.getServerPort());
        assertEquals("localhost", config.getServerHost());
    }

    @Test
    @DisplayName("Should set and get virtual thread count")
    void testVirtualThreadCount() {
        ServerConfig config = new ServerConfig();
        config.setVirtualThreadCount(500);

        assertEquals(500, config.getVirtualThreadCount());
    }

    @Test
    @DisplayName("Should set and get platform thread count")
    void testPlatformThreadCount() {
        ServerConfig config = new ServerConfig();
        config.setPlatformThreadCount(8);

        assertEquals(8, config.getPlatformThreadCount());
    }

    @Test
    @DisplayName("Should set and get cache size")
    void testCacheSize() {
        ServerConfig config = new ServerConfig();
        config.setCacheSize(2000);

        assertEquals(2000, config.getCacheSize());
    }

    @Test
    @DisplayName("Should set and get cache TTL")
    void testCacheTtl() {
        ServerConfig config = new ServerConfig();
        config.setCacheTtlSeconds(7200);

        assertEquals(7200, config.getCacheTtlSeconds());
    }

    @Test
    @DisplayName("Should parse command line arguments correctly")
    void testCommandLineParsing() {
        String[] args = {
            "--virtual-threads", "500",
            "--port", "9000",
            "--log-level", "DEBUG"
        };

        ServerConfig config = ServerConfig.fromCommandLine(args);

        assertEquals(500, config.getVirtualThreadCount());
        assertEquals(9000, config.getServerPort());
        assertEquals("DEBUG", config.getLogLevel());
    }

    @Test
    @DisplayName("Should handle short form arguments")
    void testShortFormArguments() {
        String[] args = {
            "-vt", "100",
            "-p", "8081",
            "-l", "ERROR"
        };

        ServerConfig config = ServerConfig.fromCommandLine(args);

        assertEquals(100, config.getVirtualThreadCount());
        assertEquals(8081, config.getServerPort());
        assertEquals("ERROR", config.getLogLevel());
    }

    @Test
    @DisplayName("Should create MavenConfig from ServerConfig")
    void testMavenConfigCreation() {
        ServerConfig config = new ServerConfig();
        config.setMavenExecutable("/usr/bin/mvn");
        config.setMavenSettings("/home/user/.m2/settings.xml");
        config.setMavenLocalRepository("/home/user/.m2/repository");

        var mavenConfig = config.getMavenConfig();

        assertEquals("/usr/bin/mvn", mavenConfig.getExecutable().toString().replace("\\", "/"));
        assertEquals("/home/user/.m2/settings.xml", mavenConfig.getSettingsFile().toString().replace("\\", "/"));
        assertEquals("/home/user/.m2/repository", mavenConfig.getLocalRepository().toString().replace("\\", "/"));
    }

    @Test
    @DisplayName("Should set decompiler config")
    void testDecompilerConfig() {
        ServerConfig config = new ServerConfig();
        config.getDecompilerConfig().setDecompilerType(io.github.bhxch.mcp.jlens.config.DecompilerConfig.DecompilerType.CFR);

        assertEquals(io.github.bhxch.mcp.jlens.config.DecompilerConfig.DecompilerType.CFR,
                     config.getDecompilerConfig().getDecompilerType());
    }
}



