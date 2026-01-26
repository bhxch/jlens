package io.github.bhxch.mcp.jlens.concurrent;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Manager for async tasks with cancellation support
 */
public class AsyncTaskManager {

    private final Map<String, CompletableFuture<?>> runningTasks = new ConcurrentHashMap<>();
    private final VirtualThreadExecutor executor;

    public AsyncTaskManager(VirtualThreadExecutor executor) {
        this.executor = executor;
    }

    /**
     * Submit an async task with a unique ID
     */
    public <T> CompletableFuture<T> submit(String taskId, Supplier<T> task) {
        CompletableFuture<T> future = executor.submitIoTask(task::get)
            .whenComplete((result, error) -> runningTasks.remove(taskId));

        runningTasks.put(taskId, future);
        return future;
    }

    /**
     * Cancel a running task by ID
     */
    public boolean cancel(String taskId) {
        CompletableFuture<?> future = runningTasks.get(taskId);
        if (future != null) {
            return future.cancel(true);
        }
        return false;
    }

    /**
     * Cancel all running tasks
     */
    public void cancelAll() {
        runningTasks.values().forEach(future -> future.cancel(true));
        runningTasks.clear();
    }

    /**
     * Get the number of running tasks
     */
    public int getRunningTaskCount() {
        return runningTasks.size();
    }

    /**
     * Check if a task is running
     */
    public boolean isRunning(String taskId) {
        CompletableFuture<?> future = runningTasks.get(taskId);
        return future != null && !future.isDone();
    }

    /**
     * Wait for all tasks to complete
     */
    public void awaitAll() throws InterruptedException {
        CompletableFuture.allOf(runningTasks.values().toArray(new CompletableFuture[0])).join();
    }

    /**
     * Wait for all tasks to complete with timeout
     */
    public boolean awaitAll(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            CompletableFuture.allOf(runningTasks.values().toArray(new CompletableFuture[0]))
                .get(timeout, unit);
            return true;
        } catch (ExecutionException | TimeoutException e) {
            return false;
        }
    }
}



