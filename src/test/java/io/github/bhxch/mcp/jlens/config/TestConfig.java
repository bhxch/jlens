package io.github.bhxch.mcp.jlens.config;

import io.github.bhxch.mcp.jlens.cache.CacheManager;
import io.github.bhxch.mcp.jlens.decompiler.DecompilerFactory;
import io.github.bhxch.mcp.jlens.inspector.ClassInspector;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolverFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test configuration to ensure config classes work correctly
 */
@DisplayName("Config Integration Tests")
class TestConfig {

    private ServerConfig serverConfig;
    private ClassInspector inspector;
    private MavenResolverFactory resolverFactory;
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        serverConfig = new ServerConfig();

        DecompilerConfig decompilerConfig = new DecompilerConfig();
        decompilerConfig.setDecompilerType(DecompilerConfig.DecompilerType.VINEFLOWER);
        serverConfig.setDecompilerConfig(decompilerConfig);

        resolverFactory = new MavenResolverFactory(serverConfig);
        cacheManager = new CacheManager(serverConfig);

        inspector = new ClassInspector(
            DecompilerFactory.createDecompiler(serverConfig.getDecompilerConfig())
        );
    }

    @Test
    @DisplayName("Should create valid server config")
    void testServerConfigCreation() {
        assertNotNull(serverConfig);
        assertNotNull(serverConfig.getDecompilerConfig());
        assertEquals(DecompilerConfig.DecompilerType.VINEFLOWER,
                     serverConfig.getDecompilerConfig().getDecompilerType());
    }

    @Test
    @DisplayName("Should create valid class inspector")
    void testClassInspectorCreation() {
        assertNotNull(inspector);
    }

    @Test
    @DisplayName("Should create valid resolver factory")
    void testResolverFactoryCreation() {
        assertNotNull(resolverFactory);
    }

    @Test
    @DisplayName("Should create valid cache manager")
    void testCacheManagerCreation() {
        assertNotNull(cacheManager);
    }

    @Test
    @DisplayName("Should use absolute path for pom.xml")
    void testAbsolutePathUsage() {
        Path relativePom = Paths.get("pom.xml");
        Path absolutePom = relativePom.toAbsolutePath();

        assertTrue(absolutePom.isAbsolute(), "Path should be absolute");
        assertTrue(absolutePom.toString().endsWith("pom.xml"),
                   "Absolute path should end with pom.xml");
    }

    @Test
    @DisplayName("Should handle different decompiler types")
    void testDecompilerTypes() {
        // Test VINEFLOWER
        serverConfig.getDecompilerConfig().setDecompilerType(DecompilerConfig.DecompilerType.VINEFLOWER);
        assertEquals(DecompilerConfig.DecompilerType.VINEFLOWER,
                     serverConfig.getDecompilerConfig().getDecompilerType());

        // Test CFR
        serverConfig.getDecompilerConfig().setDecompilerType(DecompilerConfig.DecompilerType.CFR);
        assertEquals(DecompilerConfig.DecompilerType.CFR,
                     serverConfig.getDecompilerConfig().getDecompilerType());

        // Test FERNFLOWER
        serverConfig.getDecompilerConfig().setDecompilerType(DecompilerConfig.DecompilerType.FERNFLOWER);
        assertEquals(DecompilerConfig.DecompilerType.FERNFLOWER,
                     serverConfig.getDecompilerConfig().getDecompilerType());
    }

    @Test
    @DisplayName("Should configure cache settings correctly")
    void testCacheSettings() {
        serverConfig.setCacheSize(2000);
        serverConfig.setCacheTtlSeconds(7200);

        assertEquals(2000, serverConfig.getCacheSize());
        assertEquals(7200, serverConfig.getCacheTtlSeconds());
    }

    @Test
    @DisplayName("Should configure Maven settings correctly")
    void testMavenSettings() {
        serverConfig.setMavenExecutable("/usr/bin/mvn");
        serverConfig.setMavenSettings("/home/user/.m2/settings.xml");
        serverConfig.setMavenLocalRepository("/home/user/.m2/repository");

        assertNotNull(serverConfig.getMavenConfig());
        var mavenConfig = serverConfig.getMavenConfig();
        assertNotNull(mavenConfig);
        assertTrue(mavenConfig.getExecutable().toString().contains("mvn"));
    }
}
