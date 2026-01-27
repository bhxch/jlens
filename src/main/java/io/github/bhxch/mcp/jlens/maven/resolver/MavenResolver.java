package io.github.bhxch.mcp.jlens.maven.resolver;

import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import io.github.bhxch.mcp.jlens.maven.model.Scope;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for Maven dependency resolution
 */
public interface MavenResolver {

    /**
     * Resolve a Maven module from its pom.xml file
     */
    ModuleContext resolveModule(Path pomFile, Scope scope, List<String> activeProfiles);

    /**
     * Check if this resolver is available
     */
    boolean isAvailable();

    /**
     * Get the name of this resolver
     */
    String getName();
}



