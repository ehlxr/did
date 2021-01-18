package cn.ceres.did.common;

import java.util.Map;

/**
 * @author ehlxr
 */
public class Constants {
    private static final Map<String, String> SYS_ENV = System.getenv();

    public static String getEnv(String key) {
        return SYS_ENV.get(key) == null ? "" : SYS_ENV.get(key);
    }

    public static String DEFAULT_HOST = "localhost";
    /**
     * HTTP协议和SDK协议服务器的端口
     */
    public static int HTTP_PORT = 16830;
    public static int SDKS_PORT = 16831;

    /**
     * HTTP协议和SDK协议的请求路径
     */
    public static String HTTP_REQUEST = "did";
    public static String SDKS_REQUEST = "did";

    /**
     * 数据中心的标识ID，取值范围：0~31
     * 机器或进程的标识ID，取值范围：0~31
     * 两个标识ID组合在分布式环境中必须唯一
     */
    public static long DATACENTER_ID = 1;
    public static long MACHINES_ID = 1;

    /**
     * 流量控制，表示每秒处理的并发数
     */
    public static int HANDLE_HTTP_TPS = 10000;
    public static int HANDLE_SDKS_TPS = 50000;
    public static int ACQUIRE_TIMEOUTMILLIS = 5000;

    private Constants() {
    }
}
