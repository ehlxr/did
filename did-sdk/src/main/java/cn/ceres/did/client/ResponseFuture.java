package cn.ceres.did.client;

import cn.ceres.did.common.SdkProto;

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

    public SdkProto waitResponse(final long timeoutMillis) throws InterruptedException {
        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
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
