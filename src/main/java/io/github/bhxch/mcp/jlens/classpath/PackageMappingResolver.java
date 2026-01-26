package io.github.bhxch.mcp.jlens.classpath;

import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Resolves which package a class belongs to based on imports and context
 */
public class PackageMappingResolver {
    
    private final Map<String, Set<String>> classToPackages = new ConcurrentHashMap<>();
    private final Map<String, String> packageToDependency = new ConcurrentHashMap<>();
    private final Pattern importPattern = Pattern.compile(
        "^import\\s+(?:static\\s+)?([a-zA-Z_$][a-zA-Z\\d_$]*(?:\\.[a-zA-Z_$][a-zA-Z\\d_$]*)*)\\.([a-zA-Z_$][a-zA-Z\\d_$]*)(?:\\s*;)?$"
    );
    
    /**
     * Build index of all classes in classpath with their packages
     */
    public void buildClassIndex(ModuleContext context) {
        try {
            List<Path> classpath = context.getClasspathJars();
            
            // Use virtual threads for parallel indexing
            try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
                List<java.util.concurrent.Future<Void>> futures = new ArrayList<>();
                
                for (Path jarPath : classpath) {
                    futures.add(executor.submit(() -> {
                        indexJarFile(jarPath, context);
                        return null;
                    }));
                }
                
                for (java.util.concurrent.Future<Void> future : futures) {
                    future.get();
                }
            }
            
            // Also index source JARs if available
            indexSourcePackages(context.getSourceJars(), context);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to build class index", e);
        }
    }
    
    /**
     * Index a single JAR file
     */
    private void indexJarFile(Path jarPath, ModuleContext context) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                        .replace('/', '.')
                        .replace(".class", "");
                    
                    int lastDot = className.lastIndexOf('.');
                    if (lastDot > 0) {
                        String packageName = className.substring(0, lastDot);
                        String simpleName = className.substring(lastDot + 1);
                        
                        // Store mapping
                        classToPackages.computeIfAbsent(simpleName, k -> ConcurrentHashMap.newKeySet())
                            .add(packageName);
                        
                        // Track which dependency provides this package
                        packageToDependency.put(packageName, 
                            getDependencyForJar(jarPath, context));
                    }
                }
            }
        } catch (IOException e) {
            // Log and continue with other JARs
            System.err.println("Failed to index JAR: " + jarPath + " - " + e.getMessage());
        }
    }
    
    /**
     * Index source JARs
     */
    private void indexSourcePackages(List<Path> sourceJars, ModuleContext context) {
        for (Path sourceJar : sourceJars) {
            try (JarFile jarFile = new JarFile(sourceJar.toFile())) {
                Enumeration<JarEntry> entries = jarFile.entries();
                
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".java")) {
                        String className = entry.getName()
                            .replace('/', '.')
                            .replace(".java", "");
                        
                        int lastDot = className.lastIndexOf('.');
                        if (lastDot > 0) {
                            String packageName = className.substring(0, lastDot);
                            String simpleName = className.substring(lastDot + 1);
                            
                            // Store mapping
                            classToPackages.computeIfAbsent(simpleName, k -> ConcurrentHashMap.newKeySet())
                                .add(packageName);
                        }
                    }
                }
            } catch (IOException e) {
                // Log and continue with other JARs
                System.err.println("Failed to index source JAR: " + sourceJar + " - " + e.getMessage());
            }
        }
    }
    
    /**
     * Parse import statements from Java source file
     */
    public List<String> parseImportsFromSource(String sourceCode) {
        List<String> imports = new ArrayList<>();
        String[] lines = sourceCode.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("import ")) {
                Matcher matcher = importPattern.matcher(line);
                if (matcher.matches()) {
                    imports.add(matcher.group(1) + "." + matcher.group(2));
                } else {
                    // Handle wildcard imports
                    if (line.endsWith(".*;")) {
                        String packageName = line.substring(7, line.length() - 3);
                        imports.add(packageName + ".*");
                    }
                }
            }
        }
        
        return imports;
    }
    
    /**
     * Resolve class name based on imports and context
     */
    public ClassResolutionResult resolveClassName(String simpleName, 
                                                 List<String> imports, 
                                                 String currentPackage) {
        
        ClassResolutionResult result = new ClassResolutionResult();
        result.setSimpleClassName(simpleName);
        
        // Check if it's a fully qualified name
        if (simpleName.contains(".")) {
            result.setResolvedClassName(simpleName);
            result.setResolutionType(ClassResolutionResult.ResolutionType.FULLY_QUALIFIED);
            return result;
        }
        
        // First check: Same package classes
        if (currentPackage != null) {
            String samePackageClass = currentPackage + "." + simpleName;
            if (classToPackages.containsKey(simpleName) && 
                classToPackages.get(simpleName).contains(currentPackage)) {
                result.setResolvedClassName(samePackageClass);
                result.setResolutionType(ClassResolutionResult.ResolutionType.SAME_PACKAGE);
                result.addMatchedPackage(currentPackage);
                return result;
            }
        }
        
        // Second check: Explicit imports
        for (String importStmt : imports) {
            if (!importStmt.endsWith(".*")) {
                // Regular import
                if (importStmt.endsWith("." + simpleName)) {
                    result.setResolvedClassName(importStmt);
                    result.setResolutionType(ClassResolutionResult.ResolutionType.EXPLICIT_IMPORT);
                    result.addMatchedPackage(importStmt.substring(0, importStmt.lastIndexOf('.')));
                    return result;
                }
            } else {
                // Wildcard import
                String packageName = importStmt.substring(0, importStmt.length() - 2);
                String possibleClass = packageName + "." + simpleName;
                
                Set<String> packages = classToPackages.get(simpleName);
                if (packages != null && packages.contains(packageName)) {
                    result.setResolvedClassName(possibleClass);
                    result.setResolutionType(ClassResolutionResult.ResolutionType.WILDCARD_IMPORT);
                    result.addMatchedPackage(packageName);
                    return result;
                }
            }
        }
        
        // Third check: Java.lang package (implicit)
        if (isJavaLangClass(simpleName)) {
            result.setResolvedClassName("java.lang." + simpleName);
            result.setResolutionType(ClassResolutionResult.ResolutionType.JAVA_LANG);
            result.addMatchedPackage("java.lang");
            return result;
        }
        
        // Fourth check: All possible packages
        Set<String> packages = classToPackages.get(simpleName);
        if (packages != null && !packages.isEmpty()) {
            result.setResolutionType(ClassResolutionResult.ResolutionType.AMBIGUOUS);
            result.setPossiblePackages(new ArrayList<>(packages));
            
            // Try to guess based on common patterns
            String guessedPackage = guessMostLikelyPackage(simpleName, packages, imports);
            if (guessedPackage != null) {
                result.setResolvedClassName(guessedPackage + "." + simpleName);
                result.setConfidence(calculateConfidence(simpleName, guessedPackage, imports));
            }
        } else {
            result.setResolutionType(ClassResolutionResult.ResolutionType.NOT_FOUND);
        }
        
        return result;
    }
    
    /**
     * Guess the most likely package based on naming patterns
     */
    private String guessMostLikelyPackage(String simpleName, Set<String> packages, 
                                         List<String> imports) {
        
        // Check if any package matches import patterns
        Map<String, Integer> scores = new HashMap<>();
        
        for (String pkg : packages) {
            int score = 0;
            
            // Score based on package commonality
            if (pkg.startsWith("java.") || pkg.startsWith("javax.")) {
                score += 10;
            }
            
            // Score based on class naming conventions
            if (simpleName.endsWith("Factory") && pkg.contains(".factory")) {
                score += 5;
            } else if (simpleName.endsWith("Service") && pkg.contains(".service")) {
                score += 5;
            } else if (simpleName.endsWith("DAO") && pkg.contains(".dao")) {
                score += 5;
            }
            
            // Score based on import context
            for (String importStmt : imports) {
                if (importStmt.startsWith(pkg.substring(0, Math.min(pkg.length(), 10)))) {
                    score += 3;
                }
            }
            
            scores.put(pkg, score);
        }
        
        return scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Calculate confidence score for guessed package
     */
    private double calculateConfidence(String simpleName, String guessedPackage, List<String> imports) {
        double confidence = 0.5; // Base confidence
        
        // Increase confidence if package matches import patterns
        for (String importStmt : imports) {
            if (importStmt.startsWith(guessedPackage.substring(0, Math.min(guessedPackage.length(), 10)))) {
                confidence += 0.2;
            }
        }
        
        // Increase confidence for standard library packages
        if (guessedPackage.startsWith("java.") || guessedPackage.startsWith("javax.")) {
            confidence += 0.2;
        }
        
        return Math.min(confidence, 1.0);
    }
    
    /**
     * Check if class is in java.lang package
     */
    private boolean isJavaLangClass(String className) {
        // Common java.lang classes
        Set<String> javaLangClasses = Set.of(
            "String", "Integer", "Long", "Double", "Float", "Boolean",
            "Character", "Byte", "Short", "Void", "Object", "Class",
            "System", "Math", "Runtime", "Thread", "Exception",
            "Error", "Throwable", "Runnable", "Comparable"
        );
        
        return javaLangClasses.contains(className);
    }
    
    /**
     * Get all possible packages for a simple class name
     */
    public List<String> getPossiblePackages(String simpleClassName) {
        Set<String> packages = classToPackages.get(simpleClassName);
        return packages != null ? new ArrayList<>(packages) : List.of();
    }
    
    /**
     * Get dependency for a package
     */
    public String getDependencyForPackage(String packageName) {
        return packageToDependency.get(packageName);
    }
    
    /**
     * Get dependency for a JAR file
     */
    private String getDependencyForJar(Path jarPath, ModuleContext context) {
        String fileName = jarPath.getFileName().toString();
        // Try to extract dependency coordinates from JAR name
        if (fileName.endsWith(".jar")) {
            String baseName = fileName.substring(0, fileName.length() - 4);
            // Simple heuristic: groupId:artifactId-version
            return baseName;
        }
        return jarPath.toString();
    }
    
    /**
     * Get class to packages mapping
     */
    public Map<String, Set<String>> getClassToPackages() {
        return classToPackages;
    }
    
    /**
     * Data class for resolution results
     */
    public static class ClassResolutionResult {
        private String simpleClassName;
        private String resolvedClassName;
        private ResolutionType resolutionType;
        private List<String> possiblePackages = new ArrayList<>();
        private List<String> matchedPackages = new ArrayList<>();
        private double confidence = 0.0;
        
        public enum ResolutionType {
            FULLY_QUALIFIED,
            SAME_PACKAGE,
            EXPLICIT_IMPORT,
            WILDCARD_IMPORT,
            JAVA_LANG,
            AMBIGUOUS,
            NOT_FOUND
        }
        
        // Getters and setters
        public String getSimpleClassName() {
            return simpleClassName;
        }
        
        public void setSimpleClassName(String simpleClassName) {
            this.simpleClassName = simpleClassName;
        }
        
        public String getResolvedClassName() {
            return resolvedClassName;
        }
        
        public void setResolvedClassName(String resolvedClassName) {
            this.resolvedClassName = resolvedClassName;
        }
        
        public ResolutionType getResolutionType() {
            return resolutionType;
        }
        
        public void setResolutionType(ResolutionType resolutionType) {
            this.resolutionType = resolutionType;
        }
        
        public List<String> getPossiblePackages() {
            return possiblePackages;
        }
        
        public void setPossiblePackages(List<String> possiblePackages) {
            this.possiblePackages = possiblePackages;
        }
        
        public List<String> getMatchedPackages() {
            return matchedPackages;
        }
        
        public void addMatchedPackage(String packageName) {
            this.matchedPackages.add(packageName);
        }
        
        public double getConfidence() {
            return confidence;
        }
        
        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }
    }
}




