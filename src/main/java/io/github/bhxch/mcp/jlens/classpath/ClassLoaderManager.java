package io.github.bhxch.mcp.jlens.classpath;

import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages ClassLoaders for different module contexts to ensure version isolation
 */
public class ClassLoaderManager {
    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderManager.class);
    
    private final Map<String, ClassLoader> loaderCache = new ConcurrentHashMap<>();

    public ClassLoader getClassLoader(ModuleContext context) {
        if (context == null) {
            return ClassLoader.getSystemClassLoader();
        }
        
        String key = context.getPomFile().toString();
        return loaderCache.computeIfAbsent(key, k -> createClassLoader(context));
    }

    private ClassLoader createClassLoader(ModuleContext context) {
        try {
            List<Path> jars = context.getClasspathJars();
            URL[] urls = new URL[jars.size()];
            for (int i = 0; i < jars.size(); i++) {
                urls[i] = jars.get(i).toUri().toURL();
            }
            
            logger.info("Created ClassLoader for module {} with {} JARs", 
                context.getArtifactId(), jars.size());
            
            return new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
        } catch (Exception e) {
            logger.error("Failed to create ClassLoader for module " + context.getArtifactId(), e);
            return ClassLoader.getSystemClassLoader();
        }
    }
    
    public void invalidate(ModuleContext context) {
        if (context != null) {
            loaderCache.remove(context.getPomFile().toString());
        }
    }
    
    public void clear() {
        loaderCache.clear();
    }
}
