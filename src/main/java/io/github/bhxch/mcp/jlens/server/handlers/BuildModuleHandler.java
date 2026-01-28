package io.github.bhxch.mcp.jlens.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.bhxch.mcp.jlens.dependency.DependencyManager;
import io.github.bhxch.mcp.jlens.dependency.MavenBuilder;
import io.github.bhxch.mcp.jlens.intelligence.BuildPromptGenerator;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import io.github.bhxch.mcp.jlens.maven.model.Scope;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolver;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolverFactory;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Handler for build_module tool
 */
public class BuildModuleHandler {

    private static final Logger logger = LoggerFactory.getLogger(BuildModuleHandler.class);

    private final MavenBuilder mavenBuilder;
    private final DependencyManager dependencyManager;
    private final MavenResolverFactory resolverFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BuildModuleHandler(MavenBuilder mavenBuilder,
                              DependencyManager dependencyManager,
                              MavenResolverFactory resolverFactory) {
        this.mavenBuilder = mavenBuilder;
        this.dependencyManager = dependencyManager;
        this.resolverFactory = resolverFactory;
    }

    /**
     * Handle the build_module tool call
     */
    public CallToolResult handle(McpSyncServerExchange exchange, CallToolRequest request) {
        try {
            // Extract parameters
            String sourceFilePath = null;
            String pomFilePath = null;
            List<String> profiles = List.of();
            List<String> goals = List.of("compile", "dependency:resolve");
            boolean downloadSources = false;
            int timeoutSeconds = 300;
            
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

                if (args.containsKey("profiles")) {
                    Object value = args.get("profiles");
                    if (value instanceof List) {
                        profiles = (List<String>) value;
                    } else if (value != null) {
                        profiles = List.of(value.toString());
                    }
                }
                
                if (args.containsKey("goals")) {
                    Object value = args.get("goals");
                    if (value != null) {
                        goals = parseGoals(value);
                    }
                }
                
                if (args.containsKey("downloadSources")) {
                    Object value = args.get("downloadSources");
                    if (value != null) {
                        downloadSources = Boolean.parseBoolean(value.toString());
                    }
                }
                
                if (args.containsKey("timeoutSeconds")) {
                    Object value = args.get("timeoutSeconds");
                    if (value != null) {
                        try {
                            timeoutSeconds = Integer.parseInt(value.toString());
                        } catch (NumberFormatException e) {
                            // Use default
                        }
                    }
                }
            }

            // Validate required parameters
            if (pomFilePath == null || pomFilePath.isEmpty()) {
                return errorResult("INVALID_ARGUMENTS", "Error: pomFilePath is required");
            }

            // Resolve module context
            Path pomFile = Paths.get(pomFilePath);
            if (!Files.exists(pomFile)) {
                return errorResult("FILE_NOT_FOUND", "Error: pom.xml does not exist: " + pomFilePath);
            }

            MavenResolver resolver = resolverFactory.createResolver();
            ModuleContext context = resolver.resolveModule(pomFile, Scope.COMPILE, profiles);

            // Add source download to goals if requested
            List<String> finalGoals = new ArrayList<>(goals);
            if (downloadSources) {
                finalGoals.add("dependency:sources");
            }

            // Execute build
            MavenBuilder.BuildResult result = mavenBuilder.buildModule(context, finalGoals, List.of(), timeoutSeconds);

            // Build response
            return buildBuildResponse(result, context);

        } catch (Exception e) {
            logger.error("Error building module", e);
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
     * Parse goals from parameter
     */
    private List<String> parseGoals(Object goalsValue) {
        List<String> goals = new ArrayList<>();
        
        if (goalsValue instanceof List) {
            for (Object item : (List<?>) goalsValue) {
                if (item != null) {
                    goals.add(item.toString());
                }
            }
        } else if (goalsValue instanceof String) {
            String[] items = goalsValue.toString().split(",");
            for (String item : items) {
                goals.add(item.trim());
            }
        }
        
        return goals;
    }

    /**
     * Build build response
     */
    private CallToolResult buildBuildResponse(MavenBuilder.BuildResult result, ModuleContext context) {
        ObjectNode response = objectMapper.createObjectNode();

        response.put("success", result.isSuccess());
        response.put("exitCode", result.getExitCode());
        response.put("durationSeconds", result.getDurationSeconds());

        // Build output (truncated if too long)
        String output = result.getOutput();
        if (output != null) {
            if (output.length() > 10000) {
                output = output.substring(0, 5000) + 
                        "\n...[truncated]\n" + 
                        output.substring(output.length() - 5000);
            }
            response.put("output", output);
        } else {
            response.put("output", "");
        }

        // Downloaded artifacts
        ArrayNode artifactsArray = objectMapper.createArrayNode();
        for (MavenBuilder.ArtifactInfo artifact : result.getDownloadedArtifacts()) {
            ObjectNode artifactNode = objectMapper.createObjectNode();
            artifactNode.put("coordinates", artifact.getCoordinates());
            artifactNode.put("type", artifact.getType());
            artifactNode.put("sizeBytes", artifact.getSizeBytes());
            if (artifact.getFile() != null) {
                artifactNode.put("file", artifact.getFile().toString());
            }
            artifactsArray.add(artifactNode);
        }
        response.set("downloadedArtifacts", artifactsArray);

        // Suggestions based on build result
        if (!result.isSuccess()) {
            BuildPromptGenerator generator = new BuildPromptGenerator();
            String suggestion = generator.generateBuildSuggestion(
                "unknown",
                context,
                result.getMissingDependencies()
            );
            response.put("suggestion", suggestion);
            
            // Common error patterns
            if (output != null) {
                if (output.contains("Could not resolve dependencies")) {
                    response.put("errorType", "DEPENDENCY_RESOLUTION_FAILED");
                } else if (output.contains("Compilation failure")) {
                    response.put("errorType", "COMPILATION_FAILED");
                } else if (output.contains("Connection refused") || 
                           output.contains("Network is unreachable")) {
                    response.put("errorType", "NETWORK_ERROR");
                    response.put("networkSuggestion", "Check your network connection and Maven repository settings");
                }
            }
            
            if (!mavenBuilder.isMavenAvailable()) {
                response.put("errorType", "MAVEN_NOT_FOUND");
                response.put("mavenSuggestion", 
                    "Maven is not available. Please install Maven or set M2_HOME environment variable");
            }
            
            if (result.getError() != null) {
                response.put("error", result.getError());
            }
        } else {
            response.put("suggestion", 
                "Build completed successfully. You can now inspect classes from the downloaded dependencies.");
        }

        return CallToolResult.builder()
            .content(List.of(new TextContent(response.toPrettyString())))
            .isError(false)
            .build();
    }
}




