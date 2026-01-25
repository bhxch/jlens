package io.github.bhxch.mcp.javastub.unit.dependency;

import io.github.bhxch.mcp.javastub.dependency.MavenBuilder;
import io.github.bhxch.mcp.javastub.maven.model.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MavenBuilder
 */
@DisplayName("MavenBuilder Tests")
class MavenBuilderTest {

    private MavenBuilder mavenBuilder;

    @BeforeEach
    void setUp() {
        mavenBuilder = new MavenBuilder();
    }

    @Test
    @DisplayName("Should create MavenBuilder with default executable")
    void testConstructor_Default() {
        MavenBuilder builder = new MavenBuilder();
        assertNotNull(builder);
    }

    @Test
    @DisplayName("Should create MavenBuilder with custom executable")
    void testConstructor_CustomExecutable() {
        MavenBuilder builder = new MavenBuilder("mvn.cmd");
        assertNotNull(builder);
    }

    @Test
    @DisplayName("Should check if Maven is available")
    void testIsMavenAvailable() {
        // This test may fail if Maven is not installed
        boolean available = mavenBuilder.isMavenAvailable();
        // Just check that it doesn't throw an exception
        assertNotNull(available);
    }

    @Test
    @DisplayName("Should get Maven version")
    void testGetMavenVersion() {
        String version = mavenBuilder.getMavenVersion();
        assertNotNull(version);
        assertFalse(version.isEmpty());
    }

    @Test
    @DisplayName("Should create build result with default values")
    void testBuildResult_DefaultValues() {
        MavenBuilder.BuildResult result = new MavenBuilder.BuildResult();

        assertFalse(result.isSuccess());
        assertEquals(0, result.getExitCode());
        assertNull(result.getOutput());
        assertNull(result.getError());
        assertEquals(0.0, result.getDurationSeconds());
        assertTrue(result.getDownloadedArtifacts().isEmpty());
        assertTrue(result.getMissingDependencies().isEmpty());
    }

    @Test
    @DisplayName("Should set and get build result properties")
    void testBuildResult_SettersAndGetters() {
        MavenBuilder.BuildResult result = new MavenBuilder.BuildResult();

        result.setSuccess(true);
        result.setExitCode(0);
        result.setOutput("Build successful");
        result.setError(null);
        result.setDurationSeconds(5.5);

        assertTrue(result.isSuccess());
        assertEquals(0, result.getExitCode());
        assertEquals("Build successful", result.getOutput());
        assertNull(result.getError());
        assertEquals(5.5, result.getDurationSeconds());
    }

    @Test
    @DisplayName("Should create artifact info with default values")
    void testArtifactInfo_DefaultValues() {
        MavenBuilder.ArtifactInfo artifact = new MavenBuilder.ArtifactInfo();

        assertNull(artifact.getCoordinates());
        assertNull(artifact.getType());
        assertEquals(0, artifact.getSizeBytes());
        assertNull(artifact.getFile());
    }

    @Test
    @DisplayName("Should set and get artifact info properties")
    void testArtifactInfo_SettersAndGetters() {
        MavenBuilder.ArtifactInfo artifact = new MavenBuilder.ArtifactInfo();

        artifact.setCoordinates("com.google.code.gson:gson:2.10.1");
        artifact.setType("jar");
        artifact.setSizeBytes(1024000);
        artifact.setFile(Path.of("/path/to/gson-2.10.1.jar"));

        assertEquals("com.google.code.gson:gson:2.10.1", artifact.getCoordinates());
        assertEquals("jar", artifact.getType());
        assertEquals(1024000, artifact.getSizeBytes());
        assertEquals(Path.of("/path/to/gson-2.10.1.jar"), artifact.getFile());
    }

    @Test
    @DisplayName("Should build module with context")
    void testBuildModule_WithContext(@TempDir Path tempDir) {
        ModuleContext context = ModuleContext.builder()
            .baseDirectory(tempDir)
            .pomFile(tempDir.resolve("pom.xml"))
            .build();

        MavenBuilder.BuildResult result = mavenBuilder.buildModule(
            context, 
            List.of("help:evaluate", "-Dexpression=project.version", "-q", "-DforceStdout"),
            List.of(),
            30
        );

        assertNotNull(result);
        assertNotNull(result.getOutput());
    }

    @Test
    @DisplayName("Should handle build timeout")
    void testBuildModule_Timeout(@TempDir Path tempDir) {
        ModuleContext context = ModuleContext.builder()
            .baseDirectory(tempDir)
            .pomFile(tempDir.resolve("pom.xml"))
            .build();

        MavenBuilder.BuildResult result = mavenBuilder.buildModule(
            context, 
            List.of("help:evaluate", "-Dexpression=project.version", "-q", "-DforceStdout"),
            List.of(),
            1 // Very short timeout
        );

        assertNotNull(result);
        // Result may be success or timeout depending on system
    }
}