package io.github.ehlxr.did.server.sdk;

import io.github.ehlxr.did.common.Constants;
import io.github.ehlxr.did.core.SnowFlake;
import io.github.ehlxr.did.server.BaseServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author ehlxr
 */
public class SdkServer extends BaseServer {
    protected Logger logger = LoggerFactory.getLogger(SdkServer.class);
    private int port = "".equals(Constants.getEnv("SDK_PORT")) ? Constants.SDK_PORT : Integer.parseInt(Constants.getEnv("SDK_PORT"));

    public SdkServer(SnowFlake snowFlake) {
        this(snowFlake, 0);
    }

    public SdkServer(SnowFlake snowFlake, int port) {
        Objects.requireNonNull(snowFlake, "snowflake instance not allow null");

        this.snowFlake = snowFlake;
        this.port = port <= 0 ? this.port : port;
    }

    public static SdkServerBuilder newBuilder() {
        return new SdkServerBuilder();
    }

    @Override
    public void shutdown() {
        logger.info("SdkServer shutdowning......");
        super.shutdown();
        logger.info("SdkServer shutdown finish!");
    }

    @Override
    public void start() {
        try {
            init();

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(defLoopGroup,
                            new SdkServerDecoder(12),
                            new SdkServerEncoder(),
                            new SdkServerHandler(snowFlake)
                    );
                }
            });

            channelFuture = serverBootstrap.bind(port).sync();
            logger.info("SdkServer start success, port is:{}", port);
        } catch (InterruptedException e) {
            logger.error("SdkServer start fail,", e);
        }
    }

    public static final class SdkServerBuilder {
        protected SnowFlake snowFlake;
        private int port;

        private SdkServerBuilder() {
        }

        public SdkServerBuilder snowFlake(SnowFlake snowFlake) {
            this.snowFlake = snowFlake;
            return this;
        }

        public SdkServerBuilder port(int port) {
            this.port = port;
            return this;
        }

        public SdkServer build() {
            return new SdkServer(snowFlake, port);
        }
    }
}