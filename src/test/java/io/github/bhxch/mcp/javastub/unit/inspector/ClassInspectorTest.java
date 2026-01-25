package io.github.bhxch.mcp.javastub.unit.inspector;

import io.github.bhxch.mcp.javastub.inspector.ClassInspector;
import io.github.bhxch.mcp.javastub.inspector.model.ClassMetadata;
import io.github.bhxch.mcp.javastub.maven.model.ModuleContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ClassInspector Unit Tests")
class ClassInspectorTest {

    private final ClassInspector inspector = new ClassInspector();

    @Test
    @DisplayName("Should inspect simple class name")
    void testInspectSimpleClassName() {
        ClassMetadata metadata = inspector.inspect("TestClass", null,
            io.github.bhxch.mcp.javastub.concurrent.ParallelProcessor.DetailLevel.BASIC, null);

        assertNotNull(metadata);
        assertEquals("TestClass", metadata.getClassName());
        assertEquals("", metadata.getPackageName());
        assertEquals("TestClass", metadata.getSimpleClassName());
    }

    @Test
    @DisplayName("Should inspect fully qualified class name")
    void testInspectFullyQualifiedClassName() {
        ClassMetadata metadata = inspector.inspect("com.example.TestClass", null,
            io.github.bhxch.mcp.javastub.concurrent.ParallelProcessor.DetailLevel.BASIC, null);

        assertNotNull(metadata);
        assertEquals("com.example.TestClass", metadata.getClassName());
        assertEquals("com.example", metadata.getPackageName());
        assertEquals("TestClass", metadata.getSimpleClassName());
    }

    @Test
    @DisplayName("Should inspect class with source file")
    void testInspectWithSourceFile() {
        Path sourceFile = Path.of("/path/to/TestClass.java");

        ClassMetadata metadata = inspector.inspect("com.example.TestClass", null,
            io.github.bhxch.mcp.javastub.concurrent.ParallelProcessor.DetailLevel.BASIC, sourceFile);

        assertNotNull(metadata);
        assertEquals(sourceFile.toString(), metadata.getSourceFile());
    }

    @Test
    @DisplayName("Should handle different detail levels")
    void testDifferentDetailLevels() {
        for (io.github.bhxch.mcp.javastub.concurrent.ParallelProcessor.DetailLevel level :
             io.github.bhxch.mcp.javastub.concurrent.ParallelProcessor.DetailLevel.values()) {

            ClassMetadata metadata = inspector.inspect("com.example.TestClass", null, level, null);

            assertNotNull(metadata);
            assertEquals("com.example.TestClass", metadata.getClassName());
        }
    }

    @Test
    @DisplayName("Should handle module context")
    void testWithModuleContext() {
        ModuleContext context = ModuleContext.builder()
            .groupId("com.example")
            .artifactId("test-artifact")
            .version("1.0.0")
            .build();

        ClassMetadata metadata = inspector.inspect("com.example.TestClass", context,
            io.github.bhxch.mcp.javastub.concurrent.ParallelProcessor.DetailLevel.FULL, null);

        assertNotNull(metadata);
        assertEquals("com.example.TestClass", metadata.getClassName());
    }

    @Test
    @DisplayName("Should handle package-less class")
    void testPackageLessClass() {
        ClassMetadata metadata = inspector.inspect("SimpleClass", null,
            io.github.bhxch.mcp.javastub.concurrent.ParallelProcessor.DetailLevel.BASIC, null);

        assertNotNull(metadata);
        assertEquals("", metadata.getPackageName());
        assertEquals("SimpleClass", metadata.getSimpleClassName());
    }

    @Test
    @DisplayName("Should handle nested class name")
    void testNestedClassName() {
        ClassMetadata metadata = inspector.inspect("com.example.OuterClass$InnerClass", null,
            io.github.bhxch.mcp.javastub.concurrent.ParallelProcessor.DetailLevel.BASIC, null);

        assertNotNull(metadata);
        assertEquals("com.example.OuterClass$InnerClass", metadata.getClassName());
        assertEquals("com.example", metadata.getPackageName());
        assertEquals("OuterClass$InnerClass", metadata.getSimpleClassName());
    }
}