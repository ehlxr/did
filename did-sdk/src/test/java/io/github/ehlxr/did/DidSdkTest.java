package io.github.ehlxr.did;

import io.github.ehlxr.did.client.SdkClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.IntStream;

/**
 * @author ehlxr
 */
public class DidSdkTest {
    private static final int NUM = 10;
    SdkClient client;

    @Before
    public void init() {
        // client = new SdkClient("127.0.0.1", 16831, 5000);
        client = SdkClient.newBuilder().timeoutMillis(5000).build();
        client.start();
    }

    @After
    public void destroy() {
        client.shutdown();
    }

    @Test
    public void didSdkTest() throws Exception {
        // 测试同步请求
        IntStream.range(0, NUM).parallel().forEach(i -> System.out.println(client.invokeSync()));

        // System.out.println("invokeync test finish");

        // 测试异步请求
        // final CountDownLatch countDownLatch = new CountDownLatch(NUM);
        // IntStream.range(0, NUM).forEach(i ->
        //         Try.of(() -> client.invokeAsync(responseFuture -> {
        //             System.out.println(responseFuture.getSdkProto());
        //             countDownLatch.countDown();
        //         })).trap(Throwable::printStackTrace).run());
        //
        // //noinspection ResultOfMethodCallIgnored
        // countDownLatch.await(10, TimeUnit.SECONDS);
        // System.out.println("invokeAsync test finish");
    }

    @Test
    public void testInvoke() {
        // System.out.println(client.invoke());

        client.setTimeoutMillis(3000);
        System.out.println(client.invoke());
    }
}
