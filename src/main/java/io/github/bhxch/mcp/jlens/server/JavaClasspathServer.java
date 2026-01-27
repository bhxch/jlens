package io.github.bhxch.mcp.jlens.server;

import io.github.bhxch.mcp.jlens.cache.CacheManager;
import io.github.bhxch.mcp.jlens.classpath.PackageMappingResolver;
import io.github.bhxch.mcp.jlens.config.ServerConfig;
import io.github.bhxch.mcp.jlens.decompiler.DecompilerFactory;
import io.github.bhxch.mcp.jlens.dependency.DependencyManager;
import io.github.bhxch.mcp.jlens.dependency.MavenBuilder;
import io.github.bhxch.mcp.jlens.inspector.ClassInspector;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenResolverFactory;
import io.github.bhxch.mcp.jlens.server.handlers.BuildModuleHandler;
import io.github.bhxch.mcp.jlens.server.handlers.InspectJavaClassHandler;
import io.github.bhxch.mcp.jlens.server.handlers.ListClassFieldsHandler;
import io.github.bhxch.mcp.jlens.server.handlers.ListModuleDependenciesHandler;
import io.github.bhxch.mcp.jlens.server.handlers.SearchJavaClassHandler;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main MCP server implementation using MCP Java SDK 0.17.2
 */
public class JavaClasspathServer {

    private static final Logger logger = LoggerFactory.getLogger(JavaClasspathServer.class);

    private final McpSyncServer mcpServer;
    private final ServerConfig config;

    public JavaClasspathServer(ServerConfig config) {
        this.config = config;

        // Create transport provider for stdin/stdout
        StdioServerTransportProvider transportProvider = new StdioServerTransportProvider(McpJsonMapper.getDefault());

        // Initialize components
        CacheManager cacheManager = new CacheManager(config);
        ClassInspector inspector = new ClassInspector(
            DecompilerFactory.createDecompiler(config.getDecompilerConfig())
        );
        MavenResolverFactory resolverFactory = new MavenResolverFactory(config);
        MavenBuilder mavenBuilder = new MavenBuilder();
        DependencyManager dependencyManager = new DependencyManager(mavenBuilder);
        PackageMappingResolver packageResolver = new PackageMappingResolver();

        // Build the server
        InspectJavaClassHandler inspectHandler = new InspectJavaClassHandler(inspector, resolverFactory, cacheManager);
        ListClassFieldsHandler listFieldsHandler = new ListClassFieldsHandler(inspector, resolverFactory);
        ListModuleDependenciesHandler listDepsHandler = new ListModuleDependenciesHandler(resolverFactory);
        SearchJavaClassHandler searchClassHandler = new SearchJavaClassHandler(packageResolver, dependencyManager, resolverFactory);
        BuildModuleHandler buildModuleHandler = new BuildModuleHandler(mavenBuilder, dependencyManager, resolverFactory);
        
        this.mcpServer = McpServer.sync(transportProvider)
            .serverInfo(new McpSchema.Implementation("jlens-mcp-server", "1.1.2"))
            .capabilities(McpSchema.ServerCapabilities.builder()
                .tools(true)
                .build())
            .instructions("This server provides tools for inspecting Java classes, listing class fields, listing Maven module dependencies, searching for classes, and building Maven modules. Use 'inspect_java_class' to inspect a Java class, 'list_class_fields' to list variables in a class with visibility filtering, 'list_module_dependencies' to list Maven dependencies, 'search_java_class' to search for classes across packages, and 'build_module' to build a Maven module.")
            .toolCall(createInspectJavaClassTool(), (exchange, request) -> inspectHandler.handle(exchange, request))
            .toolCall(createListClassFieldsTool(), (exchange, request) -> listFieldsHandler.handle(exchange, request))
            .toolCall(createListModuleDependenciesTool(), (exchange, request) -> listDepsHandler.handle(exchange, request))
            .toolCall(createSearchJavaClassTool(), (exchange, request) -> searchClassHandler.handle(exchange, request))
            .toolCall(createBuildModuleTool(), (exchange, request) -> buildModuleHandler.handle(exchange, request))
            .build();

        logger.info("MCP Server initialized");
    }

    /**
     * Create the inspect_java_class tool definition
     */
    private McpSchema.Tool createInspectJavaClassTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("className", Map.of(
            "type", "string",
            "description", "Fully qualified class name"
        ));
        properties.put("pomFilePath", Map.of(
            "type", "string",
            "description", "Path to pom.xml file"
        ));
        properties.put("profiles", Map.of(
            "type", "array",
            "description", "Active Maven profiles",
            "items", Map.of("type", "string")
        ));
        properties.put("sourceFilePath", Map.of(
            "type", "string",
            "description", "Path to source file (optional)"
        ));
        properties.put("detailLevel", Map.of(
            "type", "string",
            "description", "Level of detail",
            "enum", List.of("skeleton", "basic", "full")
        ));
        properties.put("bypassCache", Map.of(
            "type", "boolean",
            "description", "Whether to bypass cache and re-inspect",
            "default", false
        ));
        
        return McpSchema.Tool.builder()
            .name("inspect_java_class")
            .description("Inspect a Java class and return its metadata. Requires pomFilePath to identify the module.")
            .inputSchema(new McpSchema.JsonSchema(
                "object",
                properties,
                List.of("className", "pomFilePath"),
                false,
                null,
                null
            ))
            .build();
    }

    /**
     * Create the list_class_fields tool definition
     */
    private McpSchema.Tool createListClassFieldsTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("className", Map.of(
            "type", "string",
            "description", "Fully qualified class name"
        ));
        properties.put("pomFilePath", Map.of(
            "type", "string",
            "description", "Path to pom.xml file"
        ));
        properties.put("profiles", Map.of(
            "type", "array",
            "description", "Active Maven profiles",
            "items", Map.of("type", "string")
        ));
        properties.put("visibility", Map.of(
            "type", "array",
            "description", "Visibility modifiers to include",
            "items", Map.of(
                "type", "string",
                "enum", List.of("public", "protected", "private", "package-private")
            )
        ));
        properties.put("sourceFilePath", Map.of(
            "type", "string",
            "description", "Path to source file in the module (optional)"
        ));

        return McpSchema.Tool.builder()
            .name("list_class_fields")
            .description("List fields of a Java class with visibility filtering. Requires pomFilePath.")
            .inputSchema(new McpSchema.JsonSchema(
                "object",
                properties,
                List.of("className", "pomFilePath"),
                false,
                null,
                null
            ))
            .build();
    }

    /**
     * Create the list_module_dependencies tool definition
     */
    private McpSchema.Tool createListModuleDependenciesTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("pomFilePath", Map.of(
            "type", "string",
            "description", "Path to pom.xml file"
        ));
        properties.put("profiles", Map.of(
            "type", "array",
            "description", "Active Maven profiles",
            "items", Map.of("type", "string")
        ));
        properties.put("sourceFilePath", Map.of(
            "type", "string",
            "description", "Path to source file in the module (optional)"
        ));
        properties.put("scope", Map.of(
            "type", "string",
            "description", "Dependency scope",
            "enum", List.of("compile", "provided", "runtime", "test", "system")
        ));
        
        return McpSchema.Tool.builder()
            .name("list_module_dependencies")
            .description("List dependencies of a Maven module. Requires pomFilePath.")
            .inputSchema(new McpSchema.JsonSchema(
                "object",
                properties,
                List.of("pomFilePath"),
                false,
                null,
                null
            ))
            .build();
    }

    /**
     * Create the search_java_class tool definition
     */
    private McpSchema.Tool createSearchJavaClassTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("classNamePattern", Map.of(
            "type", "string",
            "description", "Class name pattern (supports wildcards: *, ?)"
        ));
        properties.put("pomFilePath", Map.of(
            "type", "string",
            "description", "Path to pom.xml file"
        ));
        properties.put("profiles", Map.of(
            "type", "array",
            "description", "Active Maven profiles",
            "items", Map.of("type", "string")
        ));
        properties.put("sourceFilePath", Map.of(
            "type", "string",
            "description", "Source file path for context (optional)"
        ));
        properties.put("searchType", Map.of(
            "type", "string",
            "description", "Search type: exact, prefix, suffix, contains, wildcard",
            "enum", List.of("exact", "prefix", "suffix", "contains", "wildcard"),
            "default", "wildcard"
        ));
        properties.put("limit", Map.of(
            "type", "integer",
            "description", "Maximum number of results to return per page",
            "default", 50
        ));
        properties.put("cursor", Map.of(
            "type", "string",
            "description", "Pagination cursor from previous request"
        ));
        
        return McpSchema.Tool.builder()
            .name("search_java_class")
            .description("Search for Java classes across packages and dependencies with pagination. Requires pomFilePath.")
            .inputSchema(new McpSchema.JsonSchema(
                "object",
                properties,
                List.of("classNamePattern", "pomFilePath"),
                false,
                null,
                null
            ))
            .build();
    }

    /**
     * Create the build_module tool definition
     */
    private McpSchema.Tool createBuildModuleTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("pomFilePath", Map.of(
            "type", "string",
            "description", "Path to pom.xml file"
        ));
        properties.put("profiles", Map.of(
            "type", "array",
            "description", "Active Maven profiles",
            "items", Map.of("type", "string")
        ));
        properties.put("sourceFilePath", Map.of(
            "type", "string",
            "description", "Source file path for module context (optional)"
        ));
        properties.put("goals", Map.of(
            "type", "array",
            "description", "Maven goals to execute (default: [\"compile\", \"dependency:resolve\"])",
            "items", Map.of("type", "string"),
            "default", List.of("compile", "dependency:resolve")
        ));
        properties.put("downloadSources", Map.of(
            "type", "boolean",
            "description", "Whether to download source JARs",
            "default", false
        ));
        properties.put("timeoutSeconds", Map.of(
            "type", "integer",
            "description", "Build timeout in seconds",
            "default", 300
        ));
        
        return McpSchema.Tool.builder()
            .name("build_module")
            .description("Build Maven module and download missing dependencies. Requires pomFilePath.")
            .inputSchema(new McpSchema.JsonSchema(
                "object",
                properties,
                List.of("pomFilePath"),
                false,
                null,
                null
            ))
            .build();
    }

    /**
     * Start the server
     */
    public void start() {
        logger.info("Starting MCP Server...");
        // The server starts automatically when built
        // It will listen on stdin/stdout for JSON-RPC requests
        // Add shutdown hook for graceful shutdown
        Thread shutdownHook = new Thread(() -> {
            logger.info("Shutting down MCP Server...");
            mcpServer.close();
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    /**
     * Get the MCP server instance
     */
    public McpSyncServer getMcpServer() {
        return mcpServer;
    }
}
