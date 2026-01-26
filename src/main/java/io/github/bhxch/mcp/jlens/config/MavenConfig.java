package io.github.bhxch.mcp.jlens.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Maven configuration
 */
public class MavenConfig {

    private Path executable;
    private Path settingsFile;
    private Path localRepository;
    private boolean offlineMode = false;
    private boolean updateSnapshots = false;
    private boolean failFast = true;
    private int timeoutSeconds = 300;
    private int maxRetries = 3;

    public MavenConfig() {
    }

    public Path getExecutable() {
        return executable;
    }

    public void setExecutable(Path executable) {
        this.executable = executable;
    }

    public void setExecutable(String executable) {
        this.executable = Paths.get(executable);
    }

    public Path getSettingsFile() {
        return settingsFile;
    }

    public void setSettingsFile(Path settingsFile) {
        this.settingsFile = settingsFile;
    }

    public void setSettingsFile(String settingsFile) {
        this.settingsFile = Paths.get(settingsFile);
    }

    public Path getLocalRepository() {
        return localRepository;
    }

    public void setLocalRepository(Path localRepository) {
        this.localRepository = localRepository;
    }

    public void setLocalRepository(String localRepository) {
        this.localRepository = Paths.get(localRepository);
    }

    public boolean isOfflineMode() {
        return offlineMode;
    }

    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
    }

    public boolean isUpdateSnapshots() {
        return updateSnapshots;
    }

    public void setUpdateSnapshots(boolean updateSnapshots) {
        this.updateSnapshots = updateSnapshots;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
}



