package io.github.bhxch.mcp.jlens.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.bhxch.mcp.jlens.maven.model.DependencyInfo;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import io.github.bhxch.mcp.jlens.maven.model.Scope;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolver;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolverFactory;
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
            List<String> profiles = List.of();
            
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

                if (args.containsKey("profiles")) {
                    Object value = args.get("profiles");
                    if (value instanceof List) {
                        profiles = (List<String>) value;
                    } else if (value != null) {
                        profiles = List.of(value.toString());
                    }
                }
            }

            // Validate required parameters
            if (pomFilePath == null || pomFilePath.isEmpty()) {
                return errorResult("INVALID_ARGUMENTS", "Error: pomFilePath is required");
            }

            // Determine the pom.xml file
            Path pomFile = Paths.get(pomFilePath);

            if (!Files.exists(pomFile)) {
                return errorResult("FILE_NOT_FOUND", "Error: pom.xml file not found at " + pomFilePath);
            }

            // Parse scope
            Scope scope = parseScope(scopeStr);

            // Resolve module
            MavenResolver resolver = resolverFactory.createResolver();
            ModuleContext moduleContext = resolver.resolveModule(pomFile, scope, profiles);

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
            return errorResult("INTERNAL_ERROR", "Error: " + e.getMessage());
        }
    }

    /**
     * Helper to create an error result
     */
    private CallToolResult errorResult(String code, String message) {
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("code", code);
        errorNode.put("message", message);
        
        return CallToolResult.builder()
            .content(List.of(new TextContent(errorNode.toPrettyString())))
            .isError(true)
            .build();
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
}




