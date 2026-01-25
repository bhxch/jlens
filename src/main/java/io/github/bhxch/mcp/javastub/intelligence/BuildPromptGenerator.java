package io.github.bhxch.mcp.javastub.intelligence;

import io.github.bhxch.mcp.javastub.maven.model.DependencyInfo;
import io.github.bhxch.mcp.javastub.maven.model.ModuleContext;
import io.github.bhxch.mcp.javastub.maven.model.Scope;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates intelligent prompts for AI to build missing dependencies
 */
public class BuildPromptGenerator {
    
    /**
     * Generate build suggestion when class is not found
     */
    public String generateBuildSuggestion(String className, ModuleContext context, 
                                          List<DependencyInfo> missingDependencies) {
        
        StringBuilder suggestion = new StringBuilder();
        
        // Level 1: Simple missing class
        if (missingDependencies.isEmpty()) {
            suggestion.append("Class '").append(className).append("' is not found in the current module's classpath.\n");
            suggestion.append("This could be because:\n");
            suggestion.append("1. The dependency is missing from local repository\n");
            suggestion.append("2. The module hasn't been built yet\n");
            suggestion.append("3. The class is in a different module\n\n");
            suggestion.append("Please try building the module first:\n");
            suggestion.append("```bash\n");
            suggestion.append(generateMavenBuildCommand(context, false));
            suggestion.append("\n```");
            return suggestion.toString();
        }
        
        // Level 2: Specific dependencies missing
        suggestion.append("The following dependencies required for '").append(className).append("' are missing:\n\n");
        
        for (DependencyInfo dep : missingDependencies) {
            suggestion.append("- ").append(dep.getCoordinates());
            if (dep.getScope() != null) {
                suggestion.append(" (scope: ").append(dep.getScope()).append(")");
            }
            suggestion.append("\n");
        }
        
        suggestion.append("\nTo resolve this, you need to:\n");
        suggestion.append("1. **Build the module** to download dependencies:\n");
        suggestion.append("   ```bash\n");
        suggestion.append("   ").append(generateMavenBuildCommand(context, true));
        suggestion.append("\n   ```\n");
        
        suggestion.append("2. **Or download specific dependencies**:\n");
        suggestion.append("   ```bash\n");
        for (DependencyInfo dep : missingDependencies) {
            suggestion.append("   mvn dependency:get -Dartifact=").append(dep.getCoordinates());
            if (!"jar".equals(dep.getType())) {
                suggestion.append(" -Dpackaging=").append(dep.getType());
            }
            suggestion.append(" -Dtransitive=false\n");
        }
        suggestion.append("   ```\n");
        
        suggestion.append("3. **For source code inspection**, also download sources:\n");
        suggestion.append("   ```bash\n");
        for (DependencyInfo dep : missingDependencies) {
            suggestion.append("   mvn dependency:get -Dartifact=").append(dep.getCoordinates());
            suggestion.append(" -Dclassifier=sources\n");
        }
        suggestion.append("   ```");
        
        return suggestion.toString();
    }
    
    /**
     * Generate Maven build command based on context
     */
    private String generateMavenBuildCommand(ModuleContext context, boolean includeTests) {
        StringBuilder cmd = new StringBuilder("mvn");
        
        // Add module-specific path
        if (!context.getModuleRoot().equals(context.getProjectRoot())) {
            cmd.append(" -pl ").append(context.getModuleRoot().relativize(context.getProjectRoot()));
        }
        
        // Add profiles
        if (!context.getActiveProfiles().isEmpty()) {
            cmd.append(" -P").append(String.join(",", context.getActiveProfiles()));
        }
        
        // Determine goals
        if (context.getScope() == Scope.TEST) {
            cmd.append(" test-compile");
        } else {
            cmd.append(" compile");
            if (!includeTests) {
                cmd.append(" -DskipTests");
            }
        }
        
        return cmd.toString();
    }
    
    /**
     * Generate package search suggestion for ambiguous class
     */
    public String generatePackageSearchSuggestion(String simpleClassName, 
                                                 List<String> possiblePackages) {
        
        StringBuilder suggestion = new StringBuilder();
        
        if (possiblePackages.size() == 1) {
            suggestion.append("Found one possible package for class '").append(simpleClassName).append("':\n");
            suggestion.append("- ").append(possiblePackages.get(0)).append(".").append(simpleClassName).append("\n");
            suggestion.append("You can use the full qualified name in your import statement.");
        } else if (possiblePackages.size() > 1) {
            suggestion.append("Found ").append(possiblePackages.size()).append(" possible packages for class '")
                     .append(simpleClassName).append("':\n\n");
            
            // Group by common prefixes
            Map<String, List<String>> grouped = groupPackagesByPrefix(possiblePackages);
            
            for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
                suggestion.append("**").append(entry.getKey()).append("**").append(":\n");
                for (String pkg : entry.getValue()) {
                    suggestion.append("  - ").append(pkg).append(".").append(simpleClassName).append("\n");
                }
                suggestion.append("\n");
            }
            
            suggestion.append("To resolve ambiguity, you can:\n");
            suggestion.append("1. Use the full qualified class name\n");
            suggestion.append("2. Check which dependencies provide these classes:\n");
            suggestion.append("   ```bash\n");
            suggestion.append("   mvn dependency:tree | grep -E '");
            for (String pkg : possiblePackages) {
                suggestion.append(pkg.replace(".", "\\.")).append("|");
            }
            suggestion.deleteCharAt(suggestion.length() - 1); // Remove last |
            suggestion.append("'\n   ```");
        } else {
            suggestion.append("No packages found for class '").append(simpleClassName).append("'.\n");
            suggestion.append("This class might not be in any dependency, or you need to build the project first.");
        }
        
        return suggestion.toString();
    }
    
    /**
     * Group packages by common prefix
     */
    private Map<String, List<String>> groupPackagesByPrefix(List<String> packages) {
        return packages.stream()
            .collect(Collectors.groupingBy(pkg -> {
                String[] parts = pkg.split("\\.");
                if (parts.length >= 2) {
                    return parts[0] + "." + parts[1];
                }
                return parts[0];
            }));
    }
}