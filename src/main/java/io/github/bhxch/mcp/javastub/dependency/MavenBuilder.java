package io.github.bhxch.mcp.javastub.dependency;

import io.github.bhxch.mcp.javastub.maven.model.ModuleContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Builds Maven modules and manages build processes
 */
public class MavenBuilder {
    
    private final String mavenExecutable;
    
    public MavenBuilder() {
        this.mavenExecutable = findMavenExecutable();
    }
    
    public MavenBuilder(String mavenExecutable) {
        this.mavenExecutable = mavenExecutable;
    }
    
    /**
     * Find Maven executable in system
     */
    private String findMavenExecutable() {
        // Check M2_HOME
        String m2Home = System.getenv("M2_HOME");
        if (m2Home != null) {
            Path mvnPath = Path.of(m2Home, "bin", "mvn.cmd");
            if (Files.exists(mvnPath)) {
                return mvnPath.toString();
            }
        }
        
        // Check PATH
        String path = System.getenv("PATH");
        if (path != null) {
            for (String dir : path.split(File.pathSeparator)) {
                Path mvnPath = Path.of(dir, "mvn.cmd");
                if (Files.exists(mvnPath)) {
                    return mvnPath.toString();
                }
            }
        }
        
        // Default to mvn
        return "mvn.cmd";
    }
    
    /**
     * Build a Maven module
     */
    public BuildResult buildModule(ModuleContext context, List<String> goals, 
                                   List<String> additionalArgs, int timeoutSeconds) {
        BuildResult result = new BuildResult();
        long startTime = System.currentTimeMillis();
        
        try {
            List<String> command = new ArrayList<>();
            command.add(mavenExecutable);
            
            // Add goals
            command.addAll(goals);
            
            // Add additional arguments
            command.addAll(additionalArgs);
            
            // Set working directory
            File workingDir = context.getBaseDirectory() != null ? 
                context.getBaseDirectory().toFile() : 
                context.getPomFile().getParent().toFile();
            
            // Build process
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(workingDir);
            processBuilder.redirectErrorStream(true);
            
            // Start process
            Process process = processBuilder.start();
            
            // Read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // Wait for completion
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                result.setSuccess(false);
                result.setExitCode(-1);
                result.setOutput(output.toString());
                result.setDurationSeconds((System.currentTimeMillis() - startTime) / 1000.0);
                result.setError("Build timed out after " + timeoutSeconds + " seconds");
                return result;
            }
            
            int exitCode = process.exitValue();
            result.setExitCode(exitCode);
            result.setOutput(output.toString());
            result.setDurationSeconds((System.currentTimeMillis() - startTime) / 1000.0);
            result.setSuccess(exitCode == 0);
            
            if (exitCode != 0) {
                result.setError("Build failed with exit code: " + exitCode);
            }
            
            // Extract downloaded artifacts
            result.setDownloadedArtifacts(extractDownloadedArtifacts(output.toString()));
            
            // Extract missing dependencies
            result.setMissingDependencies(extractMissingDependencies(output.toString()));
            
        } catch (IOException | InterruptedException e) {
            result.setSuccess(false);
            result.setExitCode(-1);
            result.setError("Build failed: " + e.getMessage());
            result.setDurationSeconds((System.currentTimeMillis() - startTime) / 1000.0);
        }
        
        return result;
    }
    
    /**
     * Extract downloaded artifacts from build output
     */
    private List<ArtifactInfo> extractDownloadedArtifacts(String output) {
        List<ArtifactInfo> artifacts = new ArrayList<>();
        
        for (String line : output.split("\n")) {
            if (line.contains("Downloading") || line.contains("Downloaded")) {
                try {
                    // Parse Maven download output
                    String[] parts = line.split("\\s+");
                    for (String part : parts) {
                        if (part.startsWith("http://") || part.startsWith("https://")) {
                            // Extract artifact coordinates from URL
                            String url = part.trim();
                            if (url.endsWith(".jar") || url.endsWith(".pom")) {
                                ArtifactInfo info = new ArtifactInfo();
                                info.setCoordinates(extractCoordinatesFromUrl(url));
                                info.setType(url.endsWith(".jar") ? "jar" : "pom");
                                artifacts.add(info);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Skip malformed lines
                }
            }
        }
        
        return artifacts;
    }
    
    /**
     * Extract missing dependencies from build output
     */
    private List<io.github.bhxch.mcp.javastub.maven.model.DependencyInfo> extractMissingDependencies(
            String output) {
        List<io.github.bhxch.mcp.javastub.maven.model.DependencyInfo> missingDeps = new ArrayList<>();
        
        if (output.contains("Could not resolve dependencies")) {
            // Parse missing dependencies from error message
            for (String line : output.split("\n")) {
                if (line.contains("Missing:") || line.contains("failure:")) {
                    try {
                        String[] parts = line.split(":");
                        if (parts.length >= 3) {
                            io.github.bhxch.mcp.javastub.maven.model.DependencyInfo dep = 
                                io.github.bhxch.mcp.javastub.maven.model.DependencyInfo.builder()
                                    .groupId(parts[0].trim())
                                    .artifactId(parts[1].trim())
                                    .version(parts[2].trim())
                                    .type("jar")
                                    .build();
                            missingDeps.add(dep);
                        }
                    } catch (Exception e) {
                        // Skip malformed lines
                    }
                }
            }
        }
        
        return missingDeps;
    }
    
    /**
     * Extract artifact coordinates from URL
     */
    private String extractCoordinatesFromUrl(String url) {
        // Maven repository URL pattern:
        // https://repo.maven.apache.org/maven2/groupId/artifactId/version/artifactId-version.jar
        String[] parts = url.split("/");
        if (parts.length >= 6) {
            String groupId = String.join(".", java.util.Arrays.copyOfRange(parts, parts.length - 5, parts.length - 3));
            String artifactId = parts[parts.length - 3];
            String version = parts[parts.length - 2];
            return groupId + ":" + artifactId + ":" + version;
        }
        return url;
    }
    
    /**
     * Check if Maven is available
     */
    public boolean isMavenAvailable() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(mavenExecutable, "--version");
            Process process = processBuilder.start();
            return process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get Maven version
     */
    public String getMavenVersion() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(mavenExecutable, "--version");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            process.waitFor(10, TimeUnit.SECONDS);
            
            // Extract version from output
            for (String line : output.toString().split("\n")) {
                if (line.contains("Apache Maven")) {
                    return line.trim();
                }
            }
            
        } catch (Exception e) {
            return "Unknown";
        }
        
        return "Unknown";
    }
    
    /**
     * Data class for build results
     */
    public static class BuildResult {
        private boolean success;
        private int exitCode;
        private String output;
        private String error;
        private double durationSeconds;
        private List<ArtifactInfo> downloadedArtifacts = new ArrayList<>();
        private List<io.github.bhxch.mcp.javastub.maven.model.DependencyInfo> missingDependencies = new ArrayList<>();
        
        // Getters and setters
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public int getExitCode() {
            return exitCode;
        }
        
        public void setExitCode(int exitCode) {
            this.exitCode = exitCode;
        }
        
        public String getOutput() {
            return output;
        }
        
        public void setOutput(String output) {
            this.output = output;
        }
        
        public String getError() {
            return error;
        }
        
        public void setError(String error) {
            this.error = error;
        }
        
        public double getDurationSeconds() {
            return durationSeconds;
        }
        
        public void setDurationSeconds(double durationSeconds) {
            this.durationSeconds = durationSeconds;
        }
        
        public List<ArtifactInfo> getDownloadedArtifacts() {
            return downloadedArtifacts;
        }
        
        public void setDownloadedArtifacts(List<ArtifactInfo> downloadedArtifacts) {
            this.downloadedArtifacts = downloadedArtifacts;
        }
        
        public List<io.github.bhxch.mcp.javastub.maven.model.DependencyInfo> getMissingDependencies() {
            return missingDependencies;
        }
        
        public void setMissingDependencies(
                List<io.github.bhxch.mcp.javastub.maven.model.DependencyInfo> missingDependencies) {
            this.missingDependencies = missingDependencies;
        }
    }
    
    /**
     * Data class for artifact information
     */
    public static class ArtifactInfo {
        private String coordinates;
        private String type;
        private long sizeBytes;
        private Path file;
        
        // Getters and setters
        public String getCoordinates() {
            return coordinates;
        }
        
        public void setCoordinates(String coordinates) {
            this.coordinates = coordinates;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public long getSizeBytes() {
            return sizeBytes;
        }
        
        public void setSizeBytes(long sizeBytes) {
            this.sizeBytes = sizeBytes;
        }
        
        public Path getFile() {
            return file;
        }
        
        public void setFile(Path file) {
            this.file = file;
        }
    }
}
