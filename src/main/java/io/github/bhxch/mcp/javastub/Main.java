package io.github.bhxch.mcp.javastub;

import io.github.bhxch.mcp.javastub.config.ServerConfig;
import io.github.bhxch.mcp.javastub.server.JavaClasspathServer;

/**
 * Main entry point for the Java Maven Classpath MCP Server
 */
public class Main {

    public static void main(String[] args) {
        ServerConfig config = ServerConfig.fromCommandLine(args);
        JavaClasspathServer server = new JavaClasspathServer(config);
        server.start();
    }
}