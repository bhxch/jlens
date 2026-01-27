package io.github.bhxch.mcp.jlens.unit.classpath;

import io.github.bhxch.mcp.jlens.classpath.ClassLoaderManager;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ClassLoaderManager Unit Tests")
class ClassLoaderManagerTest {

    private ClassLoaderManager manager;

    @BeforeEach
    void setUp() {
        manager = new ClassLoaderManager();
    }

    @Test
    @DisplayName("Should return system class loader for null context")
    void testGetClassLoader_NullContext() {
        ClassLoader loader = manager.getClassLoader(null);
        assertEquals(ClassLoader.getSystemClassLoader(), loader);
    }

    @Test
    @DisplayName("Should create and cache ClassLoader for context")
    void testGetClassLoader_ValidContext() {
        ModuleContext context = ModuleContext.builder()
            .pomFile(Path.of("pom.xml").toAbsolutePath())
            .artifactId("test-module")
            .classpathJars(List.of(Path.of("test.jar")))
            .build();

        ClassLoader loader1 = manager.getClassLoader(context);
        ClassLoader loader2 = manager.getClassLoader(context);

        assertNotNull(loader1);
        assertSame(loader1, loader2);
    }

    @Test
    @DisplayName("Should invalidate cache for context")
    void testInvalidate() {
        ModuleContext context = ModuleContext.builder()
            .pomFile(Path.of("pom.xml").toAbsolutePath())
            .artifactId("test-module")
            .build();

        ClassLoader loader1 = manager.getClassLoader(context);
        manager.invalidate(context);
        ClassLoader loader2 = manager.getClassLoader(context);

        assertNotNull(loader1);
        assertNotNull(loader2);
        assertNotSame(loader1, loader2);
    }

    @Test
    @DisplayName("Should clear all loaders")
    void testClear() {
        ModuleContext context = ModuleContext.builder()
            .pomFile(Path.of("pom.xml").toAbsolutePath())
            .artifactId("test-module")
            .build();

        ClassLoader loader1 = manager.getClassLoader(context);
        manager.clear();
        ClassLoader loader2 = manager.getClassLoader(context);

        assertNotSame(loader1, loader2);
    }
}
