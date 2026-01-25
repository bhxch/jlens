package io.github.bhxch.mcp.javastub.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.github.bhxch.mcp.javastub.config.ServerConfig;
import io.github.bhxch.mcp.javastub.inspector.model.ClassMetadata;
import io.github.bhxch.mcp.javastub.maven.model.ModuleContext;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Central cache manager for all caching needs
 */
public class CacheManager {

    private final Cache<String, ModuleContext> moduleCache;
    private final Cache<String, ClassMetadata> classMetadataCache;
    private final Cache<String, String> decompilerCache;
    private final ServerConfig config;

    public CacheManager(ServerConfig config) {
        this.config = config;
        this.moduleCache = createModuleCache();
        this.classMetadataCache = createClassMetadataCache();
        this.decompilerCache = createDecompilerCache();
    }

    private Cache<String, ModuleContext> createModuleCache() {
        return Caffeine.newBuilder()
            .maximumSize(config.getCacheSize())
            .expireAfterWrite(config.getCacheTtlSeconds(), TimeUnit.SECONDS)
            .recordStats()
            .build();
    }

    private Cache<String, ClassMetadata> createClassMetadataCache() {
        return Caffeine.newBuilder()
            .maximumSize(config.getCacheSize() * 10)
            .expireAfterWrite(config.getCacheTtlSeconds(), TimeUnit.SECONDS)
            .recordStats()
            .build();
    }

    private Cache<String, String> createDecompilerCache() {
        return Caffeine.newBuilder()
            .maximumSize(config.getDecompilerConfig().getMaxCacheSize())
            .expireAfterWrite(config.getDecompilerConfig().getCacheTtlSeconds(), TimeUnit.SECONDS)
            .recordStats()
            .build();
    }

    public ModuleContext getModuleContext(String key, Supplier<ModuleContext> loader) {
        return moduleCache.get(key, k -> loader.get());
    }

    public ModuleContext getModuleContextIfPresent(String key) {
        return moduleCache.getIfPresent(key);
    }

    public void putModuleContext(String key, ModuleContext context) {
        moduleCache.put(key, context);
    }

    public void invalidateModuleContext(String key) {
        moduleCache.invalidate(key);
    }

    public ClassMetadata getClassMetadata(String key, Supplier<ClassMetadata> loader) {
        return classMetadataCache.get(key, k -> loader.get());
    }

    public ClassMetadata getClassMetadataIfPresent(String key) {
        return classMetadataCache.getIfPresent(key);
    }

    public void putClassMetadata(String key, ClassMetadata metadata) {
        classMetadataCache.put(key, metadata);
    }

    public void invalidateClassMetadata(String key) {
        classMetadataCache.invalidate(key);
    }

    public String getDecompiledSource(String key, Supplier<String> loader) {
        return decompilerCache.get(key, k -> loader.get());
    }

    public String getDecompiledSourceIfPresent(String key) {
        return decompilerCache.getIfPresent(key);
    }

    public void putDecompiledSource(String key, String source) {
        decompilerCache.put(key, source);
    }

    public void invalidateDecompiledSource(String key) {
        decompilerCache.invalidate(key);
    }

    public void invalidateAll() {
        moduleCache.invalidateAll();
        classMetadataCache.invalidateAll();
        decompilerCache.invalidateAll();
    }

    public CacheStats getModuleCacheStats() {
        return moduleCache.stats();
    }

    public CacheStats getClassMetadataCacheStats() {
        return classMetadataCache.stats();
    }

    public CacheStats getDecompilerCacheStats() {
        return decompilerCache.stats();
    }

    public long getModuleCacheSize() {
        return moduleCache.estimatedSize();
    }

    public long getClassMetadataCacheSize() {
        return classMetadataCache.estimatedSize();
    }

    public long getDecompilerCacheSize() {
        return decompilerCache.estimatedSize();
    }

    public void cleanup() {
        moduleCache.cleanUp();
        classMetadataCache.cleanUp();
        decompilerCache.cleanUp();
    }
}