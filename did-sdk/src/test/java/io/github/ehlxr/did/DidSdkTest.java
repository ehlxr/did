/*
 * The MIT License (MIT)
 *
 * Copyright © 2020 xrv <xrg@live.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.github.ehlxr.did;

import io.github.ehlxr.did.client.SdkClient;
import io.github.ehlxr.did.common.Try;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author ehlxr
 */
public class DidSdkTest {
    private static final Logger logger = LoggerFactory.getLogger(DidSdkTest.class);

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

        logger.info("invokeync test finish");

        // 测试异步请求
        final CountDownLatch countDownLatch = new CountDownLatch(NUM);
        IntStream.range(0, NUM).parallel().forEach(i ->
                Try.of(() -> client.invokeAsync(responseFuture -> {
                    logger.info("{}", responseFuture.getSdkProto());
                    countDownLatch.countDown();
                })).trap(Throwable::printStackTrace).run());

        //noinspection ResultOfMethodCallIgnored
        countDownLatch.await(10, TimeUnit.SECONDS);
        logger.info("invokeAsync test finish");
    }

    @Test
    public void testInvoke() {
        // logger.info("{}", client.invoke());

        client.setTimeoutMillis(30000);
        logger.info("{}", client.invoke());
    }
}
