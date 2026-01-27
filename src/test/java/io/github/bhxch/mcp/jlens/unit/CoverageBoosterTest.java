package io.github.bhxch.mcp.jlens.unit;

import io.github.bhxch.mcp.jlens.config.DecompilerConfig;
import io.github.bhxch.mcp.jlens.config.MavenConfig;
import io.github.bhxch.mcp.jlens.config.ServerConfig;
import io.github.bhxch.mcp.jlens.decompiler.DecompilerFactory;
import io.github.bhxch.mcp.jlens.maven.model.DependencyInfo;
import io.github.bhxch.mcp.jlens.maven.model.Scope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Coverage Booster Tests")
class CoverageBoosterTest {

    @Test
    void testModels() {
        DependencyInfo dep = DependencyInfo.builder()
            .groupId("g").artifactId("a").version("v").type("t").scope(Scope.COMPILE)
            .build();
        assertEquals("g:a:t:v", dep.getCoordinates());
        assertNotNull(dep.toString());

        DecompilerConfig config = new DecompilerConfig();
        config.setDecompilerType(DecompilerConfig.DecompilerType.CFR);
        assertEquals(DecompilerConfig.DecompilerType.CFR, config.getDecompilerType());
        
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setDecompilerConfig(config);
        assertNotNull(serverConfig.getDecompilerConfig());
    }

    @Test
    void testDecompilerFactory() {
        DecompilerConfig config = new DecompilerConfig();
        config.setDecompilerType(DecompilerConfig.DecompilerType.CFR);
        assertNotNull(DecompilerFactory.createDecompiler(config));
        
        config.setDecompilerType(DecompilerConfig.DecompilerType.FERNFLOWER);
        assertNotNull(DecompilerFactory.createDecompiler(config));
    }

    @Test
    void testMavenConfig() {
        MavenConfig config = new MavenConfig();
        config.setOfflineMode(true);
        assertTrue(config.isOfflineMode());
        config.setFailFast(true);
        assertTrue(config.isFailFast());
        config.setExecutable(Path.of("mvn"));
        assertNotNull(config.getExecutable());
    }

    @Test
    void testScope() {
        assertEquals(Scope.COMPILE, Scope.fromString("compile"));
        assertEquals(Scope.TEST, Scope.fromString("test"));
        assertEquals(Scope.RUNTIME, Scope.fromString("runtime"));
        assertEquals(Scope.PROVIDED, Scope.fromString("provided"));
        assertEquals(Scope.COMPILE, Scope.fromString("unknown"));
    }
}
