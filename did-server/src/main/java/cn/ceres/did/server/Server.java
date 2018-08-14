package cn.ceres.did.server;

/**
 * @author ehlxr
 */
public interface Server {
    /**
     * 启动服务
     */
    void start();

    /**
     * 关闭服务
     */
    void shutdown();
}
