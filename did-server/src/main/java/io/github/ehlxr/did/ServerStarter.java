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

package io.github.ehlxr.did;

import io.github.ehlxr.did.common.Constants;
import io.github.ehlxr.did.generator.SnowFlake;
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

        final SnowFlake snowFlake = SnowFlake.newBuilder().datacenterId(datacenterId).machineId(machineId).build();

        // 启动 Http 服务器
        final Server httpServer = HttpServer.newBuilder().snowFlake(snowFlake).build();
        httpServer.start();

        // 启动 Sdk 服务器
        final Server sdkServer = SdkServer.newBuilder().snowFlake(snowFlake).build();
        sdkServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                Arrays.stream(new Server[]{httpServer, sdkServer})
                        // 并行 shutdown server
                        .parallel()
                        .forEach(Server::shutdown))
        );
    }
}
