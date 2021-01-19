package cn.ceres.did.client;

import cn.ceres.did.common.Constants;
import cn.ceres.did.common.NettyUtil;
import cn.ceres.did.sdk.SdkClientDecoder;
import cn.ceres.did.sdk.SdkClientEncoder;
import cn.ceres.did.sdk.SdkProto;
import io.netty.channel.*;
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
    private String host;
    private int port;

    public SdkClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public SdkClient() {
    }

    @Override
    public void start() {
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
            channelFuture = bootstrap.connect((host == null || "".equals(host)) ? Constants.DEFAULT_HOST : host,
                    port == 0 ? Constants.SDKS_PORT : port).sync();

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

    class SdkClientHandler extends SimpleChannelInboundHandler<SdkProto> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, SdkProto sdkProto) {
            final int rqid = sdkProto.getRqid();
            final ResponseFuture responseFuture = asyncResponse.get(rqid);
            if (responseFuture != null) {
                responseFuture.setSdkProto(sdkProto);
                responseFuture.release();
                asyncResponse.remove(rqid);

                // 异步请求，执行回调函数
                if (responseFuture.getInvokeCallback() != null) {
                    responseFuture.executeInvokeCallback();
                } else {
                    // 同步请求，返回数据并释放CountDown
                    responseFuture.putResponse(sdkProto);
                }
            } else {
                logger.warn("receive response, but not matched any request, address is {}", NettyUtil.parseRemoteAddr(ctx.channel()));
                logger.warn("response data is {}", sdkProto.toString());
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("SdkHandler error", cause);
            NettyUtil.closeChannel(ctx.channel());
        }

    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
