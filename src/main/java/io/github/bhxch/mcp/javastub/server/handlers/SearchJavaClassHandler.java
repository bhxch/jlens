package io.github.bhxch.mcp.javastub.server.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.bhxch.mcp.javastub.classpath.PackageMappingResolver;
import io.github.bhxch.mcp.javastub.dependency.DependencyManager;
import io.github.bhxch.mcp.javastub.intelligence.BuildPromptGenerator;
import io.github.bhxch.mcp.javastub.maven.model.DependencyInfo;
import io.github.bhxch.mcp.javastub.maven.model.ModuleContext;
import io.github.bhxch.mcp.javastub.maven.model.Scope;
import io.github.bhxch.mcp.javastub.maven.resolver.MavenResolver;
import io.github.bhxch.mcp.javastub.maven.resolver.MavenResolverFactory;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handler for search_java_class tool
 */
public class SearchJavaClassHandler {

    private static final Logger logger = LoggerFactory.getLogger(SearchJavaClassHandler.class);

    private final PackageMappingResolver packageResolver;
    private final DependencyManager dependencyManager;
    private final MavenResolverFactory resolverFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SearchJavaClassHandler(PackageMappingResolver packageResolver,
                                  DependencyManager dependencyManager,
                                  MavenResolverFactory resolverFactory) {
        this.packageResolver = packageResolver;
        this.dependencyManager = dependencyManager;
        this.resolverFactory = resolverFactory;
    }

    /**
     * Handle the search_java_class tool call
     */
    public CallToolResult handle(McpSyncServerExchange exchange, CallToolRequest request) {
        try {
            // Extract parameters
            String classNamePattern = null;
            String sourceFilePath = null;
            String searchType = "wildcard";
            int limit = 50;
            
            if (request.arguments() != null) {
                var args = request.arguments();
                if (args.containsKey("classNamePattern")) {
                    Object value = args.get("classNamePattern");
                    if (value != null) {
                        classNamePattern = value.toString();
                    }
                }
                
                if (args.containsKey("sourceFilePath")) {
                    Object value = args.get("sourceFilePath");
                    if (value != null) {
                        sourceFilePath = value.toString();
                    }
                }
                
                if (args.containsKey("searchType")) {
                    Object value = args.get("searchType");
                    if (value != null) {
                        searchType = value.toString();
                    }
                }
                
                if (args.containsKey("limit")) {
                    Object value = args.get("limit");
                    if (value != null) {
                        try {
                            limit = Integer.parseInt(value.toString());
                        } catch (NumberFormatException e) {
                            // Use default
                        }
                    }
                }
            }

            // Validate required parameters
            if (classNamePattern == null || classNamePattern.isEmpty()) {
                return CallToolResult.builder()
                    .content(List.of(new TextContent("Error: classNamePattern is required")))
                    .isError(true)
                    .build();
            }

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

            // Build class index if not already built
            if (context != null) {
                packageResolver.buildClassIndex(context);
            } else {
                // If no context provided, build index from system classpath
                buildDefaultClassIndex();
            }

            // Search for classes
            List<ClassSearchResult> results = searchClasses(classNamePattern, searchType, limit);

            // Check for missing dependencies
            List<DependencyInfo> missingDeps = List.of();
            if (context != null) {
                missingDeps = dependencyManager.findMissingDependencies(context);
            }

            // Build response
            return buildSearchResponse(results, missingDeps, context, classNamePattern);

        } catch (Exception e) {
            logger.error("Error searching for classes", e);
            return CallToolResult.builder()
                .content(List.of(new TextContent("Error: " + e.getMessage())))
                .isError(true)
                .build();
        }
    }

    /**
     * Search for classes based on pattern
     */
    private List<ClassSearchResult> searchClasses(String pattern, String searchType, int limit) {
        Pattern regexPattern = convertToRegex(pattern, searchType);
        List<ClassSearchResult> results = new java.util.ArrayList<>();

        // Search in indexed classes
        for (var entry : packageResolver.getClassToPackages().entrySet()) {
            String className = entry.getKey();
            if (regexPattern.matcher(className).matches()) {
                for (String packageName : entry.getValue()) {
                    ClassSearchResult result = new ClassSearchResult();
                    result.setClassName(packageName + "." + className);
                    result.setSimpleClassName(className);
                    result.setPackageName(packageName);
                    result.setDependency(packageResolver.getDependencyForPackage(packageName));
                    result.setInClasspath(true);
                    
                    results.add(result);
                    
                    if (results.size() >= limit) {
                        break;
                    }
                }
            }

            if (results.size() >= limit) {
                break;
            }
        }

        return results;
    }

    /**
     * Convert search pattern to regex
     */
    private Pattern convertToRegex(String pattern, String searchType) {
        String regex;
        
        switch (searchType.toLowerCase()) {
            case "exact":
                regex = Pattern.quote(pattern);
                break;
            case "prefix":
                regex = Pattern.quote(pattern) + ".*";
                break;
            case "suffix":
                regex = ".*" + Pattern.quote(pattern);
                break;
            case "contains":
                regex = ".*" + Pattern.quote(pattern) + ".*";
                break;
            case "wildcard":
            default:
                // Convert * and ? to regex
                regex = pattern.replace(".", "\\.")
                              .replace("*", ".*")
                              .replace("?", ".");
                break;
        }
        
        return Pattern.compile(regex);
    }

    /**
     * Build search response
     */
    private CallToolResult buildSearchResponse(List<ClassSearchResult> results, 
                                               List<DependencyInfo> missingDeps,
                                               ModuleContext context,
                                               String pattern) {
        ObjectNode response = objectMapper.createObjectNode();

        // Results array
        ArrayNode resultsArray = objectMapper.createArrayNode();
        for (ClassSearchResult result : results) {
            ObjectNode resultNode = objectMapper.createObjectNode();
            resultNode.put("className", result.getClassName());
            resultNode.put("simpleName", result.getSimpleClassName());
            resultNode.put("package", result.getPackageName());
            resultNode.put("dependency", result.getDependency());
            resultNode.put("inClasspath", result.isInClasspath());
            resultsArray.add(resultNode);
        }

        response.set("results", resultsArray);
        response.put("totalResults", results.size());

        // Add suggestions if dependencies are missing
        if (!missingDeps.isEmpty()) {
            BuildPromptGenerator generator = new BuildPromptGenerator();
            String suggestion = generator.generateBuildSuggestion(
                results.isEmpty() ? pattern : results.get(0).getSimpleClassName(),
                context,
                missingDeps
            );

            response.put("suggestion", suggestion);
            response.put("hasMissingDependencies", true);

            ArrayNode missingArray = objectMapper.createArrayNode();
            for (DependencyInfo dep : missingDeps) {
                missingArray.add(dep.getCoordinates());
            }
            response.set("missingDependencies", missingArray);
        } else {
            response.put("hasMissingDependencies", false);
        }

        return CallToolResult.builder()
            .content(List.of(new TextContent(response.toPrettyString())))
            .isError(false)
            .build();
    }

    /**
     * Build default class index from system classpath
     */
    private void buildDefaultClassIndex() {
        try {
            // Get system classpath
            String classpath = System.getProperty("java.class.path");
            String[] classpathEntries = classpath.split(System.getProperty("path.separator"));

            // Performance optimization: Limit number of JARs to index
            int maxJars = 10; // Only index first 10 JAR files
            int jarsIndexed = 0;

            // Index JAR files from classpath
            for (String entry : classpathEntries) {
                if (entry.endsWith(".jar")) {
                    Path jarPath = Paths.get(entry);
                    if (Files.exists(jarPath)) {
                        indexJarFile(jarPath);
                        jarsIndexed++;
                        
                        // Stop after indexing maxJars JARs
                        if (jarsIndexed >= maxJars) {
                            break;
                        }
                    }
                }
            }

            // Also add common java.lang classes manually
            addCommonJavaLangClasses();

            logger.info("Built default class index from {} JAR files", jarsIndexed);

        } catch (Exception e) {
            logger.warn("Failed to build default class index", e);
        }
    }

    /**
     * Index a single JAR file
     */
    private void indexJarFile(Path jarPath) {
        try {
            java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jarPath.toFile());
            java.util.Enumeration<java.util.jar.JarEntry> entries = jarFile.entries();

            // Performance optimization: Limit number of classes to index per JAR
            int maxClassesPerJar = 1000; // Only index first 1000 classes per JAR
            int classesIndexed = 0;

            while (entries.hasMoreElements() && classesIndexed < maxClassesPerJar) {
                java.util.jar.JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                        .replace('/', '.')
                        .replace(".class", "");

                    int lastDot = className.lastIndexOf('.');
                    if (lastDot > 0) {
                        String packageName = className.substring(0, lastDot);
                        String simpleName = className.substring(lastDot + 1);

                        // Store mapping
                        packageResolver.getClassToPackages()
                            .computeIfAbsent(simpleName, k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                            .add(packageName);
                        
                        classesIndexed++;
                    }
                }
            }
            
            logger.debug("Indexed {} classes from JAR: {}", classesIndexed, jarPath.getFileName());

        } catch (Exception e) {
            logger.warn("Failed to index JAR: " + jarPath, e);
        }
    }

    /**
     * Add common java.lang classes manually
     */
    private void addCommonJavaLangClasses() {
        String javaLangPackage = "java.lang";
        String[] commonClasses = {
            "String", "Integer", "Long", "Double", "Float", "Boolean",
            "Character", "Byte", "Short", "Void", "Object", "Class",
            "System", "Math", "Runtime", "Thread", "Exception",
            "Error", "Throwable", "Runnable", "Comparable", "StringBuilder",
            "StringBuffer", "Number", "Enum", "Iterable", "Collection",
            "List", "Map", "Set", "ArrayList", "HashMap", "HashSet"
        };

        for (String className : commonClasses) {
            packageResolver.getClassToPackages()
                .computeIfAbsent(className, k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(javaLangPackage);
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

    /**
     * Data class for class search results
     */
    public static class ClassSearchResult {
        private String className;
        private String simpleClassName;
        private String packageName;
        private String dependency;
        private boolean inClasspath;

        // Getters and setters
        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getSimpleClassName() {
            return simpleClassName;
        }

        public void setSimpleClassName(String simpleClassName) {
            this.simpleClassName = simpleClassName;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getDependency() {
            return dependency;
        }

        public void setDependency(String dependency) {
            this.dependency = dependency;
        }

        public boolean isInClasspath() {
            return inClasspath;
        }

        public void setInClasspath(boolean inClasspath) {
            this.inClasspath = inClasspath;
        }
    }
}
