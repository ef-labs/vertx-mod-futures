import com.englishtown.futures.ETFutures
import com.englishtown.promises.Promise
import com.englishtown.promises.Value
import com.google.common.util.concurrent.MoreExecutors
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.AsyncResultHandler
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.Executors
/**
 * Created by badvok on 2013-12-17.
 */
class ConvertFromGuavaTests extends Specification {
    def "test converting guava future to promise"() {
        setup: "Create a future that ultimately returns the value of 18271"
        def executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));
        def listenableFuture = executor.submit(new Callable<Integer>() {
            @Override
            Integer call() throws Exception {
                return 18271
            }
        })

        when: "We convert that future to a promise"
        def promise = ETFutures.convertToPromise(listenableFuture)

        then: "the result of the promise should be 18271"
        promise.then(new com.englishtown.promises.Runnable<Promise<Integer, Void>, Integer>() {
            @Override
            Promise<Integer, Void> run(Integer value) {
                assert value == 18271
                return null
            }
        })
    }

    def "test converting a guava future that then fails to a promise"() {
        setup: "Create a future that throws an exception"
        def executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));
        def listenableFuture = executor.submit(new Callable<Integer>() {
            @Override
            Integer call() throws Exception {
                throw new Exception("It's all gone horribly wrong")
            }
        })

        when: "we convert that future to a promise"
        def promise = ETFutures.convertToPromise(listenableFuture)

        then: "we should go through the rejected promise method with the correct exception message"
        promise.then(
                new com.englishtown.promises.Runnable<Promise<Integer, Void>, Integer>() {
                    @Override
                    Promise<Integer, Void> run(Integer value) {
                        assert false

                        return null
                    }
                },
                new com.englishtown.promises.Runnable<Promise<Integer, Void>, Value<Integer>>() {
                    @Override
                    Promise<Integer, Void> run(Value<Integer> value) {
                        assert value.error.getMessage() == "java.lang.Exception: It's all gone horribly wrong"

                        return null
                    }
                }
        )
    }

    def "test converting a guava future to a vertx future"() {
        setup: "Create a future that returns the number 18271"
        def executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2))
        def listenableFuture = executor.submit(new Callable<Integer>() {
            @Override
            Integer call() throws Exception {
                return 18271
            }
        })
        AsyncResult<Integer> result

        when: "we convert that future to a vertx future and set a handler on that vertx future"
        def vertxFuture = ETFutures.convertToVertxFuture(listenableFuture)
        vertxFuture.setHandler(new AsyncResultHandler<Integer>() {
            @Override
            void handle(AsyncResult<Integer> asyncResult) {
                result = asyncResult
            }
        })

        then: "the result should be a success with a value of 18271"
        result.succeeded()
        result.result() == 18271
    }

    def "test converting a guava future that fails to a vertx future"() {
        setup: "Create a future that fails"
        def executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2))
        def listenableFuture = executor.submit(new Callable<Integer>() {
            @Override
            Integer call() throws Exception {
                throw new Exception("It's all gone horribly wrong.")
            }
        })
        AsyncResult<Integer> result

        when: "we convert that future to a vertx future and set a handler on that vertx future"
        def vertxFuture = ETFutures.convertToVertxFuture(listenableFuture)
        vertxFuture.setHandler(new AsyncResultHandler<Integer>() {
            @Override
            void handle(AsyncResult<Integer> asyncResult) {
                result = asyncResult
            }
        })

        then: "the result should be a failure and the exception should be what we expect"
        result.failed()
        result.cause().getMessage() == "It's all gone horribly wrong."
    }
}
