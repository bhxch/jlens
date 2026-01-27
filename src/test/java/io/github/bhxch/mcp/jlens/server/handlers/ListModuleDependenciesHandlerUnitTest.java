package io.github.bhxch.mcp.jlens.server.handlers;

import io.github.bhxch.mcp.jlens.maven.model.DependencyInfo;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import io.github.bhxch.mcp.jlens.maven.model.Scope;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolver;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListModuleDependenciesHandlerUnitTest {

    @Mock
    private MavenResolverFactory resolverFactory;
    @Mock
    private McpSyncServerExchange exchange;
    @Mock
    private MavenResolver mavenResolver;

    private ListModuleDependenciesHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ListModuleDependenciesHandler(resolverFactory);
    }

    @Test
    void testHandleSuccessfulResolution() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = mockStatic(Paths.class)) {
            
            Path mockPom = mock(Path.class);
            pathsMock.when(() -> Paths.get("pom.xml")).thenReturn(mockPom);
            filesMock.when(() -> Files.exists(mockPom)).thenReturn(true);

            when(resolverFactory.createResolver()).thenReturn(mavenResolver);
            ModuleContext mockContext = mock(ModuleContext.class);
            DependencyInfo dep = DependencyInfo.builder()
                    .groupId("g")
                    .artifactId("a")
                    .version("1.0")
                    .scope(Scope.COMPILE)
                    .build();
            when(mockContext.getDependencies()).thenReturn(List.of(dep));
            when(mockContext.getGroupId()).thenReturn("g.main");
            when(mockContext.getArtifactId()).thenReturn("a.main");
            when(mockContext.getVersion()).thenReturn("1.0.0");
            when(mavenResolver.resolveModule(any(), any(), any())).thenReturn(mockContext);

            Map<String, Object> arguments = new HashMap<>();
            arguments.put("pomFilePath", "pom.xml");
            arguments.put("scope", "compile");
            CallToolRequest request = new CallToolRequest("list_module_dependencies", arguments);

            CallToolResult result = handler.handle(exchange, request);

            assertFalse(result.isError());
            String text = ((TextContent) result.content().get(0)).text();
            assertTrue(text.contains("g:a:1.0"));
            assertTrue(text.contains("g.main"));
        }
    }

    @Test
    void testHandleWithSourceFilePath() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = mockStatic(Paths.class)) {
            
            Path mockSrc = mock(Path.class);
            Path mockParent = mock(Path.class);
            Path mockPom = mock(Path.class);
            
            pathsMock.when(() -> Paths.get("src/Main.java")).thenReturn(mockSrc);
            when(mockSrc.getParent()).thenReturn(mockParent);
            when(mockParent.resolve("pom.xml")).thenReturn(mockPom);
            filesMock.when(() -> Files.exists(mockSrc)).thenReturn(true);
            filesMock.when(() -> Files.exists(mockPom)).thenReturn(true);

            when(resolverFactory.createResolver()).thenReturn(mavenResolver);
            when(mavenResolver.resolveModule(any(), any(), any())).thenReturn(mock(ModuleContext.class));

            Map<String, Object> arguments = new HashMap<>();
            arguments.put("sourceFilePath", "src/Main.java");
            CallToolRequest request = new CallToolRequest("list_module_dependencies", arguments);

            CallToolResult result = handler.handle(exchange, request);
            assertNotNull(result);
            verify(mavenResolver).resolveModule(eq(mockPom), any(), any());
        }
    }

    @Test
    void testNoFilePathProvided() {
        Map<String, Object> arguments = new HashMap<>();
        CallToolRequest request = new CallToolRequest("list_module_dependencies", arguments);

        CallToolResult result = handler.handle(exchange, request);
        assertTrue(result.isError());
        assertTrue(((TextContent) result.content().get(0)).text().contains("Either pomFilePath or sourceFilePath must be provided"));
    }
}
