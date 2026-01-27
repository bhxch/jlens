package io.github.bhxch.mcp.jlens.server.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.bhxch.mcp.jlens.classpath.PackageMappingResolver;
import io.github.bhxch.mcp.jlens.dependency.DependencyManager;
import io.github.bhxch.mcp.jlens.intelligence.BuildPromptGenerator;
import io.github.bhxch.mcp.jlens.maven.model.DependencyInfo;
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
import java.util.Base64;
import java.util.Comparator;
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
            String pomFilePath = null;
            String mavenProfile = null;
            String searchType = "wildcard";
            int limit = 50;
            String cursor = null;
            
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

                if (args.containsKey("cursor")) {
                    Object value = args.get("cursor");
                    if (value != null) {
                        cursor = value.toString();
                    }
                }
            }

            // Validate required parameters
            if (classNamePattern == null || classNamePattern.isEmpty()) {
                ObjectNode errorNode = objectMapper.createObjectNode();
                errorNode.put("code", "INVALID_ARGUMENTS");
                errorNode.put("message", "Error: classNamePattern is required");
                
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

            // Build class index if not already built
            if (context != null) {
                packageResolver.buildClassIndex(context);
            } else {
                // If no context provided, build index from system classpath
                buildDefaultClassIndex();
            }

            // Search for classes
            SearchResultContainer searchResults = searchClasses(classNamePattern, searchType, limit, cursor);

            // Check for missing dependencies
            List<DependencyInfo> missingDeps = List.of();
            if (context != null) {
                missingDeps = dependencyManager.findMissingDependencies(context);
            }

            // Build response
            return buildSearchResponse(searchResults, missingDeps, context, classNamePattern);

        } catch (Exception e) {
            logger.error("Error searching for classes", e);
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
     * Search for classes based on pattern with pagination
     */
    private SearchResultContainer searchClasses(String pattern, String searchType, int limit, String cursor) {
        Pattern regexPattern = convertToRegex(pattern, searchType);
        List<ClassSearchResult> allMatches = new ArrayList<>();

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
                    
                    allMatches.add(result);
                }
            }
        }

        // Sort results for stable pagination
        allMatches.sort(Comparator.comparing(ClassSearchResult::getClassName));

        int startIndex = 0;
        if (cursor != null && !cursor.isEmpty()) {
            try {
                String decodedCursor = new String(Base64.getDecoder().decode(cursor));
                JsonNode cursorNode = objectMapper.readTree(decodedCursor);
                String lastClassName = cursorNode.get("lastClassName").asText();
                
                for (int i = 0; i < allMatches.size(); i++) {
                    if (allMatches.get(i).getClassName().equals(lastClassName)) {
                        startIndex = i + 1;
                        break;
                    }
                }
            } catch (Exception e) {
                logger.warn("Invalid cursor: {}", cursor);
            }
        }

        int endIndex = Math.min(startIndex + limit, allMatches.size());
        List<ClassSearchResult> pagedResults = allMatches.subList(startIndex, endIndex);
        
        String nextCursor = null;
        if (endIndex < allMatches.size()) {
            ObjectNode nextCursorNode = objectMapper.createObjectNode();
            nextCursorNode.put("lastClassName", pagedResults.get(pagedResults.size() - 1).getClassName());
            nextCursor = Base64.getEncoder().encodeToString(nextCursorNode.toString().getBytes());
        }

        return new SearchResultContainer(pagedResults, allMatches.size(), nextCursor);
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
    private CallToolResult buildSearchResponse(SearchResultContainer container, 
                                               List<DependencyInfo> missingDeps,
                                               ModuleContext context,
                                               String pattern) {
        ObjectNode response = objectMapper.createObjectNode();

        // Results array
        ArrayNode resultsArray = objectMapper.createArrayNode();
        for (ClassSearchResult result : container.results) {
            ObjectNode resultNode = objectMapper.createObjectNode();
            resultNode.put("className", result.getClassName());
            resultNode.put("simpleName", result.getSimpleClassName());
            resultNode.put("package", result.getPackageName());
            resultNode.put("dependency", result.getDependency());
            resultNode.put("inClasspath", result.isInClasspath());
            resultsArray.add(resultNode);
        }

        response.set("results", resultsArray);
        response.put("totalResults", container.totalCount);
        if (container.nextCursor != null) {
            response.put("nextCursor", container.nextCursor);
            response.put("hasMore", true);
        } else {
            response.put("hasMore", false);
        }

        // Add suggestions if dependencies are missing
        if (!missingDeps.isEmpty()) {
            BuildPromptGenerator generator = new BuildPromptGenerator();
            String suggestion = generator.generateBuildSuggestion(
                container.results.isEmpty() ? pattern : container.results.get(0).getSimpleClassName(),
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
     * Container for search results and pagination info
     */
    private static class SearchResultContainer {
        final List<ClassSearchResult> results;
        final int totalCount;
        final String nextCursor;

        SearchResultContainer(List<ClassSearchResult> results, int totalCount, String nextCursor) {
            this.results = results;
            this.totalCount = totalCount;
            this.nextCursor = nextCursor;
        }
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




