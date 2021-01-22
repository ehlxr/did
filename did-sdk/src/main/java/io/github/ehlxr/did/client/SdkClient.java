package io.github.ehlxr.did.client;

import io.github.ehlxr.did.client.handler.SdkClientDecoder;
import io.github.ehlxr.did.client.handler.SdkClientEncoder;
import io.github.ehlxr.did.client.handler.SdkClientHandler;
import io.github.ehlxr.did.common.Constants;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author ehlxr
 */
public class SdkClient extends AbstractClient {
    private final Logger logger = LoggerFactory.getLogger(SdkClient.class);

    public SdkClient(String host, int port) {
        this(host, port, Constants.SDK_CLIENT_TIMEOUTMILLIS);
    }

    public SdkClient(String host, int port, int timeoutMillis) {
        this.host = host;
        this.port = port;
        this.timeoutMillis = timeoutMillis;
    }

    public SdkClient() {
        this("".equals(Constants.getEnv("SDK_HOST")) ? Constants.SERVER_HOST : Constants.getEnv("HTTP_PORT"),
                "".equals(Constants.getEnv("SDK_PORT")) ? Constants.SDK_PORT : Integer.parseInt(Constants.getEnv("SDK_PORT")));
    }

    @Override
    public void start() {
        init();

        bootstrap.group(workGroup)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // .option(ChannelOption.TCP_NODELAY, true)
                // .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast("SdkServerDecoder", new SdkClientDecoder(12))
                                .addLast("SdkServerEncoder", new SdkClientEncoder())
                                .addLast("SdkClientHandler", new SdkClientHandler());
                    }
                });

        try {
            channelFuture = bootstrap.connect(host, port).sync();

            channelFuture.channel().closeFuture().addListener((ChannelFutureListener) channelFuture -> {
                logger.warn("client channel close.", channelFuture.cause());
                shutdown();
            });

            InetSocketAddress address = (InetSocketAddress) channelFuture.channel().remoteAddress();
            logger.info("SdkClient start success, host is {}, port is {}", address.getHostName(), address.getPort());
        } catch (InterruptedException e) {
            logger.error("SdkClient start error", e);
            shutdown();
        }
    }

}
