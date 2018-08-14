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

import java.net.InetSocketAddress;

/**
 * Http服务器，使用Netty中的Http协议栈，
 * 实现中支持多条请求路径，对于不存在的请求路径返回404状态码
 * 如：http://localhost:8099/getTime
 *
 * @author ehlxr
 */
public class HttpServer extends BaseServer {
    private SnowFlake snowFlake;

    public HttpServer(SnowFlake snowFlake) {
        this.snowFlake = snowFlake;
        this.port = Constants.HTTP_PORT;
    }

    @Override
    public void init() {
        super.init();
        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .localAddress(new InetSocketAddress(port))
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
            channelFuture = serverBootstrap.bind().sync();
            InetSocketAddress addr = (InetSocketAddress) channelFuture.channel().localAddress();
            logger.info("HttpServer start success, port is:{}", addr.getPort());
        } catch (InterruptedException e) {
            logger.error("HttpServer start fail,", e);
        }
    }

    @Override
    public void shutdown() {
        if (defLoopGroup != null) {
            defLoopGroup.shutdownGracefully();
        }
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

}
