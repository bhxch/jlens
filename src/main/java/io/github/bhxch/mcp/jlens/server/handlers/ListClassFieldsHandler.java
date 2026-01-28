package io.github.bhxch.mcp.jlens.server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.bhxch.mcp.jlens.inspector.ClassInspector;
import io.github.bhxch.mcp.jlens.inspector.model.ClassMetadata;
import io.github.bhxch.mcp.jlens.inspector.model.FieldInfo;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import io.github.bhxch.mcp.jlens.maven.model.Scope;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolver;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolverFactory;
import io.github.bhxch.mcp.jlens.concurrent.ParallelProcessor;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler for list_class_fields tool
 */
public class ListClassFieldsHandler {

    private static final Logger logger = LoggerFactory.getLogger(ListClassFieldsHandler.class);

    private final ClassInspector inspector;
    private final MavenResolverFactory resolverFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ListClassFieldsHandler(ClassInspector inspector, MavenResolverFactory resolverFactory) {
        this.inspector = inspector;
        this.resolverFactory = resolverFactory;
    }

    /**
     * Handle the list_class_fields tool call
     */
    public CallToolResult handle(McpSyncServerExchange exchange, CallToolRequest request) {
        try {
            // Extract parameters
            String className = null;
            List<String> visibility = new ArrayList<>();
            String sourceFilePath = null;
            String pomFilePath = null;
            List<String> profiles = List.of();
            
            if (request.arguments() != null) {
                var args = request.arguments();
                if (args.containsKey("className")) {
                    Object value = args.get("className");
                    if (value != null) {
                        className = value.toString();
                    }
                }
                
                if (args.containsKey("visibility")) {
                    Object value = args.get("visibility");
                    if (value instanceof List) {
                        for (Object item : (List<?>) value) {
                            visibility.add(item.toString());
                        }
                    }
                }
                
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
            }

            // Validate required parameters
            if (className == null || className.isEmpty()) {
                return errorResult("INVALID_ARGUMENTS", "Error: className is required");
            }
            if (pomFilePath == null || pomFilePath.isEmpty()) {
                return errorResult("INVALID_ARGUMENTS", "Error: pomFilePath is required");
            }

            // Resolve module context
            ModuleContext context = null;
            Path pomFile = Paths.get(pomFilePath);
            if (Files.exists(pomFile)) {
                MavenResolver resolver = resolverFactory.createResolver();
                context = resolver.resolveModule(pomFile, Scope.COMPILE, profiles);
            } else {
                return errorResult("NOT_FOUND", "Error: pom.xml not found at " + pomFilePath);
            }

            // Inspect the class with FULL level to get all fields for filtering
            ClassMetadata metadata = inspector.inspect(className, context, ParallelProcessor.DetailLevel.FULL, null);

            // Check if the class exists
            if (metadata == null || metadata.getClassName() == null || (metadata.getFields().isEmpty() && !isClassExists(className))) {
                return errorResult("CLASS_NOT_FOUND", "Error: Class '" + className + "' not found or could not be inspected");
            }

            // Filter fields by visibility
            List<FieldInfo> filteredFields = metadata.getFields().stream()
                .filter(field -> matchesVisibility(field.getModifiers(), visibility))
                .collect(Collectors.toList());

            // Return the result as JSON
            return CallToolResult.builder()
                .content(List.of(new TextContent(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(filteredFields))))
                .isError(false)
                .build();

        } catch (Exception e) {
            logger.error("Error listing class fields", e);
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

    private boolean matchesVisibility(int modifiers, List<String> visibility) {
        if (visibility == null || visibility.isEmpty()) {
            return true;
        }
        
        for (String v : visibility) {
            switch (v.toLowerCase()) {
                case "public":
                    if (Modifier.isPublic(modifiers)) return true;
                    break;
                case "private":
                    if (Modifier.isPrivate(modifiers)) return true;
                    break;
                case "protected":
                    if (Modifier.isProtected(modifiers)) return true;
                    break;
                case "package-private":
                case "default":
                    if (!Modifier.isPublic(modifiers) && !Modifier.isPrivate(modifiers) && !Modifier.isProtected(modifiers)) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    private boolean isClassExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Throwable t) {
            return true;
        }
    }
}
