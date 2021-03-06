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

import io.github.ehlxr.did.adapter.MessageDecoder;
import io.github.ehlxr.did.adapter.MessageEncoder;
import io.github.ehlxr.did.client.handler.SdkClientHandler;
import io.github.ehlxr.did.common.Constants;
import io.github.ehlxr.did.common.Try;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author ehlxr
 */
public class SdkClient extends AbstractClient {
    private final Logger logger = LoggerFactory.getLogger(SdkClient.class);

    public SdkClient(String host, int port) {
        this(host, port, 0);
    }

    public SdkClient(String host, int port, int timeoutMillis) {
        this.host = Objects.isNull(host) ?
                "".equals(Constants.getEnv("SDK_HOST")) ? Constants.SERVER_HOST : Constants.getEnv("HTTP_PORT") :
                host;

        this.port = port <= 0 ?
                "".equals(Constants.getEnv("SDK_PORT")) ? Constants.SDK_PORT : Integer.parseInt(Constants.getEnv("SDK_PORT")) :
                port;

        this.timeoutMillis = timeoutMillis <= 0 ? Constants.SDK_CLIENT_TIMEOUTMILLIS : timeoutMillis;
    }

    public SdkClient() {
        this(null, 0);
    }

    public static SdkClientBuilder newBuilder() {
        return new SdkClientBuilder();
    }

    @Override
    public void start() {
        init();

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline()
                        .addLast(new MessageEncoder())
                        .addLast(new MessageDecoder())
                        .addLast(new SdkClientHandler());
            }
        });

        Try.of(() -> {
            channelFuture = bootstrap.connect(host, port)
                    .sync()
                    .channel()
                    .closeFuture()
                    .addListener((ChannelFutureListener) channelFuture ->
                            logger.warn("client channel close.", channelFuture.cause()));

            InetSocketAddress address = (InetSocketAddress) channelFuture.channel().remoteAddress();
            logger.info("SdkClient start success, host is {}, port is {}", address.getHostName(), address.getPort());
        }).trap(e -> {
            logger.error("SdkClient start error", e);
            shutdown();
        }).run();
    }

    public static final class SdkClientBuilder {
        int timeoutMillis;
        String host;
        int port;

        private SdkClientBuilder() {
        }

        public static SdkClientBuilder aSdkClient() {
            return new SdkClientBuilder();
        }

        public SdkClientBuilder timeoutMillis(int timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            return this;
        }

        public SdkClientBuilder host(String host) {
            this.host = host;
            return this;
        }

        public SdkClientBuilder port(int port) {
            this.port = port;
            return this;
        }

        public SdkClient build() {
            return new SdkClient(host, port, timeoutMillis);
        }
    }
}
