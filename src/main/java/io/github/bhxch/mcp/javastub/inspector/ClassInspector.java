package io.github.bhxch.mcp.javastub.inspector;

import io.github.bhxch.mcp.javastub.decompiler.DecompilerAdapter;
import io.github.bhxch.mcp.javastub.inspector.model.ClassMetadata;
import io.github.bhxch.mcp.javastub.maven.model.ModuleContext;
import io.github.bhxch.mcp.javastub.concurrent.ParallelProcessor;

import java.nio.file.Path;

/**
 * Inspector for Java classes
 */
public class ClassInspector {

    private final DecompilerAdapter decompiler;

    public ClassInspector(DecompilerAdapter decompiler) {
        this.decompiler = decompiler;
    }

    public ClassInspector() {
        this.decompiler = null;
    }

    /**
     * Inspect a Java class
     */
    public ClassMetadata inspect(String className, ModuleContext context,
                                 ParallelProcessor.DetailLevel level, Path sourceFile) {
        ClassMetadata.Builder builder = ClassMetadata.builder();

        builder.className(className);

        int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex > 0) {
            builder.packageName(className.substring(0, lastDotIndex));
            builder.simpleClassName(className.substring(lastDotIndex + 1));
        } else {
            builder.packageName("");
            builder.simpleClassName(className);
        }

        if (sourceFile != null) {
            builder.sourceFile(sourceFile.toString());
        }

        return builder.build();
    }
}