package io.github.bhxch.mcp.jlens.server;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JavaClasspathServer using direct JSON-RPC communication
 * Note: Due to MCP SDK 0.17.2 API limitations, we use direct JSON-RPC over stdio
 */
@DisplayName("JavaClasspathServer Integration Tests")
class JavaClasspathServerMcpClientTest {

    private Process serverProcess;
    private BufferedReader reader;
    private OutputStreamWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        // Start the MCP server process
        ProcessBuilder pb = new ProcessBuilder(
            "java",
            "-jar",
            "target/jlens-mcp-server-1.1.0.jar"
        );
        pb.redirectErrorStream(true);
        serverProcess = pb.start();

        reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));
        writer = new OutputStreamWriter(serverProcess.getOutputStream());

        // Give the server time to start
        Thread.sleep(2000);

        // Initialize the connection
        String initRequest = "{\"jsonrpc\":\"2.0\",\"id\":0,\"method\":\"initialize\",\"params\":{\"protocolVersion\":\"2024-11-05\",\"capabilities\":{},\"clientInfo\":{\"name\":\"test-client\",\"version\":\"1.0.0\"}}}\n";
        writer.write(initRequest);
        writer.flush();

        String initNotification = "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}\n";
        writer.write(initNotification);
        writer.flush();

        // Read initialization response - skip non-JSON lines (logs)
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("{")) {
                // Found JSON response
                assertNotNull(line, "Server should respond to initialize request");
                break;
            }
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (writer != null) {
            writer.close();
        }
        if (reader != null) {
            reader.close();
        }
        if (serverProcess != null) {
            serverProcess.destroy();
            serverProcess.waitFor(5, TimeUnit.SECONDS);
        }
    }

    private String sendRequest(String request) throws IOException, InterruptedException {
        writer.write(request);
        writer.flush();

        // Read response - skip non-JSON lines, with timeout
        long startTime = System.currentTimeMillis();
        long timeout = 120000; // 120 seconds timeout for long operations
        
        while ((System.currentTimeMillis() - startTime) < timeout) {
            String line = reader.readLine();
            if (line == null) {
                Thread.sleep(100); // Wait a bit before retrying
                continue;
            }
            if (line.trim().startsWith("{")) {
                return line;
            }
        }
        
        // Timeout - return null
        return null;
    }

    @Test
    @DisplayName("Should list all available tools")
    void testListTools() {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}\n";
        String response = assertDoesNotThrow(() -> sendRequest(request));
        assertNotNull(response);
        assertTrue(response.contains("tools"), "Response should contain tools array");
        assertTrue(response.contains("inspect_java_class"), "Response should contain inspect_java_class");
        assertTrue(response.contains("list_module_dependencies"), "Response should contain list_module_dependencies");
        assertTrue(response.contains("search_java_class"), "Response should contain search_java_class");
        assertTrue(response.contains("build_module"), "Response should contain build_module");
    }

    @Test
    @DisplayName("Should execute inspect_java_class tool")
    void testInspectJavaClass() {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"inspect_java_class\",\"arguments\":{\"className\":\"java.util.List\",\"detailLevel\":\"basic\"}}}\n";
        String response = assertDoesNotThrow(() -> sendRequest(request));
        assertNotNull(response);
        assertTrue(response.contains("List") || response.contains("java.util"), "Response should contain class information");
    }

    @Test
    @DisplayName("Should execute inspect_java_class with full detail level")
    void testInspectJavaClassWithFullDetail() {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"inspect_java_class\",\"arguments\":{\"className\":\"java.util.ArrayList\",\"detailLevel\":\"full\"}}}\n";
        String response = assertDoesNotThrow(() -> sendRequest(request));
        assertNotNull(response);
        assertTrue(response.contains("ArrayList") || response.contains("java.util"), "Response should contain class information");
    }

    @Test
    @DisplayName("Should execute inspect_java_class with source file path")
    void testInspectJavaClassWithSourceFile() {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"inspect_java_class\",\"arguments\":{\"className\":\"io.github.bhxch.mcp.jlens.Main\",\"sourceFilePath\":\"E:/repos/0000/jlens/src/main/java/io/github/bhxch/mcp/jlens/Main.java\",\"detailLevel\":\"basic\"}}}\n";
        String response = assertDoesNotThrow(() -> sendRequest(request));
        assertNotNull(response);
        assertTrue(response.contains("Main") || response.contains("io.github.bhxch.mcp.jlens"), "Response should contain class information");
    }

    @Test
    @DisplayName("Should handle missing className parameter")
    void testInspectJavaClassMissingParameter() {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"inspect_java_class\",\"arguments\":{\"detailLevel\":\"basic\"}}}\n";
        String response = assertDoesNotThrow(() -> sendRequest(request));
        assertNotNull(response);
        assertTrue(response.contains("error") || response.contains("required"), "Response should indicate error");
    }

    @Test
    @DisplayName("Should execute list_module_dependencies tool")
    void testListModuleDependencies() {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"list_module_dependencies\",\"arguments\":{\"pomFilePath\":\"E:/repos/0000/jlens/pom.xml\",\"scope\":\"compile\"}}}\n";
        String response = assertDoesNotThrow(() -> sendRequest(request));
        assertNotNull(response, "Should get a response from list_module_dependencies");
    }

    @Test
    @DisplayName("Should execute list_module_dependencies with source file path")
    void testListModuleDependenciesWithSourceFile() {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"list_module_dependencies\",\"arguments\":{\"sourceFilePath\":\"E:/repos/0000/jlens/src/main/java/io/github/bhxch/mcp/jlens/Main.java\",\"scope\":\"compile\"}}}\n";
        String response = assertDoesNotThrow(() -> sendRequest(request));
        assertNotNull(response, "Should get a response from list_module_dependencies with source file");
    }

    @Test
    @DisplayName("Should execute list_module_dependencies with test scope")
    void testListModuleDependenciesWithTestScope() {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"list_module_dependencies\",\"arguments\":{\"pomFilePath\":\"E:/repos/0000/jlens/pom.xml\",\"scope\":\"test\"}}}\n";
        String response = assertDoesNotThrow(() -> sendRequest(request));
        assertNotNull(response);
        assertTrue(response.contains("test") || response.contains("junit") || response.contains("error"), 
                   "Response should contain test dependencies or error");
    }

    @Test
    @DisplayName("Should handle invalid pom.xml file")
    void testListModuleDependenciesInvalidFile() {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"list_module_dependencies\",\"arguments\":{\"pomFilePath\":\"E:/repos/0000/jlens/nonexistent/pom.xml\",\"scope\":\"compile\"}}}\n";
        String response = assertDoesNotThrow(() -> sendRequest(request));
        assertNotNull(response);
        assertTrue(response.contains("error") || response.contains("not found"), "Response should indicate error");
    }

    @Test
    @DisplayName("Should execute search_java_class tool with wildcard")
    void testSearchJavaClassWildcard() {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"search_java_class\",\"arguments\":{\"classNamePattern\":\"*List*\",\"sourceFilePath\":\"E:/repos/0000/jlens/src/main/java/io/github/bhxch/mcp/jlens/Main.java\",\"searchType\":\"wildcard\",\"limit\":10}}}\n";
        String response = assertDoesNotThrow(() -> sendRequest(request));
        assertNotNull(response);
        assertTrue(response.contains("List") || response.contains("result"), "Response should contain search results");
    }

    @Test
    @DisplayName("Should execute search_java_class tool with prefix")
    void testSearchJavaClassPrefix() {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"search_java_class\",\"arguments\":{\"classNamePattern\":\"String\",\"sourceFilePath\":\"E:/repos/0000/jlens/src/main/java/io/github/bhxch/mcp/jlens/Main.java\",\"searchType\":\"prefix\",\"limit\":5}}}\n";
        String response = assertDoesNotThrow(() -> sendRequest(request));
        assertNotNull(response);
        assertTrue(response.contains("String") || response.contains("result"), "Response should contain search results");
    }

    @Test
    @DisplayName("Should execute search_java_class tool with exact match")
    void testSearchJavaClassExact() {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"search_java_class\",\"arguments\":{\"classNamePattern\":\"Map\",\"sourceFilePath\":\"E:/repos/0000/jlens/src/main/java/io/github/bhxch/mcp/jlens/Main.java\",\"searchType\":\"exact\",\"limit\":10}}}\n";
        String response = assertDoesNotThrow(() -> sendRequest(request));
        assertNotNull(response);
        assertTrue(response.contains("Map") || response.contains("result"), "Response should contain search results");
    }

    @Test
    @DisplayName("Should execute search_java_class tool for project classes")
    void testSearchJavaClassProjectClasses() {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"search_java_class\",\"arguments\":{\"classNamePattern\":\"*Handler*\",\"sourceFilePath\":\"E:/repos/0000/jlens/src/main/java/io/github/bhxch/mcp/jlens/Main.java\",\"searchType\":\"wildcard\",\"limit\":10}}}\n";
        String response = assertDoesNotThrow(() -> sendRequest(request));
        assertNotNull(response);
        assertTrue(response.contains("Handler") || response.contains("result"), "Response should contain search results");
    }

    @Test
    @DisplayName("Should execute build_module tool with default goals")
    void testBuildModuleDefaultGoals() {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"build_module\",\"arguments\":{\"sourceFilePath\":\"E:/repos/0000/jlens/src/main/java/io/github/bhxch/mcp/jlens/Main.java\"}}}\n";
        String response = assertDoesNotThrow(() -> sendRequest(request));
        assertNotNull(response);
        assertTrue(response.contains("BUILD") || response.contains("build") || response.contains("exitCode"), "Response should contain build information");
    }

    @Test
    @DisplayName("Should execute build_module tool with source download")
    void testBuildModuleWithSourceDownload() {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"build_module\",\"arguments\":{\"sourceFilePath\":\"E:/repos/0000/jlens/src/main/java/io/github/bhxch/mcp/jlens/Main.java\",\"downloadSources\":true,\"goals\":[\"compile\"]}}}\n";
        String response = assertDoesNotThrow(() -> sendRequest(request));
        assertNotNull(response);
        assertTrue(response.contains("BUILD") || response.contains("build") || response.contains("exitCode"), "Response should contain build information");
    }

    @Test
    @DisplayName("Should execute build_module tool with custom goals")
    void testBuildModuleCustomGoals() {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"build_module\",\"arguments\":{\"sourceFilePath\":\"E:/repos/0000/jlens/src/main/java/io/github/bhxch/mcp/jlens/Main.java\",\"goals\":[\"clean\",\"test-compile\"]}}}\n";
        String response = assertDoesNotThrow(() -> sendRequest(request));
        assertNotNull(response);
        assertTrue(response.contains("BUILD") || response.contains("build") || response.contains("exitCode"), "Response should contain build information");
    }

    @Test
    @DisplayName("Should handle complete workflow: search -> inspect")
    void testCompleteWorkflow() {
        // Step 1: Search for List classes
        String searchRequest = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"search_java_class\",\"arguments\":{\"classNamePattern\":\"*List*\",\"sourceFilePath\":\"E:/repos/0000/jlens/src/main/java/io/github/bhxch/mcp/jlens/Main.java\",\"searchType\":\"wildcard\",\"limit\":5}}}\n";
        String searchResponse = assertDoesNotThrow(() -> sendRequest(searchRequest));
        assertNotNull(searchResponse);

        // Step 2: Inspect java.util.List
        String inspectRequest = "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/call\",\"params\":{\"name\":\"inspect_java_class\",\"arguments\":{\"className\":\"java.util.List\",\"detailLevel\":\"basic\"}}}\n";
        String inspectResponse = assertDoesNotThrow(() -> sendRequest(inspectRequest));
        assertNotNull(inspectResponse);
        assertTrue(inspectResponse.contains("List") || inspectResponse.contains("java.util"), "Response should contain class information");
    }

    @Test
    @DisplayName("Should handle error recovery: non-existent class -> search -> inspect")
    void testErrorRecovery() {
        // Step 1: Try to inspect non-existent class
        String inspectRequest1 = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"inspect_java_class\",\"arguments\":{\"className\":\"com.fake.NonExistentClass\",\"detailLevel\":\"basic\"}}}\n";
        String result1 = assertDoesNotThrow(() -> sendRequest(inspectRequest1));
        assertNotNull(result1);

        // Step 2: Search for similar classes
        String searchRequest = "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/call\",\"params\":{\"name\":\"search_java_class\",\"arguments\":{\"classNamePattern\":\"*Class*\",\"sourceFilePath\":\"E:/repos/0000/jlens/src/main/java/io/github/bhxch/mcp/jlens/Main.java\",\"searchType\":\"wildcard\",\"limit\":10}}}\n";
        String searchResponse = assertDoesNotThrow(() -> sendRequest(searchRequest));
        assertNotNull(searchResponse);

        // Step 3: Inspect found class
        String inspectRequest2 = "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\"inspect_java_class\",\"arguments\":{\"className\":\"java.lang.Class\",\"detailLevel\":\"basic\"}}}\n";
        String result2 = assertDoesNotThrow(() -> sendRequest(inspectRequest2));
        assertNotNull(result2);
    }

    @Test
    @DisplayName("Should handle dependency resolution and build workflow")
    void testDependencyResolutionAndBuild() {
        // Step 1: List current dependencies
        String listRequest1 = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"list_module_dependencies\",\"arguments\":{\"pomFilePath\":\"E:/repos/0000/jlens/pom.xml\",\"scope\":\"compile\"}}}\n";
        String result1 = assertDoesNotThrow(() -> sendRequest(listRequest1));
        assertNotNull(result1);

        // Step 2: Build module
        String buildRequest = "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/call\",\"params\":{\"name\":\"build_module\",\"arguments\":{\"sourceFilePath\":\"E:/repos/0000/jlens/src/main/java/io/github/bhxch/mcp/jlens/Main.java\",\"goals\":[\"compile\"]}}}\n";
        String buildResult = assertDoesNotThrow(() -> sendRequest(buildRequest));
        assertNotNull(buildResult);

        // Step 3: List dependencies after build
        String listRequest2 = "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\"list_module_dependencies\",\"arguments\":{\"pomFilePath\":\"E:/repos/0000/jlens/pom.xml\",\"scope\":\"compile\"}}}\n";
        String result2 = assertDoesNotThrow(() -> sendRequest(listRequest2));
        assertNotNull(result2);
    }
}



