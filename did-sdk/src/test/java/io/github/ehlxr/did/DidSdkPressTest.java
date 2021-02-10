package io.github.ehlxr.did;

import io.github.ehlxr.did.client.SdkClient;
import io.github.ehlxr.did.common.Try;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

/**
 * 压测
 *
 * @author ehlxr
 */
public class DidSdkPressTest {
    private static final Logger logger = LoggerFactory.getLogger(DidSdkPressTest.class);
    private SdkClient client;

    @Before
    public void init() {
        client = new SdkClient();
        client.start();
    }

    @After
    public void destroy() {
        client.shutdown();
    }

    @Test
    public void syncTest() {
        int nums = 6000;
        int round = 10;

        AtomicLong amount = new AtomicLong();
        AtomicLong allcast = new AtomicLong();

        IntStream.range(0, round).forEach(j -> {
            long start = System.currentTimeMillis();
            IntStream.range(0, nums).parallel().forEach(i -> client.invokeSync());

            long end = System.currentTimeMillis();
            long cast = (end - start);
            allcast.addAndGet(cast);

            logger.info("invokeSync test num is: {}, cast time: {} millsec, throughput: {} send/millsec",
                    nums, cast, (long) nums / cast);
            amount.addAndGet(nums);
        });

        logger.info("invokeSync test all num is: {}, all cast time: {} millsec, all throughput: {} send/millsec",
                amount, allcast, amount.get() / allcast.get());
    }

    @Test
    public void asyncTest() {
        int nums = 6000;
        int round = 10;

        AtomicLong amount = new AtomicLong();
        AtomicLong allcast = new AtomicLong();

        IntStream.range(0, round).forEach(j -> {
            long start = System.currentTimeMillis();

            final CountDownLatch countDownLatch = new CountDownLatch(nums);
            IntStream.range(0, nums).parallel().forEach(i ->
                    Try.of(() ->
                            client.invokeAsync(rf -> countDownLatch.countDown())
                    ).trap(Throwable::printStackTrace).run()
            );
            Try.of((Try.ThrowableRunnable) countDownLatch::await).trap(Throwable::printStackTrace).run();

            long end = System.currentTimeMillis();
            long cast = (end - start);
            allcast.addAndGet(cast);

            logger.info("invokeAsync test num is: {}, cast time: {} millsec, throughput: {} send/millsec",
                    nums, cast, (long) nums / cast);
            amount.addAndGet(nums);
        });

        logger.info("invokeAsync test all num is: {}, all cast time: {} millsec, all throughput: {} send/millsec",
                amount, allcast, amount.get() / allcast.get());
    }
}
