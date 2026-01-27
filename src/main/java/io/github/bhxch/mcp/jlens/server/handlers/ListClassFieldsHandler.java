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
            String mavenProfile = null;
            
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

                if (args.containsKey("mavenProfile")) {
                    Object value = args.get("mavenProfile");
                    if (value != null) {
                        mavenProfile = value.toString();
                    }
                }
            }

            // Validate required parameters
            if (className == null || className.isEmpty()) {
                ObjectNode errorNode = objectMapper.createObjectNode();
                errorNode.put("code", "INVALID_ARGUMENTS");
                errorNode.put("message", "Error: className is required");
                
                return CallToolResult.builder()
                    .content(List.of(new TextContent(errorNode.toPrettyString())))
                    .isError(true)
                    .build();
            }

            // Resolve module context if source file is provided
            ModuleContext context = null;
            List<String> activeProfiles = mavenProfile != null && !mavenProfile.isEmpty() ? List.of(mavenProfile) : List.of();

            if (pomFilePath != null && !pomFilePath.isEmpty()) {
                Path pomFile = Paths.get(pomFilePath);
                if (Files.exists(pomFile)) {
                    MavenResolver resolver = resolverFactory.createResolver();
                    context = resolver.resolveModule(pomFile, Scope.COMPILE, activeProfiles);
                }
            } else if (sourceFilePath != null && !sourceFilePath.isEmpty()) {
                Path path = Paths.get(sourceFilePath);
                if (Files.exists(path)) {
                    Path pomFile = findPomFile(path);
                    if (pomFile != null && Files.exists(pomFile)) {
                        MavenResolver resolver = resolverFactory.createResolver();
                        context = resolver.resolveModule(pomFile, Scope.COMPILE, activeProfiles);
                    }
                }
            }

            // Inspect the class with BASIC level to get fields
            ClassMetadata metadata = inspector.inspect(className, context, ParallelProcessor.DetailLevel.BASIC, null);

            // Check if the class exists
            if (metadata == null || metadata.getClassName() == null || (metadata.getFields().isEmpty() && !isClassExists(className))) {
                ObjectNode errorNode = objectMapper.createObjectNode();
                errorNode.put("code", "CLASS_NOT_FOUND");
                errorNode.put("message", "Error: Class '" + className + "' not found or could not be inspected");
                
                return CallToolResult.builder()
                    .content(List.of(new TextContent(errorNode.toPrettyString())))
                    .isError(true)
                    .build();
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
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("code", "INTERNAL_ERROR");
            errorNode.put("message", "Error: " + e.getMessage());
            
            return CallToolResult.builder()
                .content(List.of(new TextContent(errorNode.toPrettyString())))
                .isError(true)
                .build();
        }
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
