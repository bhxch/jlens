package io.github.bhxch.mcp.jlens.maven.resolver;

import io.github.bhxch.mcp.jlens.config.MavenConfig;
import io.github.bhxch.mcp.jlens.maven.model.DependencyInfo;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import io.github.bhxch.mcp.jlens.maven.model.Scope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maven resolver using Maven Invoker to execute Maven commands
 */
public class MavenInvokerResolver implements MavenResolver {

    private final MavenConfig config;
    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("\\[INFO\\]\\s+(.*):(.*):(.*):(.*):\\s*(.*)");

    public MavenInvokerResolver(MavenConfig config) {
        this.config = config;
    }

    @Override
    public ModuleContext resolveModule(Path pomFile, Scope scope, List<String> activeProfiles) {
        if (!Files.exists(pomFile)) {
            throw new IllegalArgumentException("POM file does not exist: " + pomFile);
        }

        try {
            List<String> command = buildMavenCommand(pomFile, scope, activeProfiles);

            Process process = startProcess(pomFile, command);

            List<String> outputLines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputLines.add(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Maven command failed with exit code: " + exitCode);
            }

            return parseModuleContext(pomFile, outputLines, scope);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to resolve Maven module: " + pomFile, e);
        }
    }

    /**
     * Start the Maven process. Extracted for testing.
     */
    protected Process startProcess(Path pomFile, List<String> command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(pomFile.getParent().toFile());
        pb.redirectErrorStream(true);
        return pb.start();
    }

    private List<String> buildMavenCommand(Path pomFile, Scope scope, List<String> activeProfiles) {
        List<String> command = new ArrayList<>();

        if (config.getExecutable() != null) {
            command.add(config.getExecutable().toString());
        } else {
            command.add("mvn");
        }

        command.add("dependency:list");
        command.add("-DincludeScope=" + scope.name().toLowerCase());
        
        // Pass active profiles
        if (activeProfiles != null && !activeProfiles.isEmpty()) {
            command.add("-P" + String.join(",", activeProfiles));
        }

        if (config.getSettingsFile() != null) {
            command.add("-s");
            command.add(config.getSettingsFile().toString());
        }

        if (config.isOfflineMode()) {
            command.add("-o");
        }

        if (config.isFailFast()) {
            command.add("-ff");
        }

        return command;
    }

    private ModuleContext parseModuleContext(Path pomFile, List<String> outputLines, Scope scope) {
        List<DependencyInfo> dependencies = new ArrayList<>();

        for (String line : outputLines) {
            Matcher matcher = DEPENDENCY_PATTERN.matcher(line);
            if (matcher.find()) {
                String groupId = matcher.group(1);
                String artifactId = matcher.group(2);
                String version = matcher.group(3);
                String type = matcher.group(4);
                String scopeStr = matcher.group(5);

                DependencyInfo dep = DependencyInfo.builder()
                    .groupId(groupId)
                    .artifactId(artifactId)
                    .version(version)
                    .type(type)
                    .scope(scopeStr != null ? Scope.fromString(scopeStr) : scope)
                    .build();

                dependencies.add(dep);
            }
        }

        Path baseDirectory = pomFile.getParent();
        Path outputDirectory = baseDirectory.resolve("target/classes");
        Path testOutputDirectory = baseDirectory.resolve("target/test-classes");

        return ModuleContext.builder()
            .pomFile(pomFile)
            .baseDirectory(baseDirectory)
            .groupId("unknown")
            .artifactId("unknown")
            .version("unknown")
            .dependencies(dependencies)
            .outputDirectory(outputDirectory)
            .testOutputDirectory(testOutputDirectory)
            .build();
    }

    @Override
    public boolean isAvailable() {
        if (config.getExecutable() != null) {
            return Files.exists(config.getExecutable());
        }

        try {
            Process process = new ProcessBuilder("mvn", "-version").start();
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return "InvokerResolver";
    }
}




