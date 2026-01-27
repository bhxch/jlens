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

    public ClassMetadata getByGav(String className, String groupId, String artifactId, String version) {
        String key = buildGavKey(className, groupId, artifactId, version);
        return cache.getIfPresent(key);
    }

    public void put(String className, String classpathKey, ClassMetadata metadata) {
        String key = buildKey(className, classpathKey);
        cache.put(key, metadata);
    }

    public void putWithGav(String className, String groupId, String artifactId, String version, ClassMetadata metadata) {
        String key = buildGavKey(className, groupId, artifactId, version);
        cache.put(key, metadata);
    }

    public void invalidate(String className, String classpathKey) {
        String key = buildKey(className, classpathKey);
        cache.invalidate(key);
    }

    public void invalidateGav(String className, String groupId, String artifactId, String version) {
        String key = buildGavKey(className, groupId, artifactId, version);
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
        return "cp:" + classpathKey + ":" + className;
    }

    private String buildGavKey(String className, String groupId, String artifactId, String version) {
        return "gav:" + groupId + ":" + artifactId + ":" + version + ":" + className;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }
}



