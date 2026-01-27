package io.github.bhxch.mcp.jlens.unit.cache;

import io.github.bhxch.mcp.jlens.cache.ModuleCache;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModuleCache Unit Tests")
class ModuleCacheTest {

    private ModuleCache cache;

    @BeforeEach
    void setUp() {
        cache = new ModuleCache(100, 3600);
    }

    @Test
    @DisplayName("Should put and get module context")
    void testPutAndGet() {
        Path path = Path.of("pom.xml");
        ModuleContext context = ModuleContext.builder()
                .pomFile(path)
                .build();

        cache.put(path, context);
        assertEquals(context, cache.get(path));
    }

    @Test
    @DisplayName("Should return null for missing module")
    void testGetMissing() {
        assertNull(cache.get(Path.of("non-existent.xml")));
    }

    @Test
    @DisplayName("Should invalidate cache")
    void testInvalidate() {
        Path path = Path.of("pom.xml");
        cache.put(path, ModuleContext.builder().pomFile(path).build());
        cache.invalidate(path);
        assertNull(cache.get(path));
    }

    @Test
    @DisplayName("Should invalidate all")
    void testInvalidateAll() {
        Path path = Path.of("pom.xml");
        cache.put(path, ModuleContext.builder().pomFile(path).build());
        cache.invalidateAll();
        assertEquals(0, cache.size());
    }
}