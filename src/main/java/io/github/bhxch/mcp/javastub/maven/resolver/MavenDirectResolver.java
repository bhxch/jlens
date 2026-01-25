package io.github.bhxch.mcp.javastub.maven.resolver;

import io.github.bhxch.mcp.javastub.maven.model.DependencyInfo;
import io.github.bhxch.mcp.javastub.maven.model.ModuleContext;
import io.github.bhxch.mcp.javastub.maven.model.Scope;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple POM parser that directly reads pom.xml without invoking Maven
 * This is a fallback resolver when Maven is not available
 */
public class MavenDirectResolver implements MavenResolver {

    private static final Pattern GROUP_ID_PATTERN = Pattern.compile("<groupId>([^<]+)</groupId>");
    private static final Pattern ARTIFACT_ID_PATTERN = Pattern.compile("<artifactId>([^<]+)</artifactId>");
    private static final Pattern VERSION_PATTERN = Pattern.compile("<version>([^<]+)</version>");
    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("<dependency>\\s*<groupId>([^<]+)</groupId>\\s*<artifactId>([^<]+)</artifactId>\\s*<version>([^<]+)</version>.*?</dependency>", Pattern.DOTALL);
    private static final Pattern SCOPE_PATTERN = Pattern.compile("<scope>([^<]+)</scope>");

    @Override
    public ModuleContext resolveModule(Path pomFile, Scope scope, List<String> excludes) {
        if (!Files.exists(pomFile)) {
            throw new IllegalArgumentException("POM file does not exist: " + pomFile);
        }

        try {
            String pomContent = Files.readString(pomFile);

            String groupId = extractValue(pomContent, GROUP_ID_PATTERN);
            String artifactId = extractValue(pomContent, ARTIFACT_ID_PATTERN);
            String version = extractValue(pomContent, VERSION_PATTERN);

            if (groupId == null || artifactId == null) {
                throw new IllegalArgumentException("Invalid POM file: missing groupId or artifactId");
            }

            if (version == null) {
                version = "unknown";
            }

            List<DependencyInfo> dependencies = extractDependencies(pomContent);

            Path baseDirectory = pomFile.getParent();
            if (baseDirectory == null) {
                baseDirectory = pomFile.toAbsolutePath().getParent();
            }

            Path outputDirectory = baseDirectory.resolve("target/classes");
            Path testOutputDirectory = baseDirectory.resolve("target/test-classes");

            List<Path> classpath = new ArrayList<>();
            classpath.add(outputDirectory);

            return ModuleContext.builder()
                .pomFile(pomFile)
                .baseDirectory(baseDirectory)
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version)
                .dependencies(dependencies)
                .classpath(classpath)
                .outputDirectory(outputDirectory)
                .testOutputDirectory(testOutputDirectory)
                .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to read POM file: " + pomFile, e);
        }
    }

    private String extractValue(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private List<DependencyInfo> extractDependencies(String pomContent) {
        List<DependencyInfo> dependencies = new ArrayList<>();
        Matcher dependencyMatcher = DEPENDENCY_PATTERN.matcher(pomContent);

        while (dependencyMatcher.find()) {
            String groupId = dependencyMatcher.group(1).trim();
            String artifactId = dependencyMatcher.group(2).trim();
            String version = dependencyMatcher.group(3).trim();

            String dependencyBlock = dependencyMatcher.group(0);
            String scopeStr = extractValue(dependencyBlock, SCOPE_PATTERN);
            Scope scope = scopeStr != null ? Scope.fromString(scopeStr) : Scope.COMPILE;

            DependencyInfo dep = DependencyInfo.builder()
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version)
                .scope(scope)
                .build();

            dependencies.add(dep);
        }

        return dependencies;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getName() {
        return "DirectResolver";
    }
}
