package io.github.bhxch.mcp.javastub.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bhxch.mcp.javastub.maven.model.ModuleContext;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Cache for Maven module contexts
 */
public class ModuleCache {

    private final Cache<String, ModuleContext> cache;
    private final long ttlSeconds;

    public ModuleCache(long maxSize, long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
        this.cache = Caffeine.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
            .build();
    }

    public ModuleContext get(Path pomFile) {
        String key = pomFile.toString();
        return cache.getIfPresent(key);
    }

    public void put(Path pomFile, ModuleContext context) {
        String key = pomFile.toString();
        cache.put(key, context);
    }

    public void invalidate(Path pomFile) {
        String key = pomFile.toString();
        cache.invalidate(key);
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    public long size() {
        return cache.estimatedSize();
    }

    public void cleanup() {
        cache.cleanUp();
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }
}