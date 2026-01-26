package io.github.bhxch.mcp.jlens.unit.maven;

import io.github.bhxch.mcp.jlens.maven.model.DependencyInfo;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import io.github.bhxch.mcp.jlens.maven.model.Scope;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenDirectResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MavenDirectResolver Unit Tests")
class MavenDirectResolverTest {

    private final MavenDirectResolver resolver = new MavenDirectResolver();

    @Test
    @DisplayName("Should resolve module from valid POM file")
    void testResolveModule(@TempDir Path tempDir) throws Exception {
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0</version>
                <dependencies>
                    <dependency>
                        <groupId>org.junit.jupiter</groupId>
                        <artifactId>junit-jupiter</artifactId>
                        <version>5.10.0</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
            </project>
            """;

        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, pomContent);

        ModuleContext context = resolver.resolveModule(pomFile, Scope.COMPILE, List.of());

        assertNotNull(context);
        assertEquals("com.example", context.getGroupId());
        assertEquals("test-project", context.getArtifactId());
        assertEquals("1.0.0", context.getVersion());
        assertEquals(tempDir, context.getBaseDirectory());
        assertEquals(pomFile, context.getPomFile());
    }

    @Test
    @DisplayName("Should extract dependencies from POM")
    void testExtractDependencies(@TempDir Path tempDir) throws Exception {
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0</version>
                <dependencies>
                    <dependency>
                        <groupId>org.junit.jupiter</groupId>
                        <artifactId>junit-jupiter</artifactId>
                        <version>5.10.0</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>com.google.guava</groupId>
                        <artifactId>guava</artifactId>
                        <version>32.0.0</version>
                    </dependency>
                </dependencies>
            </project>
            """;

        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, pomContent);

        ModuleContext context = resolver.resolveModule(pomFile, Scope.COMPILE, List.of());

        assertFalse(context.getDependencies().isEmpty());

        boolean foundGuava = context.getDependencies().stream()
            .anyMatch(dep -> dep.getArtifactId().equals("guava"));
        assertTrue(foundGuava);
    }

    @Test
    @DisplayName("Should throw exception for non-existent POM file")
    void testNonExistentPomFile(@TempDir Path tempDir) {
        Path nonExistentPom = tempDir.resolve("nonexistent.xml");

        assertThrows(IllegalArgumentException.class, () -> {
            resolver.resolveModule(nonExistentPom, Scope.COMPILE, List.of());
        });
    }

    @Test
    @DisplayName("Should handle POM with no dependencies")
    void testNoDependencies(@TempDir Path tempDir) throws Exception {
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0</version>
            </project>
            """;

        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, pomContent);

        ModuleContext context = resolver.resolveModule(pomFile, Scope.COMPILE, List.of());

        assertTrue(context.getDependencies().isEmpty());
    }

    @Test
    @DisplayName("Should return correct resolver name")
    void testResolverName() {
        assertEquals("DirectResolver", resolver.getName());
    }

    @Test
    @DisplayName("Should always be available")
    void testAvailability() {
        assertTrue(resolver.isAvailable());
    }
}



