package io.github.bhxch.mcp.jlens.maven.model;

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
    private final Path projectRoot;
    private final Path localRepository;
    private final Scope scope;
    private final List<String> activeProfiles;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String packaging;
    private final List<DependencyInfo> dependencies;
    private final List<Path> classpath;
    private final List<Path> classpathJars;
    private final List<Path> sourceJars;
    private final Path outputDirectory;
    private final Path testOutputDirectory;

    private ModuleContext(Builder builder) {
        this.pomFile = builder.pomFile;
        this.baseDirectory = builder.baseDirectory;
        this.projectRoot = builder.projectRoot != null ? builder.projectRoot : builder.baseDirectory;
        this.localRepository = builder.localRepository;
        this.scope = builder.scope != null ? builder.scope : Scope.COMPILE;
        this.activeProfiles = builder.activeProfiles != null ? 
            List.copyOf(builder.activeProfiles) : List.of();
        this.groupId = builder.groupId;
        this.artifactId = builder.artifactId;
        this.version = builder.version;
        this.packaging = builder.packaging;
        this.dependencies = Collections.unmodifiableList(new ArrayList<>(builder.dependencies));
        this.classpath = Collections.unmodifiableList(new ArrayList<>(builder.classpath));
        this.classpathJars = Collections.unmodifiableList(new ArrayList<>(builder.classpathJars));
        this.sourceJars = Collections.unmodifiableList(new ArrayList<>(builder.sourceJars));
        this.outputDirectory = builder.outputDirectory;
        this.testOutputDirectory = builder.testOutputDirectory;
    }

    public Path getPomFile() {
        return pomFile;
    }

    public Path getBaseDirectory() {
        return baseDirectory;
    }

    public Path getModuleRoot() {
        return baseDirectory;
    }

    public Path getProjectRoot() {
        return projectRoot;
    }

    public Path getLocalRepository() {
        return localRepository;
    }

    public Scope getScope() {
        return scope;
    }

    public List<String> getActiveProfiles() {
        return activeProfiles;
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

    public List<Path> getClasspathJars() {
        return classpathJars;
    }

    public List<Path> getSourceJars() {
        return sourceJars;
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
        private Path projectRoot;
        private Path localRepository;
        private Scope scope = Scope.COMPILE;
        private List<String> activeProfiles = new ArrayList<>();
        private String groupId;
        private String artifactId;
        private String version;
        private String packaging = "jar";
        private List<DependencyInfo> dependencies = new ArrayList<>();
        private List<Path> classpath = new ArrayList<>();
        private List<Path> classpathJars = new ArrayList<>();
        private List<Path> sourceJars = new ArrayList<>();
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

        public Builder projectRoot(Path projectRoot) {
            this.projectRoot = projectRoot;
            return this;
        }

        public Builder localRepository(Path localRepository) {
            this.localRepository = localRepository;
            return this;
        }

        public Builder scope(Scope scope) {
            this.scope = scope;
            return this;
        }

        public Builder activeProfile(String profile) {
            this.activeProfiles.add(profile);
            return this;
        }

        public Builder activeProfiles(List<String> profiles) {
            this.activeProfiles = new ArrayList<>(profiles);
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

        public Builder addClasspathJar(Path jarPath) {
            this.classpathJars.add(jarPath);
            return this;
        }

        public Builder classpathJars(List<Path> classpathJars) {
            this.classpathJars = new ArrayList<>(classpathJars);
            return this;
        }

        public Builder addSourceJar(Path jarPath) {
            this.sourceJars.add(jarPath);
            return this;
        }

        public Builder sourceJars(List<Path> sourceJars) {
            this.sourceJars = new ArrayList<>(sourceJars);
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




