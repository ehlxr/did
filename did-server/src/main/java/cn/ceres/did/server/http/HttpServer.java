package cn.ceres.did.server.http;

import cn.ceres.did.common.Constants;
import cn.ceres.did.core.SnowFlake;
import cn.ceres.did.server.BaseServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Http 服务器，使用 Netty 中的 Http 协议栈，
 *
 * @author ehlxr
 */
public class HttpServer extends BaseServer {
    protected Logger logger = LoggerFactory.getLogger(HttpServer.class);

    public HttpServer(SnowFlake snowFlake, int port) {
        this.snowFlake = snowFlake;
        this.port = port;
    }

    public HttpServer(SnowFlake snowFlake) {
        this.snowFlake = snowFlake;
        this.port = "".equals(Constants.getEnv("HTTP_PORT")) ?
                Constants.HTTP_PORT :
                Integer.parseInt(Constants.getEnv("HTTP_PORT"));
    }

    @Override
    public void init() {
        super.init();
        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                // .option(ChannelOption.SO_KEEPALIVE, false)
                // .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                // .localAddress(new InetSocketAddress(port))
                .childHandler(new ChannelInitializer<SocketChannel>() {
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
    }

    @Override
    public void start() {
        try {
            channelFuture = serverBootstrap.bind(port).sync();
            logger.info("HttpServer start success, port is:{}", port);
        } catch (InterruptedException e) {
            logger.error("HttpServer start fail,", e);
        }
    }

    @Override
    public void shutdown() {
        logger.info("HttpServer shutdowning......");
        super.shutdown();
        logger.info("HttpServer shutdown finish!");
    }
}
