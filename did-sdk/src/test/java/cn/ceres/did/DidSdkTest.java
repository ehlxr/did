package cn.ceres.did;

import cn.ceres.did.client.SdkClient;
import cn.ceres.did.sdk.SdkProto;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author ehlxr
 */
public class DidSdkTest {
    private static final int NUM = 10;

    @Test
    public void didSdkTest() throws Exception {
        SdkClient client = new SdkClient("127.0.0.1", 16831);
        // SdkClient client = new SdkClient();
        client.init();
        client.start();

        // 测试同步请求，关注rqid是否对应
        for (int i = 0; i < NUM; i++) {
            SdkProto sdkProto = new SdkProto();
            System.out.println(i + " sendProto: " + sdkProto.toString());
            SdkProto resultProto = client.invokeSync(sdkProto, 2000);
            System.out.println(i + " resultProto: " + resultProto.toString());
        }
        System.out.println("invokeync test finish");

        // 测试异步请求，关注rqid是否对应
        final CountDownLatch countDownLatch = new CountDownLatch(NUM);
        for (int i = 0; i < NUM; i++) {
            final SdkProto sdkProto = new SdkProto();
            final int finalI = i;
            client.invokeAsync(sdkProto, 2000, responseFuture -> {
                System.out.println(finalI + " sendProto: " + sdkProto.toString());
                countDownLatch.countDown();
                System.out.println(finalI + " resultProto: " + responseFuture.getSdkProto().toString());
            });
        }
        countDownLatch.await(10, TimeUnit.SECONDS);
        System.out.println("invokeAsync test finish");

    }
}
