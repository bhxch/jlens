package io.github.bhxch.mcp.jlens.inspector;

import io.github.bhxch.mcp.jlens.decompiler.DecompilerAdapter;
import io.github.bhxch.mcp.jlens.inspector.model.ClassMetadata;
import io.github.bhxch.mcp.jlens.inspector.model.FieldInfo;
import io.github.bhxch.mcp.jlens.inspector.model.MethodInfo;
import io.github.bhxch.mcp.jlens.inspector.model.ParameterInfo;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import io.github.bhxch.mcp.jlens.concurrent.ParallelProcessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Inspector for Java classes using Reflection
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
     * Inspect a Java class (backward compatibility)
     */
    public ClassMetadata inspect(String className, ModuleContext context,
                                 ParallelProcessor.DetailLevel level, Path sourceFile) {
        return inspect(className, context, level, sourceFile, null);
    }

    /**
     * Inspect a Java class with specific ClassLoader
     */
    public ClassMetadata inspect(String className, ModuleContext context,
                                 ParallelProcessor.DetailLevel level, Path sourceFile,
                                 ClassLoader classLoader) {
        
        // 1. Check if the class is from a local module in workspace
        if (context != null && isLocalModule(context)) {
            Path localSource = findSourceFileInModule(className, context);
            if (localSource != null) {
                return ClassMetadata.builder()
                    .className(className)
                    .status("LOCAL_SOURCE")
                    .sourceFile(localSource.toString())
                    .suggestion("This class is in your local workspace. Please use 'read_file' to view its source code directly for the most accurate information.")
                    .build();
            }
        }

        try {
            Class<?> clazz;
            if (classLoader != null) {
                clazz = Class.forName(className, true, classLoader);
            } else {
                clazz = Class.forName(className);
            }
            return inspectClass(clazz, level, sourceFile);
        } catch (ClassNotFoundException e) {
            // Fallback to basic stub if class not found in classpath
            return createStubMetadata(className, sourceFile);
        } catch (Throwable t) {
            // Handle other loading errors
            return createStubMetadata(className, sourceFile);
        }
    }

    private boolean isLocalModule(ModuleContext context) {
        // Simple heuristic: if pomFile is under current working directory
        Path currentDir = Paths.get("").toAbsolutePath();
        return context.getPomFile().toAbsolutePath().startsWith(currentDir);
    }

    private Path findSourceFileInModule(String className, ModuleContext context) {
        // Implementation to find source file in module src/main/java
        String relativePath = className.replace('.', '/') + ".java";
        Path srcDir = context.getBaseDirectory().resolve("src/main/java");
        Path sourcePath = srcDir.resolve(relativePath);
        if (Files.exists(sourcePath)) {
            return sourcePath;
        }
        
        // Check src/test/java as well
        Path testSrcDir = context.getBaseDirectory().resolve("src/test/java");
        Path testSourcePath = testSrcDir.resolve(relativePath);
        if (Files.exists(testSourcePath)) {
            return testSourcePath;
        }
        
        return null;
    }

    private ClassMetadata inspectClass(Class<?> clazz, ParallelProcessor.DetailLevel level, Path sourceFile) {
        ClassMetadata.Builder builder = ClassMetadata.builder();

        builder.className(clazz.getName());
        builder.packageName(clazz.getPackageName());
        builder.simpleClassName(clazz.getSimpleName());
        
        if (clazz.getSuperclass() != null) {
            builder.superClass(clazz.getSuperclass().getName());
        }

        for (Class<?> iface : clazz.getInterfaces()) {
            builder.addInterface(iface.getName());
        }

        builder.isInterface(clazz.isInterface());
        builder.isEnum(clazz.isEnum());
        builder.isAnnotation(clazz.isAnnotation());
        builder.isAbstract(Modifier.isAbstract(clazz.getModifiers()));
        builder.isFinal(Modifier.isFinal(clazz.getModifiers()));
        builder.isStatic(Modifier.isStatic(clazz.getModifiers()));
        builder.modifiers(clazz.getModifiers());

        if (sourceFile != null) {
            builder.sourceFile(sourceFile.toString());
        }

        // Add members based on detail level
        if (level != ParallelProcessor.DetailLevel.SKELETON) {
            // Add fields
            for (Field field : clazz.getDeclaredFields()) {
                builder.addField(FieldInfo.builder()
                    .name(field.getName())
                    .type(field.getType().getName())
                    .modifiers(field.getModifiers())
                    .isStatic(Modifier.isStatic(field.getModifiers()))
                    .isFinal(Modifier.isFinal(field.getModifiers()))
                    .build());
            }

            // Add constructors
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                List<ParameterInfo> params = new ArrayList<>();
                java.lang.reflect.Parameter[] parameters = constructor.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    java.lang.reflect.Parameter p = parameters[i];
                    params.add(ParameterInfo.builder()
                        .name(p.getName())
                        .type(p.getType().getName())
                        .index(i)
                        .isVarArgs(p.isVarArgs())
                        .build());
                }
                builder.addConstructor(MethodInfo.builder()
                    .name("<init>")
                    .returnType("void")
                    .parameters(params)
                    .modifiers(constructor.getModifiers())
                    .isStatic(Modifier.isStatic(constructor.getModifiers()))
                    .build());
            }

            // Add methods
            for (Method method : clazz.getDeclaredMethods()) {
                List<ParameterInfo> params = new ArrayList<>();
                java.lang.reflect.Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    java.lang.reflect.Parameter p = parameters[i];
                    params.add(ParameterInfo.builder()
                        .name(p.getName())
                        .type(p.getType().getName())
                        .index(i)
                        .isVarArgs(p.isVarArgs())
                        .build());
                }
                builder.addMethod(MethodInfo.builder()
                    .name(method.getName())
                    .returnType(method.getReturnType().getName())
                    .parameters(params)
                    .modifiers(method.getModifiers())
                    .isStatic(Modifier.isStatic(method.getModifiers()))
                    .isAbstract(Modifier.isAbstract(method.getModifiers()))
                    .isFinal(Modifier.isFinal(method.getModifiers()))
                    .isSynchronized(Modifier.isSynchronized(method.getModifiers()))
                    .isNative(Modifier.isNative(method.getModifiers()))
                    .isDefault(method.isDefault())
                    .isVarArgs(method.isVarArgs())
                    .build());
            }
        }

        return builder.build();
    }

    private ClassMetadata createStubMetadata(String className, Path sourceFile) {
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




