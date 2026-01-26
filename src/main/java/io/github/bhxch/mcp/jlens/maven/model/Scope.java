package io.github.bhxch.mcp.jlens.maven.model;

/**
 * Maven dependency scope
 */
public enum Scope {
    COMPILE,
    PROVIDED,
    RUNTIME,
    TEST,
    SYSTEM,
    IMPORT;

    public static Scope fromString(String scope) {
        if (scope == null) {
            return COMPILE;
        }
        try {
            return Scope.valueOf(scope.toUpperCase());
        } catch (IllegalArgumentException e) {
            return COMPILE;
        }
    }
}



