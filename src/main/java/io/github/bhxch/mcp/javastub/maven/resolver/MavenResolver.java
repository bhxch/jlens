package io.github.bhxch.mcp.javastub.maven.resolver;

import io.github.bhxch.mcp.javastub.maven.model.ModuleContext;
import io.github.bhxch.mcp.javastub.maven.model.Scope;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for Maven dependency resolution
 */
public interface MavenResolver {

    /**
     * Resolve a Maven module from its pom.xml file
     */
    ModuleContext resolveModule(Path pomFile, Scope scope, List<String> excludes);

    /**
     * Check if this resolver is available
     */
    boolean isAvailable();

    /**
     * Get the name of this resolver
     */
    String getName();
}