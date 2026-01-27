package io.github.bhxch.mcp.jlens.unit.maven;

import io.github.bhxch.mcp.jlens.maven.model.MavenProject;
import io.github.bhxch.mcp.jlens.maven.model.DependencyInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MavenProject Unit Tests")
class MavenProjectTest {

    @Test
    @DisplayName("Should create MavenProject with all fields")
    void testMavenProjectCreation() {
        Path pomPath = Path.of("pom.xml");
        Path baseDir = Path.of(".");
        DependencyInfo dep = DependencyInfo.builder()
                .groupId("g")
                .artifactId("a")
                .version("1.0")
                .build();
        
        MavenProject project = new MavenProject(
                pomPath,
                "com.test",
                "test-project",
                "1.0.0",
                "jar",
                "Test Project",
                "Description",
                List.of(dep),
                Map.of("prop", "value"),
                List.of(),
                baseDir
        );

        assertEquals("com.test", project.getGroupId());
        assertEquals("test-project", project.getArtifactId());
        assertEquals("1.0.0", project.getVersion());
        assertEquals(pomPath, project.getPomFile());
        assertEquals(1, project.getDependencies().size());
        assertEquals("com.test:test-project:1.0.0", project.getCoordinates());
        assertFalse(project.isMultiModule());
    }
}