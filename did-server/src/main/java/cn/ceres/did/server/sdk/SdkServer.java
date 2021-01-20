package cn.ceres.did.server.sdk;

import cn.ceres.did.common.Constants;
import cn.ceres.did.core.SnowFlake;
import cn.ceres.did.server.BaseServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ehlxr
 */
public class SdkServer extends BaseServer {
    protected Logger logger = LoggerFactory.getLogger(SdkServer.class);

    public SdkServer(SnowFlake snowFlake) {
        this.snowFlake = snowFlake;
        this.port = "".equals(Constants.getEnv("SDK_PORT")) ?
                Constants.SDK_PORT :
                Integer.parseInt(Constants.getEnv("SDK_PORT"));
    }

    public SdkServer(SnowFlake snowFlake, int port) {
        this.snowFlake = snowFlake;
        this.port = port;
    }

    @Override
    public void init() {
        super.init();
        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                // .option(ChannelOption.SO_KEEPALIVE, true)
                // .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                // .localAddress(new InetSocketAddress(port))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(defLoopGroup,
                                new SdkServerDecoder(12),
                                new SdkServerEncoder(),
                                new SdkServerHandler(snowFlake)
                        );
                    }
                });
    }

    @Override
    public void start() {
        try {
            channelFuture = serverBootstrap.bind(port).sync();
            logger.info("SdkServer start success, port is:{}", port);
        } catch (InterruptedException e) {
            logger.error("SdkServer start fail,", e);
        }
    }

    @Override
    public void shutdown() {
        logger.info("SdkServer shutdowning......");
        super.shutdown();
        logger.info("SdkServer shutdown finish!");
    }
}
