package io.github.ehlxr.did;

import io.github.ehlxr.did.common.Constants;
import io.github.ehlxr.did.core.SnowFlake;
import io.github.ehlxr.did.server.Server;
import io.github.ehlxr.did.server.http.HttpServer;
import io.github.ehlxr.did.server.sdk.SdkServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
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

        // 并行 shutdown server
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                Arrays.stream(new Server[]{httpServer, sdkServer}).parallel().forEach(Server::shutdown)));
    }
}
