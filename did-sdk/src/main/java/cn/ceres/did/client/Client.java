package cn.ceres.did.client;

import cn.ceres.did.sdk.SdkProto;

/**
 * @author ehlxr
 */
public interface Client {
    void start();

    void shutdown();

    SdkProto invokeSync(SdkProto proto, long timeoutMillis) throws Exception;

    void invokeAsync(SdkProto proto, long timeoutMillis, InvokeCallback invokeCallback) throws Exception;

    void invokeOneWay(SdkProto proto, long timeoutMillis) throws Exception;
}
