package cn.ceres.did.client;


import cn.ceres.did.common.SdkProto;

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
     * @throws Exception 调用异常
     */
    SdkProto invokeSync(long timeoutMillis) throws Exception;

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
     * @throws Exception 调用异常
     */
    default long invoke(long timeoutMillis) throws Exception {
        return invokeSync(timeoutMillis).getDid();
    }
}
