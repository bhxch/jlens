package io.github.bhxch.mcp.javastub.unit.intelligence;

import io.github.bhxch.mcp.javastub.intelligence.BuildPromptGenerator;
import io.github.bhxch.mcp.javastub.maven.model.DependencyInfo;
import io.github.bhxch.mcp.javastub.maven.model.ModuleContext;
import io.github.bhxch.mcp.javastub.maven.model.Scope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BuildPromptGenerator
 */
@DisplayName("BuildPromptGenerator Tests")
class BuildPromptGeneratorTest {

    private final BuildPromptGenerator generator = new BuildPromptGenerator();

    @Test
    @DisplayName("Should generate suggestion for missing class without dependencies")
    void testGenerateBuildSuggestion_MissingClassNoDependencies() {
        String className = "com.example.SomeClass";
        ModuleContext context = ModuleContext.builder()
            .baseDirectory(Path.of("/path/to/module"))
            .build();
        List<DependencyInfo> missingDependencies = List.of();

        String suggestion = generator.generateBuildSuggestion(className, context, missingDependencies);

        assertNotNull(suggestion);
        assertTrue(suggestion.contains(className));
        assertTrue(suggestion.contains("mvn"));
        assertTrue(suggestion.contains("compile"));
    }

    @Test
    @DisplayName("Should generate suggestion with specific missing dependencies")
    void testGenerateBuildSuggestion_WithMissingDependencies() {
        String className = "com.google.gson.Gson";
        ModuleContext context = ModuleContext.builder()
            .baseDirectory(Path.of("/path/to/module"))
            .build();
        
        DependencyInfo dep = DependencyInfo.builder()
            .groupId("com.google.code.gson")
            .artifactId("gson")
            .version("2.10.1")
            .type("jar")
            .build();
        
        List<DependencyInfo> missingDependencies = List.of(dep);

        String suggestion = generator.generateBuildSuggestion(className, context, missingDependencies);

        assertNotNull(suggestion);
        assertTrue(suggestion.contains("com.google.code.gson:gson:2.10.1"));
        assertTrue(suggestion.contains("mvn dependency:get"));
        assertTrue(suggestion.contains("-Dclassifier=sources"));
    }

    @Test
    @DisplayName("Should generate package search suggestion for single package")
    void testGeneratePackageSearchSuggestion_SinglePackage() {
        String simpleClassName = "List";
        List<String> possiblePackages = List.of("java.util");

        String suggestion = generator.generatePackageSearchSuggestion(simpleClassName, possiblePackages);

        assertNotNull(suggestion);
        assertTrue(suggestion.contains("java.util.List"));
        assertTrue(suggestion.contains("one possible package"));
    }

    @Test
    @DisplayName("Should generate package search suggestion for multiple packages")
    void testGeneratePackageSearchSuggestion_MultiplePackages() {
        String simpleClassName = "Factory";
        List<String> possiblePackages = List.of(
            "com.google.inject",
            "org.springframework.beans.factory",
            "javax.inject"
        );

        String suggestion = generator.generatePackageSearchSuggestion(simpleClassName, possiblePackages);

        assertNotNull(suggestion);
        assertTrue(suggestion.contains("3 possible packages"));
        assertTrue(suggestion.contains("com.google.inject.Factory"));
        assertTrue(suggestion.contains("org.springframework.beans.factory.Factory"));
        assertTrue(suggestion.contains("javax.inject.Factory"));
    }

    @Test
    @DisplayName("Should generate package search suggestion for no packages")
    void testGeneratePackageSearchSuggestion_NoPackages() {
        String simpleClassName = "UnknownClass";
        List<String> possiblePackages = List.of();

        String suggestion = generator.generatePackageSearchSuggestion(simpleClassName, possiblePackages);

        assertNotNull(suggestion);
        assertTrue(suggestion.contains("No packages found"));
        assertTrue(suggestion.contains("build the project first"));
    }

    @Test
    @DisplayName("Should include Maven commands in build suggestion")
    void testGenerateBuildSuggestion_IncludesMavenCommands() {
        String className = "com.example.SomeClass";
        ModuleContext context = ModuleContext.builder()
            .baseDirectory(Path.of("/path/to/module"))
            .scope(Scope.TEST)
            .activeProfiles(List.of("profile1", "profile2"))
            .build();
        List<DependencyInfo> missingDependencies = List.of();

        String suggestion = generator.generateBuildSuggestion(className, context, missingDependencies);

        assertNotNull(suggestion);
        assertTrue(suggestion.contains("mvn"));
        assertTrue(suggestion.contains("test-compile"));
        assertTrue(suggestion.contains("-Pprofile1,profile2"));
    }
}