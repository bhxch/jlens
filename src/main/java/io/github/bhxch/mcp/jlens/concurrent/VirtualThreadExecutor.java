package io.github.bhxch.mcp.jlens.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Executor service optimized for virtual threads
 * Provides better performance for I/O-bound operations
 */
public class VirtualThreadExecutor {

    private static final AtomicInteger threadCounter = new AtomicInteger(0);

    private final ExecutorService virtualThreadExecutor;
    private final ExecutorService platformThreadExecutor;
    private volatile boolean isShutdown = false;

    public VirtualThreadExecutor(int maxConcurrentTasks) {
        this.virtualThreadExecutor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual()
                .name("mcp-vthread-", 0)
                .factory()
        );

        this.platformThreadExecutor = Executors.newFixedThreadPool(
            Math.min(Runtime.getRuntime().availableProcessors(), maxConcurrentTasks),
            Thread.ofPlatform()
                .name("mcp-pthread-", 0)
                .factory()
        );
    }

    /**
     * Submit an I/O-bound task to virtual thread pool
     */
    public <T> CompletableFuture<T> submitIoTask(Callable<T> task) {
        if (isShutdown) {
            throw new RejectedExecutionException("Executor has been shutdown");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, virtualThreadExecutor);
    }

    /**
     * Submit an I/O-bound task to virtual thread pool (Runnable)
     */
    public CompletableFuture<Void> submitIoTask(Runnable task) {
        if (isShutdown) {
            throw new RejectedExecutionException("Executor has been shutdown");
        }

        return CompletableFuture.runAsync(task, virtualThreadExecutor);
    }

    /**
     * Submit a CPU-bound task to platform thread pool
     */
    public <T> CompletableFuture<T> submitCpuTask(Callable<T> task) {
        if (isShutdown) {
            throw new RejectedExecutionException("Executor has been shutdown");
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, platformThreadExecutor);
    }

    /**
     * Submit a CPU-bound task to platform thread pool (Runnable)
     */
    public CompletableFuture<Void> submitCpuTask(Runnable task) {
        if (isShutdown) {
            throw new RejectedExecutionException("Executor has been shutdown");
        }

        return CompletableFuture.runAsync(task, platformThreadExecutor);
    }

    /**
     * Execute tasks in parallel with virtual threads
     */
    public <T> List<T> executeInParallel(List<Callable<T>> tasks, int timeoutSeconds)
            throws TimeoutException, ExecutionException {
        try {
            List<CompletableFuture<T>> futures = tasks.stream()
                .map(this::submitIoTask)
                .collect(Collectors.toList());

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );

            allFutures.get(timeoutSeconds, TimeUnit.SECONDS);

            return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExecutionException("Task interrupted", e);
        }
    }

    /**
     * Get the virtual thread executor
     */
    public ExecutorService getIoExecutor() {
        return virtualThreadExecutor;
    }

    /**
     * Get the platform thread executor
     */
    public ExecutorService getCpuExecutor() {
        return platformThreadExecutor;
    }

    /**
     * Check if the executor is shutdown
     */
    public boolean isShutdown() {
        return isShutdown;
    }

    /**
     * Shutdown the executor gracefully
     */
    public void shutdown() {
        isShutdown = true;
        virtualThreadExecutor.shutdown();
        platformThreadExecutor.shutdown();

        try {
            if (!virtualThreadExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                virtualThreadExecutor.shutdownNow();
            }
            if (!platformThreadExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                platformThreadExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            virtualThreadExecutor.shutdownNow();
            platformThreadExecutor.shutdownNow();
        }
    }

    /**
     * Shutdown the executor immediately
     */
    public List<Runnable> shutdownNow() {
        isShutdown = true;
        List<Runnable> remaining = new ArrayList<>();
        remaining.addAll(virtualThreadExecutor.shutdownNow());
        remaining.addAll(platformThreadExecutor.shutdownNow());
        return remaining;
    }
}



