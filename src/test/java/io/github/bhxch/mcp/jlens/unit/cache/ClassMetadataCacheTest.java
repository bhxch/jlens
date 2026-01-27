package io.github.bhxch.mcp.jlens.unit.cache;

import io.github.bhxch.mcp.jlens.cache.ClassMetadataCache;
import io.github.bhxch.mcp.jlens.config.ServerConfig;
import io.github.bhxch.mcp.jlens.inspector.model.ClassMetadata;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ClassMetadataCache Unit Tests")
class ClassMetadataCacheTest {

    private ClassMetadataCache cache;

    @BeforeEach
    void setUp() {
        cache = new ClassMetadataCache(100, 3600);
    }

    @Test
    @DisplayName("Should cache and retrieve metadata by class name")
    void testCacheAndRetrieve() {
        ClassMetadata metadata = ClassMetadata.builder()
            .className("com.example.Test")
            .build();

        cache.put("com.example.Test", "default", metadata);
        ClassMetadata retrieved = cache.get("com.example.Test", "default");

        assertNotNull(retrieved);
        assertEquals("com.example.Test", retrieved.getClassName());
    }

    @Test
    @DisplayName("Should cache and retrieve metadata by GAV")
    void testCacheAndRetrieve_GAV() {
        ClassMetadata metadata = ClassMetadata.builder()
            .className("com.example.Test")
            .build();

        cache.putWithGav("com.example.Test", "com.example", "test", "1.0.0", metadata);
        ClassMetadata retrieved = cache.getByGav("com.example.Test", "com.example", "test", "1.0.0");

        assertNotNull(retrieved);
        assertEquals("com.example.Test", retrieved.getClassName());
    }

    @Test
    @DisplayName("Should separate cache by version")
    void testCacheIsolation() {
        ClassMetadata m1 = ClassMetadata.builder().className("Test").build();
        ClassMetadata m2 = ClassMetadata.builder().className("Test").build();

        cache.putWithGav("Test", "group", "art", "1.0.0", m1);
        cache.putWithGav("Test", "group", "art", "2.0.0", m2);

        assertNotNull(cache.getByGav("Test", "group", "art", "1.0.0"));
        assertNotNull(cache.getByGav("Test", "group", "art", "2.0.0"));
    }

    @Test
    @DisplayName("Should clear cache")
    void testClear() {
        cache.put("Test", "cp", ClassMetadata.builder().className("Test").build());
        cache.invalidateAll();
        assertNull(cache.get("Test", "cp"));
    }
}
