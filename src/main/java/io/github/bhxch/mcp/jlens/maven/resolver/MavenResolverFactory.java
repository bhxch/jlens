package io.github.bhxch.mcp.jlens.maven.resolver;

import io.github.bhxch.mcp.jlens.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Factory for creating Maven resolvers.
 * Defaults to MavenInvokerResolver for full Maven resolution support,
 * falls back to MavenDirectResolver if mvn command is unavailable.
 */
public class MavenResolverFactory {

    private static final Logger logger = LoggerFactory.getLogger(MavenResolverFactory.class);
    private static Boolean mvnAvailable = null;

    private final ServerConfig config;

    public MavenResolverFactory(ServerConfig config) {
        this.config = config;
    }

    /**
     * Create a Maven resolver based on configuration.
     * Attempts to use MavenInvokerResolver first (preferred), falls back to MavenDirectResolver.
     */
    public MavenResolver createResolver() {
        // Check if mvn is available (cache result for performance)
        if (mvnAvailable == null) {
            mvnAvailable = checkMvnAvailable();
        }

        // If mvn is available or explicit executable is configured, use MavenInvokerResolver
        if (mvnAvailable || config.getMavenConfig().getExecutable() != null) {
            try {
                logger.info("Using MavenInvokerResolver for full Maven resolution support");
                return new MavenInvokerResolver(config.getMavenConfig());
            } catch (Exception e) {
                logger.warn("MavenInvokerResolver failed to initialize, falling back to MavenDirectResolver", e);
                return new MavenDirectResolver();
            }
        }

        // Fallback to MavenDirectResolver
        logger.info("Using MavenDirectResolver (mvn command not available)");
        return new MavenDirectResolver();
    }

    /**
     * Check if 'mvn' command is available in the system
     */
    private boolean checkMvnAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("mvn", "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                // Read output to prevent blocking
                while (reader.readLine() != null) {
                    // Just consume output
                }
            }

            int exitCode = process.waitFor();
            boolean available = (exitCode == 0);

            if (available) {
                logger.debug("Maven command is available");
            } else {
                logger.debug("Maven command is not available (exit code: {})", exitCode);
            }

            return available;

        } catch (IOException | InterruptedException e) {
            logger.debug("Maven command is not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Create a direct resolver (always available, but with limited features)
     */
    public MavenResolver createDirectResolver() {
        return new MavenDirectResolver();
    }
}



