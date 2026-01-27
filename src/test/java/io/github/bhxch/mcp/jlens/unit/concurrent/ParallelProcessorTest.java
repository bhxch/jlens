package io.github.bhxch.mcp.jlens.unit.concurrent;

import io.github.bhxch.mcp.jlens.concurrent.ParallelProcessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ParallelProcessor Unit Tests")
class ParallelProcessorTest {

    @Test
    @DisplayName("Should process tasks in parallel")
    void testProcessParallel() {
        List<Callable<String>> tasks = List.of(
            () -> "Task 1",
            () -> "Task 2",
            () -> {
                Thread.sleep(100);
                return "Task 3";
            }
        );

        ParallelProcessor.ParallelResult<String> result = ParallelProcessor.processInParallel(tasks, 5);

        assertNotNull(result);
        assertEquals(3, result.getResults().size());
        assertTrue(result.getResults().contains("Task 1"));
        assertTrue(result.getResults().contains("Task 2"));
        assertTrue(result.getResults().contains("Task 3"));
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should handle errors in parallel tasks")
    void testProcessParallelWithErrors() {
        List<Callable<String>> tasks = List.of(
            () -> "Success",
            () -> { throw new RuntimeException("Error occurred"); }
        );

        ParallelProcessor.ParallelResult<String> result = ParallelProcessor.processInParallel(tasks, 5);

        assertNotNull(result);
        assertEquals(1, result.getResults().size());
        assertEquals("Success", result.getResults().get(0));
        assertEquals(1, result.getErrors().size());
    }

    @Test
    @DisplayName("Should respect timeout")
    void testProcessParallelTimeout() {
        List<Callable<String>> tasks = List.of(
            () -> {
                Thread.sleep(2000);
                return "Slow Task";
            }
        );

        ParallelProcessor.ParallelResult<String> result = ParallelProcessor.processInParallel(tasks, 1);

        assertNotNull(result);
        assertTrue(result.hasErrors());
    }
}
