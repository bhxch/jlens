package io.github.bhxch.mcp.jlens.maven.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Represents a Maven project
 */
public class MavenProject {

    private final Path pomFile;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String packaging;
    private final String name;
    private final String description;
    private final List<DependencyInfo> dependencies;
    private final Map<String, String> properties;
    private final List<MavenProject> modules;
    private final Path baseDirectory;

    public MavenProject(Path pomFile, String groupId, String artifactId, String version,
                       String packaging, String name, String description,
                       List<DependencyInfo> dependencies, Map<String, String> properties,
                       List<MavenProject> modules, Path baseDirectory) {
        this.pomFile = pomFile;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.packaging = packaging;
        this.name = name;
        this.description = description;
        this.dependencies = dependencies;
        this.properties = properties;
        this.modules = modules;
        this.baseDirectory = baseDirectory;
    }

    public Path getPomFile() {
        return pomFile;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getPackaging() {
        return packaging;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<DependencyInfo> getDependencies() {
        return dependencies;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public List<MavenProject> getModules() {
        return modules;
    }

    public Path getBaseDirectory() {
        return baseDirectory;
    }

    public String getCoordinates() {
        return groupId + ":" + artifactId + ":" + version;
    }

    public boolean isMultiModule() {
        return !modules.isEmpty();
    }
}



