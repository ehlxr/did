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

package io.github.ehlxr.did.server.http;

import io.github.ehlxr.did.common.Constants;
import io.github.ehlxr.did.common.Try;
import io.github.ehlxr.did.generator.SnowFlake;
import io.github.ehlxr.did.server.BaseServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Http 服务器，使用 Netty 中的 Http 协议栈，
 *
 * @author ehlxr
 */
public class HttpServer extends BaseServer {
    protected Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private int port = "".equals(Constants.getEnv("HTTP_PORT")) ? Constants.HTTP_PORT : Integer.parseInt(Constants.getEnv("HTTP_PORT"));

    public HttpServer(SnowFlake snowFlake, int port) {
        Objects.requireNonNull(snowFlake, "snowflake instance not allow null");

        this.snowFlake = snowFlake;
        this.port = port == 0 ? this.port : port;
    }

    public HttpServer(SnowFlake snowFlake) {
        this(snowFlake, 0);
    }

    public static HttpServerBuilder newBuilder() {
        return new HttpServerBuilder();
    }

    @Override
    public void shutdown() {
        logger.info("HttpServer shutdowning......");
        super.shutdown();
        logger.info("HttpServer shutdown finish!");
    }

    @Override
    public void start() {
        Try.of(() -> {
            init();

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(defLoopGroup,
                            new HttpRequestDecoder(),
                            new HttpObjectAggregator(65536),
                            new HttpResponseEncoder(),
                            new HttpServerHandler(snowFlake)
                    );
                }
            });

            channelFuture = serverBootstrap.bind(port).sync();
            logger.info("HttpServer start success, port is:{}", port);
        }).trap(e -> logger.error("HttpServer start fail,", e)).run();
    }

    public static final class HttpServerBuilder {
        protected SnowFlake snowFlake;
        private int port;

        private HttpServerBuilder() {
        }

        public HttpServerBuilder snowFlake(SnowFlake snowFlake) {
            this.snowFlake = snowFlake;
            return this;
        }

        public HttpServerBuilder port(int port) {
            this.port = port;
            return this;
        }

        public HttpServer build() {
            return new HttpServer(snowFlake, port);
        }
    }
}
