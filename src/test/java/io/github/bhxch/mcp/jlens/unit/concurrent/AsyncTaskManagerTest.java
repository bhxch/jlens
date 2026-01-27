package io.github.bhxch.mcp.jlens.unit.concurrent;

import io.github.bhxch.mcp.jlens.concurrent.AsyncTaskManager;
import io.github.bhxch.mcp.jlens.concurrent.VirtualThreadExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AsyncTaskManager Unit Tests")
class AsyncTaskManagerTest {

    private AsyncTaskManager manager;
    private VirtualThreadExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new VirtualThreadExecutor(10);
        manager = new AsyncTaskManager(executor);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        executor.shutdown();
    }

    @Test
    @DisplayName("Should submit and retrieve task")
    void testSubmitAndGet() throws Exception {
        CompletableFuture<String> future = manager.submit("task1", () -> "Task Result");
        assertNotNull(future);
        assertEquals("Task Result", future.get(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should handle failed task")
    void testTaskFailure() {
        CompletableFuture<Object> future = manager.submit("task2", () -> {
            throw new RuntimeException("Failure");
        });

        assertThrows(Exception.class, () -> future.get(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should cancel task")
    void testCancelTask() {
        manager.submit("task3", () -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {}
            return "Result";
        });
        assertTrue(manager.cancel("task3"));
    }

    @Test
    @DisplayName("Should count active tasks")
    void testRunningTaskCount() {
        manager.submit("task4", () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            return "Done";
        });

        assertTrue(manager.getRunningTaskCount() > 0);
    }
}
