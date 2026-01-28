package io.github.bhxch.mcp.jlens.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to extract expected metadata from source files for verification
 */
public class GroundTruthProvider {

    public static Map<String, Object> getExpectedMetadata(Path sourceFile) throws IOException {
        String content = Files.readString(sourceFile);
        Map<String, Object> expected = new HashMap<>();

        // Extract class name
        Pattern classPattern = Pattern.compile("(class|interface|enum)\\s+([a-zA-Z0-9_$]+)");
        Matcher classMatcher = classPattern.matcher(content);
        if (classMatcher.find()) {
            expected.put("simpleClassName", classMatcher.group(2));
            expected.put("type", classMatcher.group(1));
        }

        // Extract @since if present for class
        Pattern sincePattern = Pattern.compile("@since\\s+([^\\s*]+)");
        Matcher sinceMatcher = sincePattern.matcher(content);
        if (sinceMatcher.find()) {
            expected.put("since", sinceMatcher.group(1));
        }

        // Count public methods
        int publicMethods = 0;
        Pattern methodPattern = Pattern.compile("public\\s+.*\\s+([a-zA-Z0-9_$]+)\\s*\\(");
        Matcher methodMatcher = methodPattern.matcher(content);
        while (methodMatcher.find()) {
            publicMethods++;
        }
        expected.put("publicMethodCount", publicMethods);

        return expected;
    }
}
