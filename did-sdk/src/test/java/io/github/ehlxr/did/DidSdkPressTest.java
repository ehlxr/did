package io.github.ehlxr.did;

import io.github.ehlxr.did.client.SdkClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

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
    public void asyncTest() throws Exception {
        long start;
        long end;
        long cast;
        long amount = 0;
        long allcast = 0;

        for (int k = 0; k < 10; k++) {
            // 初始发送总量
            int NUM = 80000;
            final CountDownLatch countDownLatch = new CountDownLatch(NUM);
            start = System.currentTimeMillis();
            for (int i = 0; i < NUM; i++) {
                client.invokeAsync(responseFuture -> countDownLatch.countDown());
            }

            // countDownLatch.await(10, TimeUnit.SECONDS);
            countDownLatch.await();
            end = System.currentTimeMillis();
            cast = (end - start);
            allcast += cast;

            logger.info("invokeAsync test num is: {}, cast time: {} millsec, throughput: {} send/millsec", NUM, cast, (double) NUM / cast);
            amount += NUM;
            // NUM = NUM + 5000;
            // TimeUnit.SECONDS.sleep(2);
        }

        logger.info("invokeAsync test all num is: {}, all cast time: {} millsec, all throughput: {} send/millsec", amount, allcast, (double) amount / allcast);
    }

    @Test
    public void syncTest() {
        long start;
        long end;
        long cast;
        long amount = 0;
        long allcast = 0;

        for (int k = 0; k < 10; k++) {
            start = System.currentTimeMillis();
            int NUM = 60000;
            for (int i = 0; i < NUM; i++) {
                client.invokeSync();
            }

            end = System.currentTimeMillis();
            cast = (end - start);
            allcast += cast;

            logger.info("invokeSync test num is: {}, cast time: {} millsec, throughput: {} send/millsec", NUM, cast, (double) NUM / cast);

            amount += NUM;
            // NUM += 5000;
            // TimeUnit.SECONDS.sleep(2);
        }

        logger.info("invokeSync test all num is: {}, all cast time: {} millsec, all throughput: {} send/millsec", amount, allcast, (double) amount / allcast);
    }
}
