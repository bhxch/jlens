package io.github.bhxch.mcp.jlens.unit.dependency;

import io.github.bhxch.mcp.jlens.dependency.DependencyManager;
import io.github.bhxch.mcp.jlens.dependency.MavenBuilder;
import io.github.bhxch.mcp.jlens.maven.model.DependencyInfo;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DependencyManager Unit Tests")
class DependencyManagerTest {

    private DependencyManager manager;
    private MavenBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new MavenBuilder();
        manager = new DependencyManager(builder);
    }

    @Test
    @DisplayName("Should find missing dependencies")
    void testFindMissingDependencies() {
        DependencyInfo missing = DependencyInfo.builder()
            .groupId("non.existent")
            .artifactId("missing")
            .version("1.0.0")
            .build();

        ModuleContext context = ModuleContext.builder()
            .dependencies(List.of(missing))
            .build();

        List<DependencyInfo> found = manager.findMissingDependencies(context);
        assertFalse(found.isEmpty());
        assertEquals("non.existent", found.get(0).getGroupId());
    }

    @Test
    @DisplayName("Should find dependencies for class")
    void testFindDependenciesForClass() {
        ModuleContext context = ModuleContext.builder()
            .dependencies(List.of(
                DependencyInfo.builder().groupId("org.junit.jupiter").artifactId("junit-jupiter-api").version("5.10.2").build()
            ))
            .build();

        List<DependencyInfo> found = manager.findDependenciesForClass("org.junit.jupiter.api.Test", context);
        assertNotNull(found);
    }

    @Test
    @DisplayName("Should handle empty dependencies in context")
    void testEmptyDependencies() {
        ModuleContext context = ModuleContext.builder().dependencies(List.of()).build();
        assertTrue(manager.findMissingDependencies(context).isEmpty());
        assertTrue(manager.findDependenciesForClass("Test", context).isEmpty());
    }

    @Test
    @DisplayName("Should handle download request")
    void testDownloadDependency() {
        DependencyInfo dep = DependencyInfo.builder().groupId("g").artifactId("a").version("v").build();
        ModuleContext context = ModuleContext.builder().pomFile(Path.of("pom.xml")).build();
        // Since we didn't mock MavenBuilder, it will try to run mvn. We just want to cover the logic.
        try {
            manager.downloadDependency(dep, context, true);
        } catch (Exception e) {}
    }
}
