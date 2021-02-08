package io.github.ehlxr.did.client;


import io.github.ehlxr.did.common.Result;
import io.github.ehlxr.did.common.SdkProto;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ehlxr
 */
public interface Client {
    ConcurrentMap<Integer, ResponseFuture> REPONSE_MAP = new ConcurrentHashMap<>();

    /**
     * 启动 sdk client
     */
    void start();

    /**
     * 停止 sdk client
     */
    void shutdown();

    /**
     * 同步调用
     *
     * @param timeoutMillis 超时时间
     * @return {@link SdkProto}
     */
    Result<SdkProto> invokeSync(long timeoutMillis);

    /**
     * 异步调用
     *
     * @param timeoutMillis  超时时间
     * @param invokeCallback 回调接口
     * @throws Exception 调用异常
     */
    void invokeAsync(long timeoutMillis, InvokeCallback invokeCallback) throws Exception;

    /**
     * 获取 id
     *
     * @param timeoutMillis 超时时间
     * @return id
     */
    default Result<SdkProto> invoke(long timeoutMillis) {
        return invokeSync(timeoutMillis);
    }
}
