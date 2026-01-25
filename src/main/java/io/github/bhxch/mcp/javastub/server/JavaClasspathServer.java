package io.github.bhxch.mcp.javastub.server;

import io.github.bhxch.mcp.javastub.config.ServerConfig;
import io.github.bhxch.mcp.javastub.decompiler.DecompilerFactory;
import io.github.bhxch.mcp.javastub.inspector.ClassInspector;
import io.github.bhxch.mcp.javastub.maven.resolver.MavenResolverFactory;
import io.github.bhxch.mcp.javastub.server.handlers.InspectJavaClassHandler;
import io.github.bhxch.mcp.javastub.server.handlers.ListModuleDependenciesHandler;
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
        ClassInspector inspector = new ClassInspector(
            DecompilerFactory.createDecompiler(config.getDecompilerConfig())
        );
        MavenResolverFactory resolverFactory = new MavenResolverFactory(config);

        // Build the server
        InspectJavaClassHandler inspectHandler = new InspectJavaClassHandler(inspector, resolverFactory);
        ListModuleDependenciesHandler listDepsHandler = new ListModuleDependenciesHandler(resolverFactory);
        
        this.mcpServer = McpServer.sync(transportProvider)
            .serverInfo(new McpSchema.Implementation("javastub-mcp-server", "1.0.0"))
            .capabilities(McpSchema.ServerCapabilities.builder()
                .tools(true)
                .build())
            .instructions("This server provides tools for inspecting Java classes and listing Maven module dependencies. Use 'inspect_java_class' to inspect a Java class and 'list_module_dependencies' to list Maven dependencies.")
            .toolCall(createInspectJavaClassTool(), (exchange, request) -> inspectHandler.handle(exchange, request))
            .toolCall(createListModuleDependenciesTool(), (exchange, request) -> listDepsHandler.handle(exchange, request))
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
        properties.put("sourceFilePath", Map.of(
            "type", "string",
            "description", "Path to source file (optional)"
        ));
        properties.put("detailLevel", Map.of(
            "type", "string",
            "description", "Level of detail",
            "enum", List.of("skeleton", "basic", "full")
        ));
        
        return McpSchema.Tool.builder()
            .name("inspect_java_class")
            .description("Inspect a Java class and return its metadata")
            .inputSchema(new McpSchema.JsonSchema(
                "object",
                properties,
                List.of("className"),
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
        properties.put("sourceFilePath", Map.of(
            "type", "string",
            "description", "Path to source file in the module"
        ));
        properties.put("pomFilePath", Map.of(
            "type", "string",
            "description", "Path to pom.xml file"
        ));
        properties.put("scope", Map.of(
            "type", "string",
            "description", "Dependency scope",
            "enum", List.of("compile", "provided", "runtime", "test", "system")
        ));
        
        return McpSchema.Tool.builder()
            .name("list_module_dependencies")
            .description("List dependencies of a Maven module")
            .inputSchema(new McpSchema.JsonSchema(
                "object",
                properties,
                List.of(),
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
    }

    /**
     * Get the MCP server instance
     */
    public McpSyncServer getMcpServer() {
        return mcpServer;
    }
}