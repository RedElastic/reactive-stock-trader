package com.redelastic;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * Java 8's CompletionStage and CompletableFuture is very spartan. Here we put together some helpful functions, some of
 * this functionality has been added in the Java 9 API.
 */
public class CSHelper {
    private final ScheduledExecutorService scheduler;

    CSHelper(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    public static <T> CompletionStage<List<T>> allOf(List<CompletableFuture<T>> futures) {

        return CompletableFuture.allOf(
                futures.stream().map(CompletableFuture::toCompletableFuture).toArray(CompletableFuture[]::new))
                .thenApply(done ->
                        futures.stream()
                                .map(response -> response.toCompletableFuture().join())
                                .collect(toList())
                );
    }

    /**
     * Recover from specific exception
     *
     * @param cs             CompletionStage to monitor
     * @param exceptionClass Class of exception we want to handle.
     * @param recovery       Recovery process.
     * @param <T>            Type of response from the CompletionStage
     * @param <E>            Type of exception we want to recoverWith from.
     * @return Completion stage that either completes successfully, if {@code cs} does, or if cs produces an
     * exception which is an instance of exceptionClass then the result behaves as {@code recovery}.
     */
    public static <T, E extends Throwable> CompletionStage<T> recoverWith(CompletionStage<T> cs, Class<E> exceptionClass, Function<E, CompletionStage<T>> recovery) {
        return cs.handle((T r, Throwable ex) -> {
            if (ex == null) {
                return CompletableFuture.completedFuture(r);
            } else {
                if (exceptionClass.isInstance(ex)) {
                    return recovery.apply(exceptionClass.cast(ex));
                } else {
                    return CSHelper.<T>failedFuture(ex);
                }
            }
        })
                .thenCompose(Function.identity());
    }

    public static <T, E extends Throwable> CompletionStage<T> recover(CompletionStage<T> cs, Class<E> exceptionClass, Function<E, T> recovery) {
        return recoverWith(cs, exceptionClass, recovery.andThen(CompletableFuture::completedFuture));
    }

    /**
     * Create a CompletableFuture which completes exceptionally.
     *
     * @param ex
     * @param <T>
     * @return A CompletionStage that completes exceptionally producing {@code ex}
     */
    public static <T> CompletableFuture<T> failedFuture(Throwable ex) {
        CompletableFuture<T> future = new CompletableFuture<T>();
        future.completeExceptionally(ex);
        return future;
    }

    public <T> CompletionStage<T> withTimeout(CompletionStage<T> resultFuture, int delay, TimeUnit timeUnit) {
        CompletableFuture<T> timeoutFuture = new CompletableFuture<T>();
        scheduler.schedule(() -> timeoutFuture.completeExceptionally(new TimeoutException()), delay, timeUnit);
        return (CompletionStage<T>) CompletableFuture.anyOf(resultFuture.toCompletableFuture(), timeoutFuture);
    }
}
