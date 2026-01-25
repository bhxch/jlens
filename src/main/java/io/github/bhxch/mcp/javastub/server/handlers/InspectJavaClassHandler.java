package io.github.bhxch.mcp.javastub.server.handlers;

import io.github.bhxch.mcp.javastub.inspector.ClassInspector;
import io.github.bhxch.mcp.javastub.inspector.model.ClassMetadata;
import io.github.bhxch.mcp.javastub.maven.model.ModuleContext;
import io.github.bhxch.mcp.javastub.maven.model.Scope;
import io.github.bhxch.mcp.javastub.maven.resolver.MavenResolver;
import io.github.bhxch.mcp.javastub.maven.resolver.MavenResolverFactory;
import io.github.bhxch.mcp.javastub.concurrent.ParallelProcessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Handler for inspect_java_class tool
 */
public class InspectJavaClassHandler {

    private static final Logger logger = LoggerFactory.getLogger(InspectJavaClassHandler.class);

    private final ClassInspector inspector;
    private final MavenResolverFactory resolverFactory;

    public InspectJavaClassHandler(ClassInspector inspector, MavenResolverFactory resolverFactory) {
        this.inspector = inspector;
        this.resolverFactory = resolverFactory;
    }

    /**
     * Handle the inspect_java_class tool call
     */
    public CallToolResult handle(McpSyncServerExchange exchange, CallToolRequest request) {
        try {
            // Extract parameters
            String className = null;
            String detailLevelStr = "basic";
            String sourceFilePath = null;
            
            if (request.arguments() != null) {
                var args = request.arguments();
                if (args.containsKey("className")) {
                    Object value = args.get("className");
                    if (value != null) {
                        className = value.toString();
                    }
                }
                
                if (args.containsKey("detailLevel")) {
                    Object value = args.get("detailLevel");
                    if (value != null) {
                        detailLevelStr = value.toString();
                    }
                }
                
                if (args.containsKey("sourceFilePath")) {
                    Object value = args.get("sourceFilePath");
                    if (value != null) {
                        sourceFilePath = value.toString();
                    }
                }
            }

            // Validate required parameters
            if (className == null || className.isEmpty()) {
                return CallToolResult.builder()
                    .content(List.of(new TextContent("Error: className is required")))
                    .isError(true)
                    .build();
            }

            // Parse detail level
            ParallelProcessor.DetailLevel detailLevel = parseDetailLevel(detailLevelStr);

            // Resolve module context if source file is provided
            ModuleContext context = null;
            if (sourceFilePath != null && !sourceFilePath.isEmpty()) {
                Path path = Paths.get(sourceFilePath);
                if (Files.exists(path)) {
                    Path pomFile = findPomFile(path);
                    if (pomFile != null && Files.exists(pomFile)) {
                        MavenResolver resolver = resolverFactory.createResolver();
                        context = resolver.resolveModule(pomFile, Scope.COMPILE, List.of());
                    }
                }
            }

            // Inspect the class
            ClassMetadata metadata = inspector.inspect(className, context, detailLevel, null);

            // Return the result
            return CallToolResult.builder()
                .content(List.of(new TextContent(metadata.toString())))
                .isError(false)
                .build();

        } catch (Exception e) {
            logger.error("Error inspecting class", e);
            return CallToolResult.builder()
                .content(List.of(new TextContent("Error: " + e.getMessage())))
                .isError(true)
                .build();
        }
    }

    /**
     * Parse detail level string to enum
     */
    private ParallelProcessor.DetailLevel parseDetailLevel(String detailLevelStr) {
        try {
            return ParallelProcessor.DetailLevel.valueOf(detailLevelStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ParallelProcessor.DetailLevel.BASIC;
        }
    }

    /**
     * Find the pom.xml file for the given source file
     */
    private Path findPomFile(Path sourceFile) {
        Path current = sourceFile;
        while (current != null) {
            Path pomFile = current.resolve("pom.xml");
            if (Files.exists(pomFile)) {
                return pomFile;
            }
            current = current.getParent();
            if (current == null || current.toString().length() < 3) {
                break;
            }
        }
        return null;
    }
}