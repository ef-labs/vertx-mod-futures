package com.englishtown.futures;

import com.englishtown.promises.*;
import com.englishtown.promises.Runnable;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Utility class to convert between when.java promises, vert.x futures, and guava futures
 */
public class FuturesUtil {

    /**
     * Convert a guava future to a when.java promise
     *
     * @param listenableFuture
     * @param <T>
     * @return
     */
    public static <T> Promise<T> convertToPromise(ListenableFuture<T> listenableFuture) {
        When<T> when = new When<>();
        final Deferred<T> d = when.defer();

        Futures.addCallback(listenableFuture, new FutureCallback<T>() {
            @Override
            public void onSuccess(T result) {
                d.getResolver().resolve(result);
            }

            @Override
            public void onFailure(Throwable t) {
                d.getResolver().reject(new Value<T>(null, new RuntimeException(t)));
            }
        });

        return d.getPromise();
    }

    /**
     * Convert a vert.x future to a when.java promise
     *
     * @param vertxFuture
     * @param <T>
     * @return
     */
    public static <T> Promise<T> convertToPromise(Future<T> vertxFuture) {
        When<T> when = new When<>();
        final Deferred<T> d = when.defer();

        vertxFuture.setHandler(new Handler<AsyncResult<T>>() {
            @Override
            public void handle(AsyncResult<T> asyncResult) {
                if (asyncResult.succeeded()) {
                    d.getResolver().resolve(asyncResult.result());
                } else {
                    d.getResolver().reject(new Value<T>(null, new RuntimeException(asyncResult.cause())));
                }
            }
        });

        return d.getPromise();
    }

    /**
     * Convert a guava future to a vert.x future
     *
     * @param listenableFuture
     * @param <T>
     * @return
     */
    public static <T> Future<T> convertToVertxFuture(ListenableFuture<T> listenableFuture) {
        final DefaultFutureResult<T> defaultFutureResult = new DefaultFutureResult<>();

        Futures.addCallback(listenableFuture, new FutureCallback<T>() {
            @Override
            public void onSuccess(T result) {
                defaultFutureResult.setResult(result);
            }

            @Override
            public void onFailure(Throwable t) {
                defaultFutureResult.setFailure(t);
            }
        });

        return defaultFutureResult;
    }

    /**
     * Convert a when.java promise to a vert.x future
     *
     * @param promise
     * @param <T>
     * @param <V>
     * @return
     */
    public static <T, V> Future<T> convertToVertxFuture(Promise<T> promise) {
        final DefaultFutureResult<T> defaultFutureResult = new DefaultFutureResult<>();

        promise.then(
                new Runnable<Promise<T>, T>() {
                    @Override
                    public Promise<T> run(T value) {
                        defaultFutureResult.setResult(value);

                        return null;
                    }
                }, new Runnable<Promise<T>, Value<T>>() {
                    @Override
                    public Promise<T> run(Value<T> value) {
                        defaultFutureResult.setFailure(value.getCause());

                        return null;
                    }
                }
        );

        return defaultFutureResult;
    }

    /**
     * Convert a vert.x future to a guava future
     *
     * @param vertxFuture
     * @param <T>
     * @return
     */
    public static <T> ListenableFuture<T> convertToGuavaFuture(Future<T> vertxFuture) {
        final VertxListenableFuture<T> vertxListenableFuture = new VertxListenableFuture<>();

        vertxFuture.setHandler(new Handler<AsyncResult<T>>() {
            @Override
            public void handle(AsyncResult<T> asyncResult) {
                if (asyncResult.succeeded()) {
                    vertxListenableFuture.set(asyncResult.result());
                } else {
                    vertxListenableFuture.setException(asyncResult.cause());
                }
            }
        });

        return vertxListenableFuture;
    }

    /**
     * Convert a when.java promise to a guava future
     *
     * @param promise
     * @param <T>
     * @param <V>
     * @return
     */
    public static <T, V> ListenableFuture<T> convertToGuavaFuture(Promise<T> promise) {
        final VertxListenableFuture<T> vertxListenableFuture = new VertxListenableFuture<>();

        promise.then(
                new Runnable<Promise<T>, T>() {
                    @Override
                    public Promise<T> run(T value) {
                        vertxListenableFuture.set(value);
                        return null;
                    }
                },
                new Runnable<Promise<T>, Value<T>>() {
                    @Override
                    public Promise<T> run(Value<T> value) {
                        vertxListenableFuture.setException(value.getCause());
                        return null;
                    }
                }
        );

        return vertxListenableFuture;
    }
}
