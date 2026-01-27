package io.github.bhxch.mcp.jlens.server.handlers;

import io.github.bhxch.mcp.jlens.dependency.DependencyManager;
import io.github.bhxch.mcp.jlens.dependency.MavenBuilder;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
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
public class BuildModuleHandlerUnitTest {

    @Mock
    private MavenBuilder mavenBuilder;
    @Mock
    private DependencyManager dependencyManager;
    @Mock
    private MavenResolverFactory resolverFactory;
    @Mock
    private McpSyncServerExchange exchange;
    @Mock
    private MavenResolver mavenResolver;

    private BuildModuleHandler handler;

    @BeforeEach
    void setUp() {
        handler = new BuildModuleHandler(mavenBuilder, dependencyManager, resolverFactory);
    }

    @Test
    void testHandleSuccessfulBuild() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = mockStatic(Paths.class)) {
            
            Path mockPom = mock(Path.class);
            pathsMock.when(() -> Paths.get("pom.xml")).thenReturn(mockPom);
            filesMock.when(() -> Files.exists(mockPom)).thenReturn(true);

            when(resolverFactory.createResolver()).thenReturn(mavenResolver);
            ModuleContext mockContext = mock(ModuleContext.class);
            when(mavenResolver.resolveModule(any(), any(), any())).thenReturn(mockContext);

            MavenBuilder.BuildResult mockResult = mock(MavenBuilder.BuildResult.class);
            when(mockResult.isSuccess()).thenReturn(true);
            when(mockResult.getOutput()).thenReturn("Build Success Output");
            when(mavenBuilder.buildModule(any(), any(), any(), anyInt())).thenReturn(mockResult);

            Map<String, Object> arguments = new HashMap<>();
            arguments.put("pomFilePath", "pom.xml");
            arguments.put("goals", List.of("clean", "install"));
            CallToolRequest request = new CallToolRequest("build_module", arguments);

            CallToolResult result = handler.handle(exchange, request);

            assertFalse(result.isError());
            String text = ((TextContent) result.content().get(0)).text();
            assertTrue(text.contains("success\":true"));
            assertTrue(text.contains("Build Success Output"));
        }
    }

    @Test
    void testHandleGoalParsingFromString() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = mockStatic(Paths.class)) {
            
            Path mockPom = mock(Path.class);
            pathsMock.when(() -> Paths.get("pom.xml")).thenReturn(mockPom);
            filesMock.when(() -> Files.exists(mockPom)).thenReturn(true);

            when(resolverFactory.createResolver()).thenReturn(mavenResolver);
            when(mavenBuilder.buildModule(any(), any(), any(), anyInt())).thenReturn(mock(MavenBuilder.BuildResult.class));

            Map<String, Object> arguments = new HashMap<>();
            arguments.put("pomFilePath", "pom.xml");
            arguments.put("goals", "clean,compile");
            CallToolRequest request = new CallToolRequest("build_module", arguments);

            handler.handle(exchange, request);

            verify(mavenBuilder).buildModule(any(), eq(List.of("clean", "compile")), any(), anyInt());
        }
    }

    @Test
    void testHandleBuildFailure() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = mockStatic(Paths.class)) {
            
            Path mockPom = mock(Path.class);
            pathsMock.when(() -> Paths.get("pom.xml")).thenReturn(mockPom);
            filesMock.when(() -> Files.exists(mockPom)).thenReturn(true);

            when(resolverFactory.createResolver()).thenReturn(mavenResolver);
            MavenBuilder.BuildResult mockResult = mock(MavenBuilder.BuildResult.class);
            when(mockResult.isSuccess()).thenReturn(false);
            when(mockResult.getOutput()).thenReturn("Build Error Detail");
            when(mavenBuilder.buildModule(any(), any(), any(), anyInt())).thenReturn(mockResult);

            Map<String, Object> arguments = new HashMap<>();
            arguments.put("pomFilePath", "pom.xml");
            CallToolRequest request = new CallToolRequest("build_module", arguments);

            CallToolResult result = handler.handle(exchange, request);

            assertFalse(result.isError()); // MCP success but internal build failure reported in JSON
            String text = ((TextContent) result.content().get(0)).text();
            assertTrue(text.contains("success\":false"));
            assertTrue(text.contains("Build Error Detail"));
        }
    }

    @Test
    void testMissingPomFile() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class);
             MockedStatic<Paths> pathsMock = mockStatic(Paths.class)) {
            
            Path mockPom = mock(Path.class);
            pathsMock.when(() -> Paths.get("non-existent.xml")).thenReturn(mockPom);
            filesMock.when(() -> Files.exists(mockPom)).thenReturn(false);

            Map<String, Object> arguments = new HashMap<>();
            arguments.put("pomFilePath", "non-existent.xml");
            CallToolRequest request = new CallToolRequest("build_module", arguments);

            CallToolResult result = handler.handle(exchange, request);
            assertTrue(result.isError());
            assertTrue(((TextContent) result.content().get(0)).text().contains("does not exist"));
        }
    }
}
