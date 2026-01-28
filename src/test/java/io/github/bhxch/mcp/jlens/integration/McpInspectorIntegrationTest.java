package io.github.bhxch.mcp.jlens.integration;

import io.github.bhxch.mcp.jlens.cache.CacheManager;
import io.github.bhxch.mcp.jlens.config.ServerConfig;
import io.github.bhxch.mcp.jlens.inspector.ClassInspector;
import io.github.bhxch.mcp.jlens.inspector.model.ClassMetadata;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import io.github.bhxch.mcp.jlens.maven.model.Scope;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolver;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolverFactory;
import io.github.bhxch.mcp.jlens.concurrent.ParallelProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class McpInspectorIntegrationTest {

    private ClassInspector inspector;
    private MavenResolver resolver;
    private Path javaHome;
    private Path pomFile;

    @BeforeEach
    void setUp() {
        inspector = new ClassInspector();
        ServerConfig config = new ServerConfig();
        MavenResolverFactory factory = new MavenResolverFactory(config);
        resolver = factory.createResolver();
        
        String javaHomeStr = System.getenv("JAVA_HOME");
        if (javaHomeStr == null) {
            javaHomeStr = "C:\\Users\\bhxch\\.jdk\\25"; // Fallback
        }
        javaHome = Paths.get(javaHomeStr);
        pomFile = Paths.get("pom.xml");
    }

    @Test
    void testInspectJdkList() {
        ModuleContext context = resolver.resolveModule(pomFile, Scope.COMPILE, List.of());
        ClassMetadata metadata = inspector.inspect("java.util.List", context, 
            ParallelProcessor.DetailLevel.FULL, null, null, javaHome);
        
        assertNotNull(metadata);
        assertEquals("java.util.List", metadata.getClassName());
        assertTrue(metadata.isInterface());
        
        // Verify @since
        assertNotNull(metadata.getSince(), "List should have @since info");
        assertEquals("1.2", metadata.getSince());
    }

    @Test
    void testInspectJdkStream() {
        ModuleContext context = resolver.resolveModule(pomFile, Scope.COMPILE, List.of());
        ClassMetadata metadata = inspector.inspect("java.util.stream.Stream", context, 
            ParallelProcessor.DetailLevel.FULL, null, null, javaHome);
        
        assertNotNull(metadata);
        assertEquals("1.8", metadata.getSince());
        
        // Check a method added in later version if any
        // Stream.ofNullable was added in 9
        metadata.getMethods().stream()
            .filter(m -> m.getName().equals("ofNullable"))
            .findFirst()
            .ifPresent(m -> assertEquals("9", m.getSince()));
    }

    @Test
    void testInspectJdkOptional() {
        ModuleContext context = resolver.resolveModule(pomFile, Scope.COMPILE, List.of());
        ClassMetadata metadata = inspector.inspect("java.util.Optional", context, 
            ParallelProcessor.DetailLevel.FULL, null, null, javaHome);
        
        assertNotNull(metadata);
        assertEquals("1.8", metadata.getSince());
    }

    @Test
    void testDetailLevelSkeleton() {
        ModuleContext context = resolver.resolveModule(pomFile, Scope.COMPILE, List.of());
        ClassMetadata metadata = inspector.inspect("java.lang.String", context, 
            ParallelProcessor.DetailLevel.SKELETON, null, null, javaHome);
        
        assertNotNull(metadata);
        assertTrue(metadata.getMethods().isEmpty());
        assertTrue(metadata.getFields().isEmpty());
    }

    @Test
    void testDetailLevelBasic() {
        ModuleContext context = resolver.resolveModule(pomFile, Scope.COMPILE, List.of());
        ClassMetadata metadata = inspector.inspect("java.lang.String", context, 
            ParallelProcessor.DetailLevel.BASIC, null, null, javaHome);
        
        assertNotNull(metadata);
        assertFalse(metadata.getMethods().isEmpty());
        // All methods in BASIC should be public
        assertTrue(metadata.getMethods().stream().allMatch(m -> 
            (m.getModifiers() & 0x0001) != 0)); // Modifier.PUBLIC = 1
    }

    @Test
    void testInterfaceFields() {
        // java.sql.Connection has many public static final fields
        ModuleContext context = resolver.resolveModule(pomFile, Scope.COMPILE, List.of());
        ClassMetadata metadata = inspector.inspect("java.sql.Connection", context, 
            ParallelProcessor.DetailLevel.FULL, null, null, javaHome);
        
        assertNotNull(metadata);
        assertTrue(metadata.isInterface());
        assertFalse(metadata.getFields().isEmpty(), "Interface should have fields if they exist");
    }
}
