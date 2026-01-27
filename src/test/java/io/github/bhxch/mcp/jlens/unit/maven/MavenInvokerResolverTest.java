package io.github.bhxch.mcp.jlens.unit.maven;

import io.github.bhxch.mcp.jlens.config.MavenConfig;
import io.github.bhxch.mcp.jlens.maven.model.ModuleContext;
import io.github.bhxch.mcp.jlens.maven.model.Scope;
import io.github.bhxch.mcp.jlens.maven.resolver.MavenInvokerResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MavenInvokerResolver Unit Tests")
class MavenInvokerResolverTest {

    private MavenConfig config;

    @BeforeEach
    void setUp() {
        config = new MavenConfig();
    }

    static class FakeProcess extends Process {
        private final InputStream inputStream;
        private final int exitCode;

        public FakeProcess(String output, int exitCode) {
            this.inputStream = new ByteArrayInputStream(output.getBytes());
            this.exitCode = exitCode;
        }

        @Override
        public OutputStream getOutputStream() { return new ByteArrayOutputStream(); }
        @Override
        public InputStream getInputStream() { return inputStream; }
        @Override
        public InputStream getErrorStream() { return new ByteArrayInputStream(new byte[0]); }
        @Override
        public int waitFor() { return exitCode; }
        @Override
        public int exitValue() { return exitCode; }
        @Override
        public void destroy() {}
    }

    @Test
    @DisplayName("Should parse Maven output correctly")
    void testResolveModule() throws Exception {
        String output = "[INFO] io.github.bhxch:mcp-core:jar:0.17.2:compile\n" +
                       "[INFO] com.fasterxml.jackson.core:jackson-databind:jar:2.19.2:compile\n";
        
        MavenInvokerResolver resolver = new MavenInvokerResolver(config) {
            @Override
            protected Process startProcess(Path pomFile, List<String> command) {
                return new FakeProcess(output, 0);
            }
        };

        Path pomPath = Path.of("pom.xml").toAbsolutePath();
        ModuleContext context = resolver.resolveModule(pomPath, Scope.COMPILE, List.of());

        assertNotNull(context);
        assertEquals(2, context.getDependencies().size());
        assertEquals("io.github.bhxch:mcp-core:jar:0.17.2", context.getDependencies().get(0).getCoordinates());
    }

    @Test
    @DisplayName("Should handle Maven command failure")
    void testMavenFailure() throws Exception {
        MavenInvokerResolver resolver = new MavenInvokerResolver(config) {
            @Override
            protected Process startProcess(Path pomFile, List<String> command) {
                return new FakeProcess("", 1);
            }
        };

        assertThrows(RuntimeException.class, () -> {
            resolver.resolveModule(Path.of("pom.xml").toAbsolutePath(), Scope.COMPILE, List.of());
        });
    }
}