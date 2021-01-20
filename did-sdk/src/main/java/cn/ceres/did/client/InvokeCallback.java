package cn.ceres.did.client;

/**
 * @author ehlxr
 */
public interface InvokeCallback {

    /**
     * 异步回调处理方法
     *
     * @param responseFuture {@link ResponseFuture}
     */
    void operationComplete(ResponseFuture responseFuture);
}
