package io.github.bhxch.mcp.jlens.decompiler;

import java.nio.file.Path;

/**
 * Adapter for decompilers
 */
public interface DecompilerAdapter {

    /**
     * Decompile a class file
     */
    String decompile(Path classFile);

    /**
     * Get the name of the decompiler
     */
    String getName();

    /**
     * Check if the decompiler is available
     */
    boolean isAvailable();
}



