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
 */
public class ETFutures {

    public static <T> Promise<T, Void> convertToPromise(ListenableFuture<T> listenableFuture) {
        When<T, Void> when = new When<>();
        final Deferred<T, Void> d = when.defer();

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

    public static <T> Promise<T, Void> convertToPromise(Future<T> vertxFuture) {
        When<T, Void> when = new When<>();
        final Deferred<T, Void> d = when.defer();

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

    public static <T, V> Future<T> convertToVertxFuture(Promise<T, V> promise) {
        final DefaultFutureResult<T> defaultFutureResult = new DefaultFutureResult<>();

        promise.then(
                new Runnable<Promise<T, V>, T>() {
                    @Override
                    public Promise<T, V> run(T value) {
                        defaultFutureResult.setResult(value);

                        return null;
                    }
                }, new Runnable<Promise<T, V>, Value<T>>() {
                    @Override
                    public Promise<T, V> run(Value<T> value) {
                        defaultFutureResult.setFailure(value.error);

                        return null;
                    }
                }
        );

        return defaultFutureResult;
    }

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

    public static <T, V> ListenableFuture<T> convertToGuavaFuture(Promise<T, V> promise) {
        final VertxListenableFuture<T> vertxListenableFuture = new VertxListenableFuture<>();

        promise.then(
                new Runnable<Promise<T, V>, T>() {
                    @Override
                    public Promise<T, V> run(T value) {
                        vertxListenableFuture.set(value);
                        return null;
                    }
                },
                new Runnable<Promise<T, V>, Value<T>>() {
                    @Override
                    public Promise<T, V> run(Value<T> value) {
                        vertxListenableFuture.setException(value.error);
                        return null;
                    }
                }
        );

        return vertxListenableFuture;
    }
}
