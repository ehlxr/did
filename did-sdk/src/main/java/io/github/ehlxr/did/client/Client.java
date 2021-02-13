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
import io.github.ehlxr.did.common.Result;

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
