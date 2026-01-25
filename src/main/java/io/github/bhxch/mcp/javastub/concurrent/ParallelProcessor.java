package io.github.bhxch.mcp.javastub.concurrent;

import io.github.bhxch.mcp.javastub.inspector.ClassInspector;
import io.github.bhxch.mcp.javastub.inspector.model.ClassMetadata;
import io.github.bhxch.mcp.javastub.maven.model.ModuleContext;
import io.github.bhxch.mcp.javastub.maven.resolver.MavenResolver;
import io.github.bhxch.mcp.javastub.maven.model.Scope;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Utility for parallel processing with virtual threads
 */
public class ParallelProcessor {

    public enum DetailLevel {
        SKELETON,
        BASIC,
        FULL
    }

    private ParallelProcessor() {
    }

    /**
     * Process multiple class inspections in parallel
     */
    public static List<ClassMetadata> inspectClassesInParallel(
        List<String> classNames,
        ModuleContext context,
        ClassInspector inspector,
        DetailLevel level
    ) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<ClassMetadata>> tasks = classNames.stream()
                .map(className -> (Callable<ClassMetadata>) () ->
                    inspector.inspect(className, context, level, null))
                .collect(Collectors.toList());

            return executor.invokeAll(tasks).stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(result -> result != null)
                .collect(Collectors.toList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Parallel inspection interrupted", e);
        }
    }

    /**
     * Parallel dependency resolution for multiple modules
     */
    public static Map<Path, ModuleContext> resolveModulesInParallel(
        List<Path> pomFiles,
        MavenResolver resolver,
        Scope scope
    ) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Map<Path, Future<ModuleContext>> futures = new ConcurrentHashMap<>();

            for (Path pomFile : pomFiles) {
                Future<ModuleContext> future = executor.submit(() ->
                    resolver.resolveModule(pomFile, scope, List.of()));
                futures.put(pomFile, future);
            }

            Map<Path, ModuleContext> results = new ConcurrentHashMap<>();
            for (Map.Entry<Path, Future<ModuleContext>> entry : futures.entrySet()) {
                try {
                    results.put(entry.getKey(), entry.getValue().get());
                } catch (Exception e) {
                    System.err.println("Failed to resolve " + entry.getKey() + ": " + e.getMessage());
                }
            }

            return results;
        }
    }

    /**
     * Process tasks in parallel with error handling
     */
    public static <T> ParallelResult<T> processInParallel(
        List<Callable<T>> tasks,
        int timeoutSeconds
    ) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<T>> futures = executor.invokeAll(tasks);
            List<T> results = new ArrayList<>();
            List<Exception> errors = new ArrayList<>();
            AtomicInteger successCount = new AtomicInteger(0);

            for (Future<T> future : futures) {
                try {
                    T result = future.get(timeoutSeconds, TimeUnit.SECONDS);
                    results.add(result);
                    successCount.incrementAndGet();
                } catch (TimeoutException e) {
                    errors.add(new TimeoutException("Task timed out"));
                } catch (ExecutionException e) {
                    errors.add(new ExecutionException("Task execution failed", e.getCause()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    errors.add(new InterruptedException("Task interrupted"));
                }
            }

            return new ParallelResult<>(results, errors, successCount.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Parallel processing interrupted", e);
        }
    }

    /**
     * Result of parallel processing
     */
    public static class ParallelResult<T> {
        private final List<T> results;
        private final List<Exception> errors;
        private final int successCount;

        public ParallelResult(List<T> results, List<Exception> errors, int successCount) {
            this.results = results;
            this.errors = errors;
            this.successCount = successCount;
        }

        public List<T> getResults() {
            return results;
        }

        public List<Exception> getErrors() {
            return errors;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getErrorCount() {
            return errors.size();
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public boolean isCompleteSuccess() {
            return errors.isEmpty();
        }
    }
}