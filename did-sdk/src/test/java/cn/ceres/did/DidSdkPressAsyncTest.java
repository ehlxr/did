package cn.ceres.did;

import cn.ceres.did.client.InvokeCallback;
import cn.ceres.did.client.ResponseFuture;
import cn.ceres.did.client.SdkClient;
import cn.ceres.did.sdk.SdkProto;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * 异步请求压测
 *
 * @author ehlxr
 */
public class DidSdkPressAsyncTest {
    private static final Logger logger = LoggerFactory.getLogger(DidSdkPressAsyncTest.class);
    // 初始发送总量
    private static int NUM = 80000;

    @Test
    public void pressAsyncTest() throws Exception {
        SdkClient client = new SdkClient("10.19.248.200",30581);
        client.init();
        client.start();

        long start;
        long end;
        long cast;
        long amount = 0;
        long allcast = 0;

        for (int k = 0; k < 10; k++) {
            final CountDownLatch countDownLatch = new CountDownLatch(NUM);
            start = System.currentTimeMillis();
            for (int i = 0; i < NUM; i++) {
                final SdkProto sdkProto = new SdkProto();
                client.invokeAsync(sdkProto, 5000, new InvokeCallback() {
                    @Override
                    public void operationComplete(ResponseFuture responseFuture) {
                        countDownLatch.countDown();
                    }
                });
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
}
