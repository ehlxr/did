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

package io.github.ehlxr.did.client;

import io.github.ehlxr.did.SdkProto;
import io.github.ehlxr.did.common.Try;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ehlxr
 */
public class ResponseFuture {
    private final long timeoutMillis;
    private final Semaphore semaphore;
    private final InvokeCallback invokeCallback;
    private final AtomicBoolean executeCallbackOnlyOnce = new AtomicBoolean(false);
    private final AtomicBoolean semaphoreReleaseOnlyOnce = new AtomicBoolean(false);
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private volatile Throwable cause;
    private volatile SdkProto sdkProto;

    public ResponseFuture(long timeoutMillis, InvokeCallback invokeCallback, Semaphore semaphore) {
        this.timeoutMillis = timeoutMillis;
        this.invokeCallback = invokeCallback;
        this.semaphore = semaphore;
    }

    /**
     * 流量控制时，释放获取的信号量
     */
    public void release() {
        if (this.semaphore != null) {
            if (semaphoreReleaseOnlyOnce.compareAndSet(false, true)) {
                this.semaphore.release();
            }
        }
    }

    public void executeInvokeCallback() {
        if (invokeCallback != null) {
            if (executeCallbackOnlyOnce.compareAndSet(false, true)) {
                invokeCallback.operationComplete(this);
            }
        }
    }

    public SdkProto waitResponse(final long timeoutMillis) {
        Try.of(() -> {
            if (!this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
                setCause(new RuntimeException("timeout after wait " + timeoutMillis));
            }
        }).trap(this::setCause).run();
        return this.sdkProto;
    }

    /**
     * 同步请求时，释放阻塞的CountDown
     */
    public void putResponse(SdkProto sdkProto) {
        this.sdkProto = sdkProto;
        this.countDownLatch.countDown();
    }

    public InvokeCallback getInvokeCallback() {
        return invokeCallback;
    }

    public SdkProto getSdkProto() {
        return sdkProto;
    }

    public void setSdkProto(SdkProto sdkProto) {
        this.sdkProto = sdkProto;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public String toString() {
        return "ResponseFuture{" +
                ", timeoutMillis=" + timeoutMillis +
                ", semaphore=" + semaphore +
                ", invokeCallback=" + invokeCallback +
                ", executeCallbackOnlyOnce=" + executeCallbackOnlyOnce +
                ", semaphoreReleaseOnlyOnce=" + semaphoreReleaseOnlyOnce +
                ", countDownLatch=" + countDownLatch +
                ", cause=" + cause +
                ", sdkProto=" + sdkProto +
                '}';
    }
}
