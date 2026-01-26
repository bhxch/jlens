package io.github.bhxch.mcp.jlens.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bhxch.mcp.jlens.inspector.model.ClassMetadata;

import java.util.concurrent.TimeUnit;

/**
 * Cache for class metadata
 */
public class ClassMetadataCache {

    private final Cache<String, ClassMetadata> cache;
    private final long ttlSeconds;

    public ClassMetadataCache(long maxSize, long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
        this.cache = Caffeine.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
            .build();
    }

    public ClassMetadata get(String className, String classpathKey) {
        String key = buildKey(className, classpathKey);
        return cache.getIfPresent(key);
    }

    public void put(String className, String classpathKey, ClassMetadata metadata) {
        String key = buildKey(className, classpathKey);
        cache.put(key, metadata);
    }

    public void invalidate(String className, String classpathKey) {
        String key = buildKey(className, classpathKey);
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

    private String buildKey(String className, String classpathKey) {
        return classpathKey + ":" + className;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }
}



