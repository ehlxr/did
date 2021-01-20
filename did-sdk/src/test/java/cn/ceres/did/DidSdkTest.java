package cn.ceres.did;

import cn.ceres.did.client.SdkClient;
import cn.ceres.did.common.SdkProto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author ehlxr
 */
public class DidSdkTest {
    private static final int NUM = 10;
    SdkClient client;

    @Before
    public void init() {
        client = new SdkClient("127.0.0.1", 16831, 5000);
        client.start();
    }

    @After
    public void destroy() {
        client.shutdown();
    }

    @Test
    public void didSdkTest() throws Exception {
        // 测试同步请求，关注rqid是否对应
        for (int i = 0; i < NUM; i++) {
            SdkProto resultProto = client.invokeSync();
            System.out.println(i + " resultProto: " + resultProto);
        }
        System.out.println("invokeync test finish");

        // 测试异步请求，关注rqid是否对应
        final CountDownLatch countDownLatch = new CountDownLatch(NUM);
        for (int i = 0; i < NUM; i++) {
            final int finalI = i;
            client.invokeAsync(responseFuture -> {
                countDownLatch.countDown();
                System.out.println(finalI + " resultProto: " + responseFuture.getSdkProto());
            });
        }
        countDownLatch.await(10, TimeUnit.SECONDS);
        System.out.println("invokeAsync test finish");
    }

    @Test
    public void testInvoke() throws Exception {
        System.out.println(client.invoke());

        client.setTimeoutMillis(3000);
        System.out.println(client.invoke());
    }
}
