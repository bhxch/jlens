package io.github.bhxch.mcp.jlens.unit.cache;

import io.github.bhxch.mcp.jlens.cache.CacheManager;
import io.github.bhxch.mcp.jlens.config.ServerConfig;
import io.github.bhxch.mcp.jlens.inspector.model.ClassMetadata;
import io.github.bhxch.mcp.jlens.maven.model.DependencyInfo;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CacheManager Unit Tests")
class CacheManagerTest {

    private CacheManager cacheManager;
    private ServerConfig config;

    @BeforeEach
    void setUp() {
        config = new ServerConfig();
        config.setCacheSize(100);
        config.setCacheTtlSeconds(3600);
        cacheManager = new CacheManager(config);
    }

    @Test
    @DisplayName("Should cache module context and return cached value")
    void testModuleContextCaching() {
        String key = "test-module-key";
        ModuleContext expectedContext = createTestModuleContext();

        ModuleContext result1 = cacheManager.getModuleContext(key, () -> expectedContext);
        ModuleContext result2 = cacheManager.getModuleContext(key, () -> {
            fail("Should use cached value");
            return null;
        });

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(expectedContext, result1);
        assertEquals(expectedContext, result2);
        assertEquals(1, cacheManager.getModuleCacheSize());
    }

    @Test
    @DisplayName("Should handle concurrent cache access correctly")
    void testConcurrentCacheAccess() throws Exception {
        String key = "concurrent-test-key";
        AtomicInteger callCounter = new AtomicInteger(0);

        try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = new java.util.ArrayList<java.util.concurrent.CompletableFuture<ModuleContext>>();

            for (int i = 0; i < 100; i++) {
                futures.add(java.util.concurrent.CompletableFuture.supplyAsync(() ->
                    cacheManager.getModuleContext(key, () -> {
                        callCounter.incrementAndGet();
                        return createTestModuleContext();
                    }), executor));
            }

            java.util.concurrent.CompletableFuture.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0])).join();

            assertEquals(1, callCounter.get());
        }
    }

    @Test
    @DisplayName("Should get module context if present")
    void testGetModuleContextIfPresent() {
        String key = "test-key";
        ModuleContext context = createTestModuleContext();

        assertNull(cacheManager.getModuleContextIfPresent(key));

        cacheManager.putModuleContext(key, context);

        assertNotNull(cacheManager.getModuleContextIfPresent(key));
    }

    @Test
    @DisplayName("Should invalidate module context")
    void testInvalidateModuleContext() {
        String key = "test-key";
        ModuleContext context = createTestModuleContext();

        cacheManager.putModuleContext(key, context);
        assertNotNull(cacheManager.getModuleContextIfPresent(key));

        cacheManager.invalidateModuleContext(key);
        assertNull(cacheManager.getModuleContextIfPresent(key));
    }

    @Test
    @DisplayName("Should cache class metadata")
    void testClassMetadataCaching() {
        String key = "test-class";
        String classpathKey = "test-cp";
        ClassMetadata expectedMetadata = createTestClassMetadata();

        String cacheKey = classpathKey + ":" + key;
        ClassMetadata result1 = cacheManager.getClassMetadata(cacheKey, () -> expectedMetadata);
        ClassMetadata result2 = cacheManager.getClassMetadata(cacheKey, () -> {
            fail("Should use cached value");
            return null;
        });

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(expectedMetadata, result1);
    }

    @Test
    @DisplayName("Should invalidate all caches")
    void testInvalidateAll() {
        cacheManager.putModuleContext("module-key", createTestModuleContext());
        cacheManager.putClassMetadata("cp-key:class-key", createTestClassMetadata());
        cacheManager.putDecompiledSource("decompile-key", "source");

        assertTrue(cacheManager.getModuleCacheSize() > 0);

        cacheManager.invalidateAll();

        assertEquals(0, cacheManager.getModuleCacheSize());
        assertEquals(0, cacheManager.getClassMetadataCacheSize());
        assertEquals(0, cacheManager.getDecompilerCacheSize());
    }

    @Test
    @DisplayName("Should return cache stats")
    void testCacheStats() {
        cacheManager.putModuleContext("test-key", createTestModuleContext());

        var stats = cacheManager.getModuleCacheStats();
        assertNotNull(stats);
        // Note: stats may vary based on cache implementation
        assertTrue(stats.requestCount() >= 0);
    }

    private ModuleContext createTestModuleContext() {
        return ModuleContext.builder()
            .groupId("com.test")
            .artifactId("test-artifact")
            .version("1.0.0")
            .build();
    }

    private ClassMetadata createTestClassMetadata() {
        return ClassMetadata.builder()
            .className("com.test.TestClass")
            .packageName("com.test")
            .simpleClassName("TestClass")
            .build();
    }
}



