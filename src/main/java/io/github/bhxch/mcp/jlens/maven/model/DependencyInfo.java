package io.github.bhxch.mcp.jlens.maven.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Information about a Maven dependency
 */
public class DependencyInfo {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String classifier;
    private final String type;
    private final Scope scope;
    private final Path jarPath;
    private final boolean optional;
    private final String systemPath;

    public DependencyInfo(String groupId, String artifactId, String version,
                         String classifier, String type, Scope scope,
                         Path jarPath, boolean optional, String systemPath) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.type = type != null ? type : "jar";
        this.scope = scope != null ? scope : Scope.COMPILE;
        this.jarPath = jarPath;
        this.optional = optional;
        this.systemPath = systemPath;
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

    public String getClassifier() {
        return classifier;
    }

    public String getType() {
        return type;
    }

    public Scope getScope() {
        return scope;
    }

    public Path getJarPath() {
        return jarPath;
    }

    public boolean isOptional() {
        return optional;
    }

    public String getSystemPath() {
        return systemPath;
    }

    public String getCoordinates() {
        StringBuilder sb = new StringBuilder();
        sb.append(groupId).append(":").append(artifactId);
        if (classifier != null && !classifier.isEmpty()) {
            sb.append(":").append(classifier);
        }
        sb.append(":").append(type).append(":").append(version);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DependencyInfo that = (DependencyInfo) o;
        return Objects.equals(groupId, that.groupId) &&
               Objects.equals(artifactId, that.artifactId) &&
               Objects.equals(version, that.version) &&
               Objects.equals(classifier, that.classifier) &&
               Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version, classifier, type);
    }

    @Override
    public String toString() {
        return getCoordinates();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String groupId;
        private String artifactId;
        private String version;
        private String classifier;
        private String type = "jar";
        private Scope scope = Scope.COMPILE;
        private Path jarPath;
        private boolean optional = false;
        private String systemPath;

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

        public Builder classifier(String classifier) {
            this.classifier = classifier;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder scope(Scope scope) {
            this.scope = scope;
            return this;
        }

        public Builder jarPath(Path jarPath) {
            this.jarPath = jarPath;
            return this;
        }

        public Builder optional(boolean optional) {
            this.optional = optional;
            return this;
        }

        public Builder systemPath(String systemPath) {
            this.systemPath = systemPath;
            return this;
        }

        public DependencyInfo build() {
            return new DependencyInfo(groupId, artifactId, version, classifier,
                                     type, scope, jarPath, optional, systemPath);
        }
    }
}



