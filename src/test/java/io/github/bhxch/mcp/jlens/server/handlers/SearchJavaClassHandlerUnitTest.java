package io.github.bhxch.mcp.jlens.server.handlers;

import io.github.bhxch.mcp.jlens.classpath.PackageMappingResolver;
import io.github.bhxch.mcp.jlens.dependency.DependencyManager;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolverFactory;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchJavaClassHandlerUnitTest {

    @Mock
    private PackageMappingResolver packageResolver;
    @Mock
    private DependencyManager dependencyManager;
    @Mock
    private MavenResolverFactory resolverFactory;
    @Mock
    private McpSyncServerExchange exchange;

    private SearchJavaClassHandler handler;

    @BeforeEach
    void setUp() {
        // Return a map to avoid NPE in buildDefaultClassIndex if called
        lenient().when(packageResolver.getClassToPackages()).thenReturn(new ConcurrentHashMap<>());
        handler = new SearchJavaClassHandler(packageResolver, dependencyManager, resolverFactory);
    }

    @Test
    void testHandleExactSearch() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("classNamePattern", "String");
        arguments.put("pomFilePath", "pom.xml");
        arguments.put("searchType", "exact");
        CallToolRequest request = new CallToolRequest("search_java_class", arguments);

        Map<String, Set<String>> mockData = new ConcurrentHashMap<>();
        mockData.put("String", Set.of("java.lang"));
        when(packageResolver.getClassToPackages()).thenReturn(mockData);

        CallToolResult result = handler.handle(exchange, request);

        assertFalse(result.isError());
        String content = ((TextContent) result.content().get(0)).text();
        assertTrue(content.contains("java.lang.String"));
    }

    @Test
    void testHandleWildcardSearch() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("classNamePattern", "Array*List");
        arguments.put("pomFilePath", "pom.xml");
        arguments.put("searchType", "wildcard");
        CallToolRequest request = new CallToolRequest("search_java_class", arguments);

        Map<String, Set<String>> mockData = new ConcurrentHashMap<>();
        mockData.put("ArrayList", Set.of("java.util"));
        mockData.put("CopyOnWriteArrayList", Set.of("java.util.concurrent"));
        when(packageResolver.getClassToPackages()).thenReturn(mockData);

        CallToolResult result = handler.handle(exchange, request);

        assertFalse(result.isError());
        String content = ((TextContent) result.content().get(0)).text();
        assertTrue(content.contains("java.util.ArrayList"));
        assertTrue(content.contains("java.util.concurrent.CopyOnWriteArrayList"));
    }

    @Test
    void testHandleWithProjectContext() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = mockStatic(Paths.class)) {
            
            Path mockPom = mock(Path.class);
            pathsMock.when(() -> Paths.get("pom.xml")).thenReturn(mockPom);
            filesMock.when(() -> Files.exists(mockPom)).thenReturn(true);

            Map<String, Object> arguments = new HashMap<>();
            arguments.put("classNamePattern", "MyClass");
            arguments.put("pomFilePath", "pom.xml");
            CallToolRequest request = new CallToolRequest("search_java_class", arguments);

            CallToolResult result = handler.handle(exchange, request);
            assertNotNull(result);
            verify(packageResolver).buildClassIndex(any());
        }
    }

    @Test
    void testPagination() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("classNamePattern", "Test");
        arguments.put("pomFilePath", "pom.xml");
        arguments.put("limit", 1);
        CallToolRequest request = new CallToolRequest("search_java_class", arguments);

        Map<String, Set<String>> mockData = new ConcurrentHashMap<>();
        mockData.put("Test1", Set.of("com"));
        mockData.put("Test2", Set.of("com"));
        when(packageResolver.getClassToPackages()).thenReturn(mockData);

        CallToolResult result = handler.handle(exchange, request);
        String content = ((TextContent) result.content().get(0)).text();
        
        assertTrue(content.contains("totalResults\":2"));
        assertTrue(content.contains("nextCursor"));
    }

    @Test
    void testMissingPattern() {
        Map<String, Object> arguments = new HashMap<>();
        CallToolRequest request = new CallToolRequest("search_java_class", arguments);

        CallToolResult result = handler.handle(exchange, request);
        assertTrue(result.isError());
        assertTrue(((TextContent) result.content().get(0)).text().contains("classNamePattern is required"));
    }
}