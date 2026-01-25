package io.github.bhxch.mcp.javastub.unit.concurrent;

import io.github.bhxch.mcp.javastub.concurrent.VirtualThreadExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VirtualThreadExecutor Unit Tests")
class VirtualThreadExecutorTest {

    @Test
    @Timeout(5)
    @DisplayName("Should execute IO-bound tasks with virtual threads")
    void testIoBoundTaskExecution() throws Exception {
        VirtualThreadExecutor executor = new VirtualThreadExecutor(10);
        AtomicInteger completedTasks = new AtomicInteger(0);

        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            int taskId = i;
            tasks.add(() -> {
                Thread.sleep(10);
                completedTasks.incrementAndGet();
                return "Task-" + taskId;
            });
        }

        List<String> results = executor.executeInParallel(tasks, 3);

        assertEquals(100, results.size());
        assertEquals(100, completedTasks.get());
        assertTrue(results.contains("Task-0"));
        assertTrue(results.contains("Task-99"));

        executor.shutdown();
    }

    @Test
    @DisplayName("Should handle task timeouts correctly")
    void testTaskTimeout() {
        VirtualThreadExecutor executor = new VirtualThreadExecutor(5);

        TimeoutException exception = assertThrows(TimeoutException.class, () -> {
            executor.executeInParallel(List.of(() -> {
                Thread.sleep(5000);
                return "result";
            }), 1);
        });

        assertNotNull(exception);
        executor.shutdown();
    }

    @Test
    @DisplayName("Should properly shutdown and terminate")
    void testExecutorShutdown() throws Exception {
        VirtualThreadExecutor executor = new VirtualThreadExecutor(10);

        CompletableFuture<String> future = executor.submitIoTask(() -> {
            Thread.sleep(100);
            return "completed";
        });

        executor.shutdown();

        assertEquals("completed", future.get());
        assertTrue(executor.isShutdown());
    }

    @Test
    @DisplayName("Should reject tasks after shutdown")
    void testRejectAfterShutdown() {
        VirtualThreadExecutor executor = new VirtualThreadExecutor(10);
        executor.shutdown();

        assertThrows(RejectedExecutionException.class, () -> {
            executor.submitIoTask(() -> "task");
        });
    }

    @Test
    @DisplayName("Should execute CPU-bound tasks with platform threads")
    void testCpuBoundTaskExecution() throws Exception {
        VirtualThreadExecutor executor = new VirtualThreadExecutor(10);
        AtomicInteger counter = new AtomicInteger(0);

        CompletableFuture<Integer> future = executor.submitCpuTask(() -> {
            for (int i = 0; i < 1000000; i++) {
                counter.incrementAndGet();
            }
            return counter.get();
        });

        Integer result = future.get(5, TimeUnit.SECONDS);

        assertEquals(1000000, result);
        executor.shutdown();
    }

    @Test
    @DisplayName("Should handle runnable tasks")
    void testRunnableTaskExecution() throws Exception {
        VirtualThreadExecutor executor = new VirtualThreadExecutor(10);
        AtomicInteger counter = new AtomicInteger(0);

        CompletableFuture<Void> future = executor.submitIoTask(() -> {
            counter.incrementAndGet();
        });

        future.get();

        assertEquals(1, counter.get());
        executor.shutdown();
    }
}