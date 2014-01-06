import com.englishtown.futures.FuturesUtil
import com.englishtown.promises.Promise
import com.englishtown.promises.Value
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import org.vertx.java.core.impl.DefaultFutureResult
import spock.lang.Specification

/**
 * Created by badvok on 2013-12-18.
 */
class ConvertFromVertxTests extends Specification {
    def "test converting vertx future to promise"() {
        setup: "create a vertx future that returns a result of 18271"
        def vertxFuture = new DefaultFutureResult<Integer>()

        when: "we create a promise from that future and then complete the future"
        def promise = FuturesUtil.convertToPromise(vertxFuture)
        vertxFuture.setResult(18271)

        then: "our promise should complete with a value of 18271"
        promise.then(
                new com.englishtown.promises.Runnable<Promise<Integer, Void>, Integer>() {
                    @Override
                    Promise<Integer, Void> run(Integer value) {
                        assert value == 18271

                        return null
                    }
                },
                new com.englishtown.promises.Runnable<Promise<Integer, Void>, Value<Integer>>() {
                    @Override
                    Promise<Integer, Void> run(Value<Integer> value) {
                        assert false
                        return null
                    }
                }
        )
    }

    def "test converting vertx future that fails to a promise"() {
        setup: "create a vertx future that is doomed to fail"
        def vertxFuture = new DefaultFutureResult<Integer>()

        when: "we create a promise from that future and then fail the future"
        def promise = FuturesUtil.convertToPromise(vertxFuture)
        vertxFuture.setFailure(new Throwable("It's all gone horribly wrong."))

        then: "our promise should complete with a rejection with the right message"
        promise.then(
                new com.englishtown.promises.Runnable<Promise<Integer, Void>, Integer>() {
                    @Override
                    Promise<Integer, Void> run(Integer value) {
                        assert false // We shouldn't be reaching here
                        return null
                    }
                },
                new com.englishtown.promises.Runnable<Promise<Integer, Void>, Value<Integer>>() {
                    @Override
                    Promise<Integer, Void> run(Value<Integer> value) {
                        assert value.error.message == "java.lang.Throwable: It's all gone horribly wrong."

                        return null
                    }
                }
        )
    }

    def "test converting vertx future to guava listenable future"() {
        setup: "create a vertx future that returns a value of 18271"
        def vertxFuture = new DefaultFutureResult<Integer>()

        when: "we create a listenable future from that vertx future and then complete the vertx future"
        def listenableFuture = FuturesUtil.convertToGuavaFuture(vertxFuture)
        vertxFuture.setResult(18271)

        then: "when we add a callback for the listenable future, it should receive a success result of 18271"
        Futures.addCallback(listenableFuture, new FutureCallback<Integer>() {
            @Override
            void onSuccess(Integer result) {
                assert result == 18271
            }

            @Override
            void onFailure(Throwable t) {
                assert false // We shouldn't be here
            }
        } )
    }

    def "test converting vertx future that fails to a guava listenable future"() {
        setup: "create a vertx future that is doomed to fail"
        def vertxFuture = new DefaultFutureResult<Integer>()

        when: "we create a listenable future from that vertx future and then fail the vertx future"
        def listenableFuture = FuturesUtil.convertToGuavaFuture(vertxFuture)
        vertxFuture.setFailure(new Throwable("It's all gone horribly wrong."))

        then: "when we add a callback for the listenable future, it should receive a failed result with the right message"
        Futures.addCallback(listenableFuture, new FutureCallback<Integer>() {
            @Override
            void onSuccess(Integer result) {
                assert false // we shouldn't be here
            }

            @Override
            void onFailure(Throwable t) {
                assert t.message == "It's all gone horribly wrong."
            }
        })
    }
}
