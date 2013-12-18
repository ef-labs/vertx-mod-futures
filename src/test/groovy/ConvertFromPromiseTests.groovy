import com.englishtown.futures.ETFutures
import com.englishtown.promises.Value
import com.englishtown.promises.When
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.Handler
import spock.lang.Specification

/**
 * Created by badvok on 2013-12-18.
 */
class ConvertFromPromiseTests extends Specification {
    def "test converting promise of type Integer,Void to vertx future of type Integer"() {
        setup: "create a new Promise of type Integer,Void"
        def when = new When<Integer, Void>()
        def d = when.defer()
        def promise = d.promise

        when: "we convert that promise to a vertx future and complete the promise with a result of 18271"
        def vertxFuture = ETFutures.convertToVertxFuture(promise)
        d.resolver.resolve(18271)

        then: "the vertx future should complete with the right result"
        vertxFuture.setHandler(new Handler<AsyncResult<Integer>>() {
            @Override
            void handle(AsyncResult<Integer> asyncResult) {
                assert asyncResult.succeeded()
                assert asyncResult.result() == 18271
            }
        })
    }

    def "test converting promise of type Integer,Integer to vertx future of type Integer"() {
        setup: "create a new Promise of type Integer,Void"
        def when = new When<Integer, Integer>()
        def d = when.defer()
        def promise = d.promise

        when: "we convert that promise to a vertx future and complete the promise with a result of 18271"
        def vertxFuture = ETFutures.convertToVertxFuture(promise)
        d.resolver.resolve(18271)

        then: "the vertx future should complete with the right result"
        vertxFuture.setHandler(new Handler<AsyncResult<Integer>>() {
            @Override
            void handle(AsyncResult<Integer> asyncResult) {
                assert asyncResult.succeeded()
                assert asyncResult.result() == 18271
            }
        })
    }

    def "test converting promise that fails to a vertx future"() {
        setup: "create a new Promise of type Integer, Void"
        def when = new When<Integer, Void>()
        def d = when.defer()
        def promise = d.promise

        when: "we convert that promise to a vertx future and then complete the promise with a failure"
        def vertxFuture = ETFutures.convertToVertxFuture(promise)
        d.resolver.reject(new Value<Integer>(null, new RuntimeException("It's all gone horribly wrong.")))

        then: "the vertx future should complete with an error with the correct message"
        vertxFuture.setHandler(new Handler<AsyncResult<Integer>>() {
            @Override
            void handle(AsyncResult<Integer> asyncResult) {
                assert asyncResult.failed()
                assert !asyncResult.succeeded()
                assert asyncResult.cause().message == "It's all gone horribly wrong."
            }
        })
    }

    def "test converting promist of type Integer, Void to Guava future of type Integer"() {
        setup: "create a new Promise of type Integer, Void"
        def when = new When<Integer, Void>()
        def d = when.defer()
        def promise = d.promise

        when: "we convert that promise to a guava future and then complete the promise with a value of 18271"
        def guavaFuture = ETFutures.convertToGuavaFuture(promise)
        d.resolver.resolve(18271)

        then: "the guava future, when wrapped in a callback, should callback with the correct value"
        Futures.addCallback(guavaFuture, new FutureCallback<Integer>() {
            @Override
            void onSuccess(Integer result) {
                assert result == 18271
            }

            @Override
            void onFailure(Throwable t) {
                assert false // We shouldn't be here
            }
        })
    }

    def "test converting promise that will fail to a Guava Future"() {
        setup: "create a new Promise that will ultimately fail"
        def when = new When<Integer, Void>()
        def d = when.defer()
        def promise = d.promise

        when: "we convert that promise to a guava future and then fail the promise"
        def guavaFuture = ETFutures.convertToGuavaFuture(promise)
        d.resolver.reject(new Value<Integer>(null, new RuntimeException("It's all gone horribly wrong.")))

        then: "the guava future, when wrapped in a callback, should callback with the correct value"
        Futures.addCallback(guavaFuture, new FutureCallback<Integer>() {
            @Override
            void onSuccess(Integer result) {
                assert false // We shouldn't be here
            }

            @Override
            void onFailure(Throwable t) {
                assert t.message == "It's all gone horribly wrong."
            }
        })
    }
}
