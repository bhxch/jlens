package io.github.bhxch.mcp.javastub.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.bhxch.mcp.javastub.maven.model.DependencyInfo;
import io.github.bhxch.mcp.javastub.maven.model.ModuleContext;
import io.github.bhxch.mcp.javastub.maven.model.Scope;
import io.github.bhxch.mcp.javastub.maven.resolver.MavenResolver;
import io.github.bhxch.mcp.javastub.maven.resolver.MavenResolverFactory;
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
import java.util.stream.Collectors;

/**
 * Handler for list_module_dependencies tool
 */
public class ListModuleDependenciesHandler {

    private static final Logger logger = LoggerFactory.getLogger(ListModuleDependenciesHandler.class);

    private final MavenResolverFactory resolverFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ListModuleDependenciesHandler(MavenResolverFactory resolverFactory) {
        this.resolverFactory = resolverFactory;
    }

    /**
     * Handle the list_module_dependencies tool call
     */
    public CallToolResult handle(McpSyncServerExchange exchange, CallToolRequest request) {
        try {
            // Extract parameters
            String sourceFilePath = null;
            String pomFilePath = null;
            String scopeStr = "compile";
            
            if (request.arguments() != null) {
                var args = request.arguments();
                if (args.containsKey("sourceFilePath")) {
                    Object value = args.get("sourceFilePath");
                    if (value != null) {
                        sourceFilePath = value.toString();
                    }
                }
                
                if (args.containsKey("pomFilePath")) {
                    Object value = args.get("pomFilePath");
                    if (value != null) {
                        pomFilePath = value.toString();
                    }
                }
                
                if (args.containsKey("scope")) {
                    Object value = args.get("scope");
                    if (value != null) {
                        scopeStr = value.toString();
                    }
                }
            }

            // Validate that at least one path is provided
            if ((sourceFilePath == null || sourceFilePath.isEmpty()) && 
                (pomFilePath == null || pomFilePath.isEmpty())) {
                ObjectNode errorNode = objectMapper.createObjectNode();
                errorNode.put("code", "INVALID_ARGUMENTS");
                errorNode.put("message", "Error: Either sourceFilePath or pomFilePath must be provided");
                
                return CallToolResult.builder()
                    .content(List.of(new TextContent(errorNode.toPrettyString())))
                    .isError(true)
                    .build();
            }

            // Determine the pom.xml file
            Path pomFile = null;
            if (pomFilePath != null && !pomFilePath.isEmpty()) {
                pomFile = Paths.get(pomFilePath);
            } else if (sourceFilePath != null && !sourceFilePath.isEmpty()) {
                pomFile = findPomFile(Paths.get(sourceFilePath));
            }

            if (pomFile == null || !Files.exists(pomFile)) {
                ObjectNode errorNode = objectMapper.createObjectNode();
                errorNode.put("code", "FILE_NOT_FOUND");
                errorNode.put("message", "Error: pom.xml file not found");
                errorNode.put("suggestion", "Please provide a valid path to a pom.xml file or a source file in the module.");
                
                return CallToolResult.builder()
                    .content(List.of(new TextContent(errorNode.toPrettyString())))
                    .isError(true)
                    .build();
            }

            // Parse scope
            Scope scope = parseScope(scopeStr);

            // Resolve module
            MavenResolver resolver = resolverFactory.createResolver();
            ModuleContext moduleContext = resolver.resolveModule(pomFile, scope, List.of());

            if (moduleContext == null) {
                ObjectNode errorNode = objectMapper.createObjectNode();
                errorNode.put("code", "RESOLUTION_FAILED");
                errorNode.put("message", "Error: Failed to resolve module");
                
                return CallToolResult.builder()
                    .content(List.of(new TextContent(errorNode.toPrettyString())))
                    .isError(true)
                    .build();
            }

            // Build JSON response
            ObjectNode response = objectMapper.createObjectNode();
            
            ObjectNode moduleNode = objectMapper.createObjectNode();
            moduleNode.put("groupId", moduleContext.getGroupId());
            moduleNode.put("artifactId", moduleContext.getArtifactId());
            moduleNode.put("version", moduleContext.getVersion());
            response.set("module", moduleNode);
            
            ArrayNode depsArray = objectMapper.createArrayNode();
            for (DependencyInfo dep : moduleContext.getDependencies()) {
                ObjectNode depNode = objectMapper.createObjectNode();
                depNode.put("groupId", dep.getGroupId());
                depNode.put("artifactId", dep.getArtifactId());
                depNode.put("version", dep.getVersion());
                depNode.put("scope", dep.getScope().toString().toLowerCase());
                depNode.put("type", dep.getType());
                depsArray.add(depNode);
            }
            response.set("dependencies", depsArray);
            response.put("totalDependencies", moduleContext.getDependencies().size());

            // Return the result
            return CallToolResult.builder()
                .content(List.of(new TextContent(response.toPrettyString())))
                .isError(false)
                .build();

        } catch (Exception e) {
            logger.error("Error listing dependencies", e);
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("code", "INTERNAL_ERROR");
            errorNode.put("message", "Error: " + e.getMessage());
            
            return CallToolResult.builder()
                .content(List.of(new TextContent(errorNode.toPrettyString())))
                .isError(true)
                .build();
        }
    }

    /**
     * Parse scope string to enum
     */
    private Scope parseScope(String scopeStr) {
        try {
            return Scope.valueOf(scopeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Scope.COMPILE;
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
