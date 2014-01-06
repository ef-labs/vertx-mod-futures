# vertx-mod-futures
Provides helper methods for converting different types of futures/promises between each other. There are methods for converting to and from:

* When.java Promises
* Vertx Futures
* Guava Future Listeners

# Usage
This module provides a single class `FuturesUtil` that provides static methods to do the conversion. These are:

```
Promise<T, Void> convertToPromise(ListenableFuture<T> listenableFuture)
Promise<T, Void> convertToPromise(Future<T> vertxFuture)

Future<T> convertToVertxFuture(ListenableFuture<T> listenableFuture)
Future<T> convertToVertxFuture(Promise<T, V> promise)

ListenableFuture<T> convertToGuavaFuture(Future<T> vertxFuture)
ListenableFuture<T> convertToGuavaFuture(Promise<T, V> promise)
```