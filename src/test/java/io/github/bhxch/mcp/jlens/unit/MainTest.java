package io.github.bhxch.mcp.jlens.unit;

import io.github.bhxch.mcp.jlens.Main;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Main Unit Tests")
class MainTest {

    @Test
    @DisplayName("Should load Main class")
    void testMainClass() {
        // Just verify the class exists and can be initialized (it's static)
        assertNotNull(new Main());
    }
}
