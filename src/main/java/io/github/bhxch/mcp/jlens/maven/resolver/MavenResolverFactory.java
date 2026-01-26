package io.github.bhxch.mcp.jlens.maven.resolver;

import io.github.bhxch.mcp.jlens.config.ServerConfig;

/**
 * Factory for creating Maven resolvers
 */
public class MavenResolverFactory {

    private final ServerConfig config;

    public MavenResolverFactory(ServerConfig config) {
        this.config = config;
    }

    /**
     * Create a Maven resolver based on configuration
     */
    public MavenResolver createResolver() {
        MavenResolver resolver = new MavenDirectResolver();

        if (config.getMavenConfig().getExecutable() != null) {
            resolver = new MavenInvokerResolver(config.getMavenConfig());
        }

        return resolver;
    }

    /**
     * Create a direct resolver (always available)
     */
    public MavenResolver createDirectResolver() {
        return new MavenDirectResolver();
    }
}



