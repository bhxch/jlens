package io.github.bhxch.mcp.javastub.maven.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Context information for a Maven module
 */
public class ModuleContext {

    private final Path pomFile;
    private final Path baseDirectory;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String packaging;
    private final List<DependencyInfo> dependencies;
    private final List<Path> classpath;
    private final Path outputDirectory;
    private final Path testOutputDirectory;

    private ModuleContext(Builder builder) {
        this.pomFile = builder.pomFile;
        this.baseDirectory = builder.baseDirectory;
        this.groupId = builder.groupId;
        this.artifactId = builder.artifactId;
        this.version = builder.version;
        this.packaging = builder.packaging;
        this.dependencies = Collections.unmodifiableList(new ArrayList<>(builder.dependencies));
        this.classpath = Collections.unmodifiableList(new ArrayList<>(builder.classpath));
        this.outputDirectory = builder.outputDirectory;
        this.testOutputDirectory = builder.testOutputDirectory;
    }

    public Path getPomFile() {
        return pomFile;
    }

    public Path getBaseDirectory() {
        return baseDirectory;
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

    public List<DependencyInfo> getDependencies() {
        return dependencies;
    }

    public List<Path> getClasspath() {
        return classpath;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public Path getTestOutputDirectory() {
        return testOutputDirectory;
    }

    public String getCoordinates() {
        return groupId + ":" + artifactId + ":" + version;
    }

    public List<DependencyInfo> getDependenciesByScope(Scope scope) {
        return dependencies.stream()
            .filter(dep -> dep.getScope() == scope)
            .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleContext that = (ModuleContext) o;
        return Objects.equals(pomFile, that.pomFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pomFile);
    }

    @Override
    public String toString() {
        return "ModuleContext{" +
               "groupId='" + groupId + '\'' +
               ", artifactId='" + artifactId + '\'' +
               ", version='" + version + '\'' +
               ", dependencies=" + dependencies.size() +
               '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Path pomFile;
        private Path baseDirectory;
        private String groupId;
        private String artifactId;
        private String version;
        private String packaging = "jar";
        private List<DependencyInfo> dependencies = new ArrayList<>();
        private List<Path> classpath = new ArrayList<>();
        private Path outputDirectory;
        private Path testOutputDirectory;

        public Builder pomFile(Path pomFile) {
            this.pomFile = pomFile;
            return this;
        }

        public Builder baseDirectory(Path baseDirectory) {
            this.baseDirectory = baseDirectory;
            return this;
        }

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder artifactId(String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder packaging(String packaging) {
            this.packaging = packaging;
            return this;
        }

        public Builder addDependency(DependencyInfo dependency) {
            this.dependencies.add(dependency);
            return this;
        }

        public Builder dependencies(List<DependencyInfo> dependencies) {
            this.dependencies = new ArrayList<>(dependencies);
            return this;
        }

        public Builder addClasspathElement(Path path) {
            this.classpath.add(path);
            return this;
        }

        public Builder classpath(List<Path> classpath) {
            this.classpath = new ArrayList<>(classpath);
            return this;
        }

        public Builder outputDirectory(Path outputDirectory) {
            this.outputDirectory = outputDirectory;
            return this;
        }

        public Builder testOutputDirectory(Path testOutputDirectory) {
            this.testOutputDirectory = testOutputDirectory;
            return this;
        }

        public ModuleContext build() {
            return new ModuleContext(this);
        }
    }
}
