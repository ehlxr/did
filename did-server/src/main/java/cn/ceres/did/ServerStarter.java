package cn.ceres.did;

import cn.ceres.did.core.SnowFlake;
import cn.ceres.did.server.http.HttpServer;
import cn.ceres.did.server.sdk.SdkServer;
import cn.ceres.did.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 两个服务器进程最好用同一个 SnowFlake 实例，部署在分布式环境时，SnowFlake 的 datacenterId 和 machineId 作为联合键必须全局唯一，否则多个节点的服务可能产生相同的 ID
 *
 * @author ehlxr
 */
public class ServerStarter {
    private static final Logger logger = LoggerFactory.getLogger(ServerStarter.class);

    public static void main(String[] args) {
        long datacenterId = Constants.DATACENTER_ID;
        long machineId = Constants.MACHINES_ID;

        if (args != null && args.length == 2) {
            datacenterId = Long.parseLong(args[0]);
            machineId = Long.parseLong(args[1]);
        }

        datacenterId = "".equals(Constants.getEnv("DATACENTER_ID")) ? datacenterId : Long.parseLong(Constants.getEnv("DATACENTER_ID"));
        machineId = "".equals(Constants.getEnv("MACHINES_ID")) ? machineId : Long.parseLong(Constants.getEnv("MACHINES_ID"));
        logger.info("SnowFlake datacenterId is: {}, machineId is: {}", datacenterId, machineId);

        final SnowFlake snowFlake = new SnowFlake(datacenterId, machineId);

        // 启动 Http 服务器
        final HttpServer httpServer = new HttpServer(snowFlake);
        httpServer.init();
        httpServer.start();

        // 启动 Sdk 服务器
        final SdkServer sdkServer = new SdkServer(snowFlake);
        sdkServer.init();
        sdkServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                httpServer.shutdown();
                sdkServer.shutdown();
                System.exit(0);
            }
        });
    }
}
