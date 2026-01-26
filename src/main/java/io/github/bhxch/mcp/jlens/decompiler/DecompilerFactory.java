package io.github.bhxch.mcp.jlens.decompiler;

import io.github.bhxch.mcp.jlens.config.DecompilerConfig;
import io.github.bhxch.mcp.jlens.decompiler.impl.CFRDecompiler;
import io.github.bhxch.mcp.jlens.decompiler.impl.FernflowerDecompiler;
import io.github.bhxch.mcp.jlens.decompiler.impl.VineflowerDecompiler;

/**
 * Factory for creating decompilers
 */
public class DecompilerFactory {

    public static DecompilerAdapter createDecompiler(DecompilerConfig config) {
        return switch (config.getDecompilerType()) {
            case FERNFLOWER -> new FernflowerDecompiler(config);
            case CFR -> new CFRDecompiler(config);
            case VINEFLOWER -> new VineflowerDecompiler(config);
        };
    }

    public static DecompilerAdapter createDefaultDecompiler() {
        DecompilerConfig config = new DecompilerConfig();
        return new FernflowerDecompiler(config);
    }
}



