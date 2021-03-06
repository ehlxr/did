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

package io.github.ehlxr.did.common;

import java.util.Map;

/**
 * @author ehlxr
 */
public class Constants {
    public static final int MAX_FRAME_LENGTH = 1024 * 1024;  //最大长度
    public static final int LENGTH_FIELD_LENGTH = 4;  //长度字段所占的字节数
    public static final int LENGTH_FIELD_OFFSET = 2;  //长度偏移
    public static final int LENGTH_ADJUSTMENT = 0;
    public static final int INITIAL_BYTES_TO_STRIP = 0;
    private static final Map<String, String> SYS_ENV = System.getenv();
    public static String SERVER_HOST = "localhost";
    /**
     * HTTP 协议和 SDK 协议服务器默认端口
     */
    public static int HTTP_PORT = 16830;
    public static int SDK_PORT = 16831;
    /**
     * 数据中心默认标识 ID，取值范围：0~31
     * 机器或进程默认标识 ID，取值范围：0~31
     * <p>
     * 两个标识 ID 组合在分布式环境中必须唯一
     */
    public static long DATACENTER_ID = 1;
    public static long MACHINES_ID = 1;
    /**
     * Server 流量控制，表示每秒处理的并发数
     */
    public static int HANDLE_HTTP_TPS = 10000;
    public static int HANDLE_SDK_TPS = 50000;
    /**
     * sdk client 流量控制，表示每秒处理的并发数
     */
    public static int SDK_CLIENT_ASYNC_TPS = 100000;
    public static int SDK_CLIENT_ONEWAY_TPS = 100000;
    public static int ACQUIRE_TIMEOUTMILLIS = 5000;
    /**
     * sdk client 默认超时时间
     */
    public static int SDK_CLIENT_TIMEOUTMILLIS = 2000;
    /**
     * 编码解码 byte 数组固定长度
     */
    public static int DECODER_FRAMELENGTH = 100;

    private Constants() {
    }

    public static String getEnv(String key) {
        return SYS_ENV.get(key) == null ? "" : SYS_ENV.get(key);
    }
}
