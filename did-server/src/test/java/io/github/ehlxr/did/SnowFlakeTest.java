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

import io.github.ehlxr.did.common.Try;
import io.github.ehlxr.did.generator.SnowFlake;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

/**
 * @author ehlxr
 * @since 2021-02-10 09:37.
 */
public class SnowFlakeTest {
    SnowFlake snowFlake;

    @Before
    public void init() {
        snowFlake = SnowFlake.newBuilder().datacenterId(1).machineId(1).build();
    }

    @Test
    public void parallelTest() {
        Set<Long> ids = new CopyOnWriteArraySet<>();

        // 总数
        int counts = 40000;

        // 并发数
        int threads = 100;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(threads);

        // ExecutorService pool = Executors.newFixedThreadPool(threads);
        ForkJoinPool pool = new ForkJoinPool(threads);
        long now = System.currentTimeMillis();

        IntStream.range(0, counts).forEach(i ->
                pool.execute(() -> {
                    // 等待 threads 个任务准备就绪
                    Try.of((Try.ThrowableRunnable) cyclicBarrier::await).trap(System.out::println).run();

                    long id = snowFlake.nextId();
                    if (!ids.add(id)) {
                        System.out.println(id);
                    }
                }));

        pool.shutdown();
        while (!pool.isTerminated()) {
            Try.of(() -> Thread.sleep(10)).trap(System.out::println).run();
        }
        long end = System.currentTimeMillis();
        System.out.println(ids.size() + " ids create at " + (end - now) + "ms.");
    }

    @Test
    public void qpsTest() {
        int nums = 10000;

        IntStream.range(0, 10).forEach(j -> {
            long start = System.currentTimeMillis();
            IntStream.range(0, nums).parallel().forEach(i -> snowFlake.nextId());

            System.out.println("over: qps= " + ((long) nums * 1000 / (System.currentTimeMillis() - start)));
        });
    }
}
