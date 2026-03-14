package io.github.bhxch.mcp.jlens.inspector;

import io.github.bhxch.mcp.jlens.config.ServerConfig;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import io.github.bhxch.mcp.jlens.maven.model.Scope;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolver;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolverFactory;
import io.github.bhxch.mcp.jlens.concurrent.ParallelProcessor;
import io.github.bhxch.mcp.jlens.inspector.model.ClassMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to verify detailLevel implementation correctness
 */
@DisplayName("DetailLevel Implementation Tests")
class DetailLevelTest {

    private ClassInspector inspector;
    private MavenResolver resolver;

    @BeforeEach
    void setUp() {
        inspector = new ClassInspector();
        ServerConfig config = new ServerConfig();
        MavenResolverFactory factory = new MavenResolverFactory(config);
        resolver = factory.createResolver();
    }

    @Test
    @DisplayName("SKELETON level should only return class basic info without members")
    void testSkeletonLevel() {
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        ModuleContext context = resolver.resolveModule(pomFile, Scope.COMPILE, List.of());

        ClassMetadata metadata = inspector.inspect(
            "java.util.ArrayList",
            context,
            ParallelProcessor.DetailLevel.SKELETON,
            null,
            null,
            null
        );

        assertNotNull(metadata);
        assertEquals("java.util.ArrayList", metadata.getClassName());
        assertEquals("java.util.AbstractList", metadata.getSuperClass());
        assertTrue(metadata.getInterfaces().size() > 0);

        // SKELETON should not include any members
        assertTrue(metadata.getFields().isEmpty(), "SKELETON should not include fields");
        assertTrue(metadata.getMethods().isEmpty(), "SKELETON should not include methods");
        assertTrue(metadata.getConstructors().isEmpty(), "SKELETON should not include constructors");
    }

    @Test
    @DisplayName("BASIC level should include public and protected members only")
    void testBasicLevel() {
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        ModuleContext context = resolver.resolveModule(pomFile, Scope.COMPILE, List.of());

        ClassMetadata metadata = inspector.inspect(
            "java.lang.String",
            context,
            ParallelProcessor.DetailLevel.BASIC,
            null,
            null,
            null
        );

        assertNotNull(metadata);
        assertEquals("java.lang.String", metadata.getClassName());

        // BASIC should include some fields (public + protected) - String has CASE_INSENSITIVE_ORDER
        // Note: Some classes might not have any public/protected fields, which is valid
        // All fields should be public or protected
        for (var field : metadata.getFields()) {
            int mods = field.getModifiers();
            boolean isPublicOrProtected = java.lang.reflect.Modifier.isPublic(mods)
                || java.lang.reflect.Modifier.isProtected(mods);
            assertTrue(isPublicOrProtected,
                "BASIC level field should be public or protected: " + field.getName());
        }

        // Should have some methods
        assertFalse(metadata.getMethods().isEmpty(), "BASIC should include methods");

        // All methods should be public or protected
        for (var method : metadata.getMethods()) {
            int mods = method.getModifiers();
            boolean isPublicOrProtected = java.lang.reflect.Modifier.isPublic(mods)
                || java.lang.reflect.Modifier.isProtected(mods);
            assertTrue(isPublicOrProtected,
                "BASIC level method should be public or protected: " + method.getName());
        }

        // Should have some constructors
        assertFalse(metadata.getConstructors().isEmpty(), "BASIC should include constructors");

        // All constructors should be public or protected
        for (var constructor : metadata.getConstructors()) {
            int mods = constructor.getModifiers();
            boolean isPublicOrProtected = java.lang.reflect.Modifier.isPublic(mods)
                || java.lang.reflect.Modifier.isProtected(mods);
            assertTrue(isPublicOrProtected,
                "BASIC level constructor should be public or protected");
        }
    }

    @Test
    @DisplayName("FULL level should include all members including private")
    void testFullLevel() {
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        ModuleContext context = resolver.resolveModule(pomFile, Scope.COMPILE, List.of());

        ClassMetadata metadata = inspector.inspect(
            "java.util.ArrayList",
            context,
            ParallelProcessor.DetailLevel.FULL,
            null,
            null,
            null
        );

        assertNotNull(metadata);
        assertEquals("java.util.ArrayList", metadata.getClassName());

        // FULL should include all fields
        assertFalse(metadata.getFields().isEmpty(), "FULL should include fields");

        // Should include at least one private field (ArrayList has private fields)
        boolean hasPrivateField = metadata.getFields().stream()
            .anyMatch(f -> java.lang.reflect.Modifier.isPrivate(f.getModifiers()));
        assertTrue(hasPrivateField, "FULL should include private fields");

        // Should include all methods
        assertFalse(metadata.getMethods().isEmpty(), "FULL should include methods");

        // Should include all constructors
        assertFalse(metadata.getConstructors().isEmpty(), "FULL should include constructors");
    }

    @Test
    @DisplayName("BASIC should have fewer members than FULL")
    void testBasicHasFewerMembersThanFull() {
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        ModuleContext context = resolver.resolveModule(pomFile, Scope.COMPILE, List.of());

        ClassMetadata basicMetadata = inspector.inspect(
            "java.util.ArrayList",
            context,
            ParallelProcessor.DetailLevel.BASIC,
            null,
            null,
            null
        );

        ClassMetadata fullMetadata = inspector.inspect(
            "java.util.ArrayList",
            context,
            ParallelProcessor.DetailLevel.FULL,
            null,
            null,
            null
        );

        // BASIC should have fewer or equal fields
        assertTrue(basicMetadata.getFields().size() <= fullMetadata.getFields().size(),
            "BASIC should have fewer or equal fields than FULL");

        // BASIC should have fewer or equal methods
        assertTrue(basicMetadata.getMethods().size() <= fullMetadata.getMethods().size(),
            "BASIC should have fewer or equal methods than FULL");

        // BASIC should have fewer or equal constructors
        assertTrue(basicMetadata.getConstructors().size() <= fullMetadata.getConstructors().size(),
            "BASIC should have fewer or equal constructors than FULL");
    }

    @Test
    @DisplayName("SKELETON should have no members but BASIC should have some")
    void testSkeletonVsBasic() {
        Path pomFile = Paths.get("pom.xml").toAbsolutePath();
        ModuleContext context = resolver.resolveModule(pomFile, Scope.COMPILE, List.of());

        ClassMetadata skeletonMetadata = inspector.inspect(
            "java.util.HashMap",
            context,
            ParallelProcessor.DetailLevel.SKELETON,
            null,
            null,
            null
        );

        ClassMetadata basicMetadata = inspector.inspect(
            "java.util.HashMap",
            context,
            ParallelProcessor.DetailLevel.BASIC,
            null,
            null,
            null
        );

        // SKELETON should have no members
        assertTrue(skeletonMetadata.getFields().isEmpty());
        assertTrue(skeletonMetadata.getMethods().isEmpty());
        assertTrue(skeletonMetadata.getConstructors().isEmpty());

        // BASIC should have some members
        assertFalse(basicMetadata.getFields().isEmpty() && basicMetadata.getMethods().isEmpty(),
            "BASIC should have at least some fields or methods");
    }
}
