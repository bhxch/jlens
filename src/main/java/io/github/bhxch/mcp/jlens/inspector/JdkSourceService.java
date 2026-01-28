package io.github.bhxch.mcp.jlens.inspector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to extract @since information from JDK source files (src.zip)
 */
public class JdkSourceService {

    private static final Logger logger = LoggerFactory.getLogger(JdkSourceService.class);
    private static final Pattern SINCE_PATTERN = Pattern.compile("@since\\s+([^\\s*]+)");

    /**
     * Get @since information for a class and its members
     */
    public JdkSourceInfo getJdkSourceInfo(String className, Path javaHome) {
        if (javaHome == null || !Files.exists(javaHome)) {
            return null;
        }

        Path srcZip = findSrcZip(javaHome);
        if (srcZip == null) {
            logger.warn("src.zip not found in javaHome: {}", javaHome);
            return null;
        }

        String internalPath = className.replace('.', '/') + ".java";
        
        try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + srcZip.toUri()), Map.of())) {
            Path path = fs.getPath("/" + internalPath);
            if (!Files.exists(path)) {
                // Try module-based path (JDK 9+)
                // e.g. /java.base/java/util/List.java
                // We search for any directory in root that contains the internalPath
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(fs.getPath("/"))) {
                    for (Path entry : stream) {
                        Path modPath = entry.resolve(internalPath);
                        if (Files.exists(modPath)) {
                            path = modPath;
                            break;
                        }
                    }
                }
            }

            if (Files.exists(path)) {
                return parseSource(path);
            }
        } catch (IOException e) {
            logger.error("Error reading src.zip", e);
        }

        return null;
    }

    private Path findSrcZip(Path javaHome) {
        Path p1 = javaHome.resolve("lib/src.zip");
        if (Files.exists(p1)) return p1;
        Path p2 = javaHome.resolve("src.zip");
        if (Files.exists(p2)) return p2;
        return null;
    }

    private JdkSourceInfo parseSource(Path path) throws IOException {
        JdkSourceInfo info = new JdkSourceInfo();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            StringBuilder javadoc = new StringBuilder();
            boolean inJavadoc = false;

            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("/**")) {
                    inJavadoc = true;
                    javadoc.setLength(0);
                    javadoc.append(trimmed);
                } else if (inJavadoc) {
                    javadoc.append("\n").append(trimmed);
                    if (trimmed.endsWith("*/")) {
                        inJavadoc = false;
                        // Find the member following this javadoc
                        String nextLine;
                        while ((nextLine = reader.readLine()) != null) {
                            String nextTrimmed = nextLine.trim();
                            if (nextTrimmed.isEmpty() || nextTrimmed.startsWith("@")) {
                                continue;
                            }
                            processJavadocAndMember(javadoc.toString(), nextLine, info);
                            break;
                        }
                    }
                }
            }
        }
        return info;
    }

    private void processJavadocAndMember(String javadoc, String memberLine, JdkSourceInfo info) {
        String since = extractSince(javadoc);
        if (since == null) return;

        String memberTrimmed = memberLine.trim();
        
        // Very basic member detection
        if (memberTrimmed.contains("class ") || memberTrimmed.contains("interface ") || memberTrimmed.contains("enum ")) {
            info.setClassSince(since);
        } else if (memberTrimmed.contains("(")) {
            // Method
            String methodName = extractMethodName(memberTrimmed);
            if (methodName != null) {
                info.addMethodSince(methodName, since);
            }
        } else if (memberTrimmed.contains(";") || memberTrimmed.contains("=")) {
            // Field
            String fieldName = extractFieldName(memberTrimmed);
            if (fieldName != null) {
                info.addFieldSince(fieldName, since);
            }
        }
    }

    private String extractSince(String javadoc) {
        Matcher matcher = SINCE_PATTERN.matcher(javadoc);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractMethodName(String line) {
        // Simple regex for method name before '('
        Pattern p = Pattern.compile("([a-zA-Z0-9_$]+)\\s*\\(");
        Matcher m = p.matcher(line);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private String extractFieldName(String line) {
        // Simple regex for field name
        String clean = line.replaceAll("=.*", "").replaceAll(";", "").trim();
        String[] parts = clean.split("\\s+");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return null;
    }

    public static class JdkSourceInfo {
        private String classSince;
        private final Map<String, String> methodSince = new HashMap<>();
        private final Map<String, String> fieldSince = new HashMap<>();

        public String getClassSince() { return classSince; }
        public void setClassSince(String classSince) { this.classSince = classSince; }
        public Map<String, String> getMethodSince() { return methodSince; }
        public void addMethodSince(String name, String since) { methodSince.put(name, since); }
        public Map<String, String> getFieldSince() { return fieldSince; }
        public void addFieldSince(String name, String since) { fieldSince.put(name, since); }
    }
}
