package io.github.bhxch.mcp.javastub.dependency;

import io.github.bhxch.mcp.javastub.maven.model.DependencyInfo;
import io.github.bhxch.mcp.javastub.maven.model.ModuleContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages Maven dependencies, including downloading and resolving missing dependencies
 */
public class DependencyManager {
    
    private final MavenBuilder mavenBuilder;
    
    public DependencyManager(MavenBuilder mavenBuilder) {
        this.mavenBuilder = mavenBuilder;
    }
    
    /**
     * Find missing dependencies for a module
     */
    public List<DependencyInfo> findMissingDependencies(ModuleContext context) {
        List<DependencyInfo> declaredDependencies = context.getDependencies();
        List<DependencyInfo> missingDependencies = new ArrayList<>();
        
        for (DependencyInfo dep : declaredDependencies) {
            if (!isDependencyAvailable(dep, context)) {
                missingDependencies.add(dep);
            }
        }
        
        return missingDependencies;
    }
    
    /**
     * Check if a dependency is available in local repository
     */
    private boolean isDependencyAvailable(DependencyInfo dependency, ModuleContext context) {
        Path localRepo = context.getLocalRepository();
        if (localRepo == null) {
            localRepo = Path.of(System.getProperty("user.home"), ".m2", "repository");
        }
        
        String[] parts = dependency.getGroupId().split("\\.");
        Path depPath = localRepo;
        for (String part : parts) {
            depPath = depPath.resolve(part);
        }
        
        depPath = depPath.resolve(dependency.getArtifactId())
                        .resolve(dependency.getVersion())
                        .resolve(dependency.getArtifactId() + "-" + dependency.getVersion() + ".jar");
        
        return Files.exists(depPath);
    }
    
    /**
     * Check if source JAR is available for a dependency
     */
    public boolean isSourceAvailable(DependencyInfo dependency, ModuleContext context) {
        Path localRepo = context.getLocalRepository();
        if (localRepo == null) {
            localRepo = Path.of(System.getProperty("user.home"), ".m2", "repository");
        }
        
        String[] parts = dependency.getGroupId().split("\\.");
        Path depPath = localRepo;
        for (String part : parts) {
            depPath = depPath.resolve(part);
        }
        
        depPath = depPath.resolve(dependency.getArtifactId())
                        .resolve(dependency.getVersion())
                        .resolve(dependency.getArtifactId() + "-" + dependency.getVersion() + "-sources.jar");
        
        return Files.exists(depPath);
    }
    
    /**
     * Find dependencies that provide a specific class
     */
    public List<DependencyInfo> findDependenciesForClass(String className, ModuleContext context) {
        List<DependencyInfo> allDependencies = context.getDependencies();
        List<DependencyInfo> matchingDependencies = new ArrayList<>();
        
        for (DependencyInfo dep : allDependencies) {
            if (isDependencyAvailable(dep, context)) {
                if (classExistsInDependency(className, dep, context)) {
                    matchingDependencies.add(dep);
                }
            }
        }
        
        return matchingDependencies;
    }
    
    /**
     * Check if a class exists in a dependency
     */
    private boolean classExistsInDependency(String className, DependencyInfo dependency, 
                                           ModuleContext context) {
        Path localRepo = context.getLocalRepository();
        if (localRepo == null) {
            localRepo = Path.of(System.getProperty("user.home"), ".m2", "repository");
        }
        
        String[] parts = dependency.getGroupId().split("\\.");
        Path depPath = localRepo;
        for (String part : parts) {
            depPath = depPath.resolve(part);
        }
        
        depPath = depPath.resolve(dependency.getArtifactId())
                        .resolve(dependency.getVersion())
                        .resolve(dependency.getArtifactId() + "-" + dependency.getVersion() + ".jar");
        
        try (var jarFile = new java.util.jar.JarFile(depPath.toFile())) {
            String classEntry = className.replace('.', '/') + ".class";
            return jarFile.getJarEntry(classEntry) != null;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Download a specific dependency
     */
    public MavenBuilder.BuildResult downloadDependency(DependencyInfo dependency, ModuleContext context, 
                                         boolean downloadSources) {
        List<String> goals = new ArrayList<>();
        goals.add("dependency:get");
        
        List<String> args = new ArrayList<>();
        args.add("-Dartifact=" + dependency.getCoordinates());
        args.add("-Dtransitive=false");
        
        if (downloadSources) {
            goals.add("dependency:get");
            args.add("-Dartifact=" + dependency.getCoordinates() + ":sources");
        }
        
        return mavenBuilder.buildModule(context, goals, args, 300);
    }
    
    /**
     * Download all missing dependencies
     */
    public MavenBuilder.BuildResult downloadAllDependencies(ModuleContext context, boolean downloadSources) {
        List<DependencyInfo> missingDeps = findMissingDependencies(context);
        
        if (missingDeps.isEmpty()) {
            MavenBuilder.BuildResult result = new MavenBuilder.BuildResult();
            result.setSuccess(true);
            result.setOutput("All dependencies are already available");
            return result;
        }
        
        List<String> goals = new ArrayList<>();
        goals.add("compile");
        if (downloadSources) {
            goals.add("dependency:sources");
        }
        
        return mavenBuilder.buildModule(context, goals, List.of(), 600);
    }
    
    /**
     * Get all transitive dependencies
     */
    public List<DependencyInfo> getTransitiveDependencies(ModuleContext context) {
        List<String> goals = List.of("dependency:tree");
        MavenBuilder.BuildResult result = mavenBuilder.buildModule(context, goals, List.of(), 300);
        
        if (!result.isSuccess()) {
            return List.of();
        }
        
        return parseDependencyTree(result.getOutput());
    }
    
    /**
     * Parse dependency tree output
     */
    private List<DependencyInfo> parseDependencyTree(String output) {
        List<DependencyInfo> dependencies = new ArrayList<>();
        
        for (String line : output.split("\n")) {
            if (line.contains(":") && !line.trim().startsWith("[INFO]")) {
                try {
                    String[] parts = line.trim().split(":");
                    if (parts.length >= 3) {
                        DependencyInfo dep = DependencyInfo.builder()
                            .groupId(parts[0].trim())
                            .artifactId(parts[1].trim())
                            .version(parts[2].trim())
                            .type("jar")
                            .build();
                        dependencies.add(dep);
                    }
                } catch (Exception e) {
                    // Skip malformed lines
                }
            }
        }
        
        return dependencies;
    }
}
