package io.github.bhxch.mcp.jlens.maven.resolver;

import io.github.bhxch.mcp.jlens.maven.model.DependencyInfo;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import io.github.bhxch.mcp.jlens.maven.model.Scope;

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
    public ModuleContext resolveModule(Path pomFile, Scope scope, List<String> activeProfiles) {
        if (!Files.exists(pomFile)) {
            throw new IllegalArgumentException("POM file does not exist: " + pomFile);
        }

        try {
            String pomContent = Files.readString(pomFile);
            
            // Extract properties for interpolation
            java.util.Map<String, String> properties = extractProperties(pomContent);

            String groupId = extractValue(pomContent, GROUP_ID_PATTERN);
            String artifactId = extractValue(pomContent, ARTIFACT_ID_PATTERN);
            String version = extractValue(pomContent, VERSION_PATTERN);

            if (groupId == null || artifactId == null) {
                throw new IllegalArgumentException("Invalid POM file: missing groupId or artifactId");
            }

            if (version == null) {
                version = "unknown";
            }
            
            // Basic interpolation
            properties.put("project.groupId", groupId);
            properties.put("project.artifactId", artifactId);
            properties.put("project.version", version);
            properties.put("pom.groupId", groupId);
            properties.put("pom.artifactId", artifactId);
            properties.put("pom.version", version);

            groupId = interpolate(groupId, properties);
            artifactId = interpolate(artifactId, properties);
            version = interpolate(version, properties);

            List<DependencyInfo> dependencies = extractDependencies(pomContent, properties);

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

    private java.util.Map<String, String> extractProperties(String pomContent) {
        java.util.Map<String, String> properties = new java.util.HashMap<>();
        Pattern propsPattern = Pattern.compile("<properties>(.*?)</properties>", Pattern.DOTALL);
        Matcher propsMatcher = propsPattern.matcher(pomContent);
        if (propsMatcher.find()) {
            String propsBlock = propsMatcher.group(1);
            Pattern propPattern = Pattern.compile("<([^>]+)>([^<]+)</\\1>");
            Matcher propMatcher = propPattern.matcher(propsBlock);
            while (propMatcher.find()) {
                properties.put(propMatcher.group(1), propMatcher.group(2).trim());
            }
        }
        return properties;
    }

    private String interpolate(String value, java.util.Map<String, String> properties) {
        if (value == null) return null;
        String result = value;
        for (java.util.Map.Entry<String, String> entry : properties.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    private List<DependencyInfo> extractDependencies(String pomContent, java.util.Map<String, String> properties) {
        List<DependencyInfo> dependencies = new ArrayList<>();
        Matcher dependencyMatcher = DEPENDENCY_PATTERN.matcher(pomContent);

        while (dependencyMatcher.find()) {
            String groupId = interpolate(dependencyMatcher.group(1).trim(), properties);
            String artifactId = interpolate(dependencyMatcher.group(2).trim(), properties);
            String version = interpolate(dependencyMatcher.group(3).trim(), properties);

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

    private String extractValue(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
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




