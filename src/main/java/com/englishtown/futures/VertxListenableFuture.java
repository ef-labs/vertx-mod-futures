package com.englishtown.futures;

import com.google.common.util.concurrent.AbstractFuture;

/**
 */
public class VertxListenableFuture<T> extends AbstractFuture<T> {
    @Override
    public boolean set(T value) {
        return super.set(value);
    }

    @Override
    public boolean setException(Throwable throwable) {
        return super.setException(throwable);
    }
}
