package io.github.bhxch.mcp.javastub.unit.classpath;

import io.github.bhxch.mcp.javastub.classpath.PackageMappingResolver;
import io.github.bhxch.mcp.javastub.maven.model.ModuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PackageMappingResolver
 */
@DisplayName("PackageMappingResolver Tests")
class PackageMappingResolverTest {

    private PackageMappingResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new PackageMappingResolver();
    }

    @Test
    @DisplayName("Should resolve fully qualified class name")
    void testResolveClassName_FullyQualifiedName() {
        String simpleName = "java.util.List";
        List<String> imports = List.of();
        String currentPackage = "com.example";

        PackageMappingResolver.ClassResolutionResult result = 
            resolver.resolveClassName(simpleName, imports, currentPackage);

        assertEquals("java.util.List", result.getResolvedClassName());
        assertEquals(PackageMappingResolver.ClassResolutionResult.ResolutionType.FULLY_QUALIFIED, 
            result.getResolutionType());
    }

    @Test
    @DisplayName("Should resolve class from same package")
    void testResolveClassName_SamePackage() {
        String simpleName = "MyClass";
        List<String> imports = List.of();
        String currentPackage = "com.example";

        // Manually add class to index
        resolver.getClassToPackages().put("MyClass", java.util.Set.of("com.example"));

        PackageMappingResolver.ClassResolutionResult result = 
            resolver.resolveClassName(simpleName, imports, currentPackage);

        assertEquals("com.example.MyClass", result.getResolvedClassName());
        assertEquals(PackageMappingResolver.ClassResolutionResult.ResolutionType.SAME_PACKAGE, 
            result.getResolutionType());
    }

    @Test
    @DisplayName("Should resolve class from explicit import")
    void testResolveClassName_ExplicitImport() {
        String simpleName = "List";
        List<String> imports = List.of("java.util.List");
        String currentPackage = "com.example";

        // Manually add class to index
        resolver.getClassToPackages().put("List", java.util.Set.of("java.util"));

        PackageMappingResolver.ClassResolutionResult result = 
            resolver.resolveClassName(simpleName, imports, currentPackage);

        assertEquals("java.util.List", result.getResolvedClassName());
        assertEquals(PackageMappingResolver.ClassResolutionResult.ResolutionType.EXPLICIT_IMPORT, 
            result.getResolutionType());
    }

    @Test
    @DisplayName("Should resolve class from wildcard import")
    void testResolveClassName_WildcardImport() {
        String simpleName = "ArrayList";
        List<String> imports = List.of("java.util.*");
        String currentPackage = "com.example";

        // Manually add class to index
        resolver.getClassToPackages().put("ArrayList", java.util.Set.of("java.util"));

        PackageMappingResolver.ClassResolutionResult result = 
            resolver.resolveClassName(simpleName, imports, currentPackage);

        assertEquals("java.util.ArrayList", result.getResolvedClassName());
        assertEquals(PackageMappingResolver.ClassResolutionResult.ResolutionType.WILDCARD_IMPORT, 
            result.getResolutionType());
    }

    @Test
    @DisplayName("Should resolve java.lang class")
    void testResolveClassName_JavaLangClass() {
        String simpleName = "String";
        List<String> imports = List.of();
        String currentPackage = "com.example";

        PackageMappingResolver.ClassResolutionResult result = 
            resolver.resolveClassName(simpleName, imports, currentPackage);

        assertEquals("java.lang.String", result.getResolvedClassName());
        assertEquals(PackageMappingResolver.ClassResolutionResult.ResolutionType.JAVA_LANG, 
            result.getResolutionType());
    }

    @Test
    @DisplayName("Should return ambiguous result for multiple packages")
    void testResolveClassName_Ambiguous() {
        String simpleName = "Factory";
        List<String> imports = List.of();
        String currentPackage = "com.example";

        // Manually add class to index with multiple packages
        resolver.getClassToPackages().put("Factory", java.util.Set.of(
            "com.google.inject",
            "org.springframework.beans.factory",
            "javax.inject"
        ));

        PackageMappingResolver.ClassResolutionResult result = 
            resolver.resolveClassName(simpleName, imports, currentPackage);

        assertEquals(PackageMappingResolver.ClassResolutionResult.ResolutionType.AMBIGUOUS, 
            result.getResolutionType());
        assertEquals(3, result.getPossiblePackages().size());
    }

    @Test
    @DisplayName("Should return not found result for unknown class")
    void testResolveClassName_NotFound() {
        String simpleName = "UnknownClass";
        List<String> imports = List.of();
        String currentPackage = "com.example";

        PackageMappingResolver.ClassResolutionResult result = 
            resolver.resolveClassName(simpleName, imports, currentPackage);

        assertEquals(PackageMappingResolver.ClassResolutionResult.ResolutionType.NOT_FOUND, 
            result.getResolutionType());
    }

    @Test
    @DisplayName("Should parse imports from source code")
    void testParseImportsFromSource() {
        String sourceCode = """
            package com.example;
            
            import java.util.List;
            import java.util.ArrayList;
            import org.junit.Test;
            """;

        List<String> imports = resolver.parseImportsFromSource(sourceCode);

        assertEquals(3, imports.size());
        assertTrue(imports.contains("java.util.List"));
        assertTrue(imports.contains("java.util.ArrayList"));
        assertTrue(imports.contains("org.junit.Test"));
    }

    @Test
    @DisplayName("Should parse wildcard imports from source code")
    void testParseImportsFromSource_Wildcard() {
        String sourceCode = """
            package com.example;
            
            import java.util.*;
            import org.junit.jupiter.api.*;
            """;

        List<String> imports = resolver.parseImportsFromSource(sourceCode);

        assertEquals(2, imports.size());
        assertTrue(imports.contains("java.util.*"));
        assertTrue(imports.contains("org.junit.jupiter.api.*"));
    }

    @Test
    @DisplayName("Should get possible packages for class name")
    void testGetPossiblePackages() {
        resolver.getClassToPackages().put("List", java.util.Set.of("java.util", "java.awt"));

        List<String> packages = resolver.getPossiblePackages("List");

        assertEquals(2, packages.size());
        assertTrue(packages.contains("java.util"));
        assertTrue(packages.contains("java.awt"));
    }

    @Test
    @DisplayName("Should return empty list for unknown class")
    void testGetPossiblePackages_UnknownClass() {
        List<String> packages = resolver.getPossiblePackages("UnknownClass");

        assertTrue(packages.isEmpty());
    }
}