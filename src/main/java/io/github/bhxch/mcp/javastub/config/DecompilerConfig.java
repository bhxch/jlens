package io.github.bhxch.mcp.javastub.config;

/**
 * Decompiler configuration
 */
public class DecompilerConfig {

    public enum DecompilerType {
        FERNFLOWER,
        CFR,
        VINEFLOWER
    }

    private DecompilerType decompilerType = DecompilerType.FERNFLOWER;
    private boolean enableCaching = true;
    private int maxCacheSize = 1000;
    private long cacheTtlSeconds = 3600;
    private boolean includeLineNumbers = true;
    private boolean includeDebugInfo = false;
    private boolean showSyntheticMembers = false;

    public DecompilerConfig() {
    }

    public DecompilerType getDecompilerType() {
        return decompilerType;
    }

    public void setDecompilerType(DecompilerType decompilerType) {
        this.decompilerType = decompilerType;
    }

    public void setDecompilerType(String type) {
        if (type != null) {
            this.decompilerType = DecompilerType.valueOf(type.toUpperCase());
        }
    }

    public boolean isEnableCaching() {
        return enableCaching;
    }

    public void setEnableCaching(boolean enableCaching) {
        this.enableCaching = enableCaching;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public long getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }

    public void setCacheTtlSeconds(long cacheTtlSeconds) {
        this.cacheTtlSeconds = cacheTtlSeconds;
    }

    public boolean isIncludeLineNumbers() {
        return includeLineNumbers;
    }

    public void setIncludeLineNumbers(boolean includeLineNumbers) {
        this.includeLineNumbers = includeLineNumbers;
    }

    public boolean isIncludeDebugInfo() {
        return includeDebugInfo;
    }

    public void setIncludeDebugInfo(boolean includeDebugInfo) {
        this.includeDebugInfo = includeDebugInfo;
    }

    public boolean isShowSyntheticMembers() {
        return showSyntheticMembers;
    }

    public void setShowSyntheticMembers(boolean showSyntheticMembers) {
        this.showSyntheticMembers = showSyntheticMembers;
    }
}