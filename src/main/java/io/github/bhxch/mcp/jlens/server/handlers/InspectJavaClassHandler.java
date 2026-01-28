package io.github.bhxch.mcp.jlens.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.bhxch.mcp.jlens.cache.CacheManager;
import io.github.bhxch.mcp.jlens.inspector.ClassInspector;
import io.github.bhxch.mcp.jlens.inspector.model.ClassMetadata;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import io.github.bhxch.mcp.jlens.maven.model.Scope;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolver;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolverFactory;
import io.github.bhxch.mcp.jlens.concurrent.ParallelProcessor;
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
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InspectJavaClassHandler(ClassInspector inspector, 
                                  MavenResolverFactory resolverFactory,
                                  CacheManager cacheManager) {
        this.inspector = inspector;
        this.resolverFactory = resolverFactory;
        this.cacheManager = cacheManager;
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
            String javaHomePath = null;
            String pomFilePath = null;
            List<String> profiles = List.of();
            boolean bypassCache = false;
            
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

                if (args.containsKey("javaHome")) {
                    Object value = args.get("javaHome");
                    if (value != null) {
                        javaHomePath = value.toString();
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

                if (args.containsKey("bypassCache")) {
                    Object value = args.get("bypassCache");
                    if (value instanceof Boolean) {
                        bypassCache = (Boolean) value;
                    } else if (value != null) {
                        bypassCache = Boolean.parseBoolean(value.toString());
                    }
                }
            }

            // Validate required parameters
            if (className == null || className.isEmpty()) {
                return errorResult("INVALID_ARGUMENTS", "Error: className is required");
            }
            if (pomFilePath == null || pomFilePath.isEmpty()) {
                return errorResult("INVALID_ARGUMENTS", "Error: pomFilePath is required");
            }

            // Parse detail level
            ParallelProcessor.DetailLevel detailLevel = parseDetailLevel(detailLevelStr);

            // Resolve module context
            ModuleContext context = null;
            Path pomFile = Paths.get(pomFilePath);
            if (Files.exists(pomFile)) {
                MavenResolver resolver = resolverFactory.createResolver();
                context = resolver.resolveModule(pomFile, Scope.COMPILE, profiles);
            } else {
                return errorResult("NOT_FOUND", "Error: pom.xml not found at " + pomFilePath);
            }

            // Check Cache
            ClassMetadata metadata = null;
            if (!bypassCache) {
                if (context != null) {
                    metadata = cacheManager.getClassMetadataIfPresent("gav:" + context.getCoordinates() + ":" + className);
                } else {
                    metadata = cacheManager.getClassMetadataIfPresent("cp:default:" + className);
                }
            }

            if (metadata == null) {
                // Get ClassLoader
                ClassLoader classLoader = cacheManager.getClassLoaderManager().getClassLoader(context);
                
                // Prepare javaHome path
                Path javaHome = javaHomePath != null ? Paths.get(javaHomePath) : null;

                // Inspect the class
                metadata = inspector.inspect(className, context, detailLevel, null, classLoader, javaHome);
                
                // Put in cache if successful
                if ("SUCCESS".equals(metadata.getStatus())) {
                    if (context != null) {
                        cacheManager.putClassMetadata("gav:" + context.getCoordinates() + ":" + className, metadata);
                    } else {
                        cacheManager.putClassMetadata("cp:default:" + className, metadata);
                    }
                }
            }

            // Return the result as JSON
            return CallToolResult.builder()
                .content(List.of(new TextContent(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(metadata))))
                .isError(false)
                .build();

        } catch (Exception e) {
            logger.error("Error inspecting class", e);
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
     * Parse detail level string to enum

    /**
     * Check if a class exists in the classpath
     */
    private boolean isClassExists(String className, ModuleContext context) {
        try {
            // Try to load the class using Class.forName
            Class<?> clazz = Class.forName(className);
            return clazz != null;
        } catch (ClassNotFoundException e) {
            // Class not found
            return false;
        } catch (NoClassDefFoundError e) {
            // Class found but dependencies missing
            return true;
        } catch (Exception e) {
            // Other errors, assume class doesn't exist
            logger.warn("Error checking class existence: " + className, e);
            return false;
        }
    }
}



