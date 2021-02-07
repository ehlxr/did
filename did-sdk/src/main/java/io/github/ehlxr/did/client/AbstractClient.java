package io.github.ehlxr.did.client;

import io.github.ehlxr.did.common.Constants;
import io.github.ehlxr.did.common.NettyUtil;
import io.github.ehlxr.did.common.Result;
import io.github.ehlxr.did.common.SdkProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ehlxr
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class AbstractClient implements Client {
    private final Logger logger = LoggerFactory.getLogger(AbstractClient.class);

    private final Semaphore asyncSemaphore = new Semaphore(Constants.SDK_CLIENT_ASYNC_TPS);
    private final Semaphore onewaySemaphore = new Semaphore(Constants.SDK_CLIENT_ONEWAY_TPS);

    NioEventLoopGroup workGroup;
    ChannelFuture channelFuture;
    Bootstrap bootstrap;

    int timeoutMillis;
    String host;
    int port;

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    void init() {
        workGroup = new NioEventLoopGroup(new ThreadFactory() {
            private final AtomicInteger index = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "WORK_" + index.incrementAndGet());
            }
        });

        bootstrap = new Bootstrap();
        bootstrap.group(workGroup)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // .option(ChannelOption.TCP_NODELAY, true)
                // .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class);
    }

    @Override
    public void shutdown() {
        logger.info("SDK Client shutdowning......");
        try {
            if (workGroup != null) {
                workGroup.shutdownGracefully().sync();
            }
        } catch (Exception e) {
            logger.error("Client EventLoopGroup shutdown error.", e);
        }

        logger.info("SDK Client shutdown finish!");
    }

    @Override
    public Result<SdkProto> invokeSync(long timeoutMillis) {
        final Channel channel = channelFuture.channel();
        if (channel.isOpen() && channel.isActive()) {
            final SdkProto sdkProto = new SdkProto();
            final int rqid = sdkProto.getRqid();
            try {
                final ResponseFuture responseFuture = new ResponseFuture(timeoutMillis, null, null);
                REPONSE_MAP.put(rqid, responseFuture);
                channel.writeAndFlush(sdkProto).addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        //发送成功后立即跳出
                        return;
                    }
                    // 代码执行到此说明发送失败，需要释放资源
                    REPONSE_MAP.remove(rqid);
                    responseFuture.putResponse(null);
                    responseFuture.setCause(channelFuture.cause());
                    logger.error("send a request command to channel <{}> failed.", NettyUtil.parseRemoteAddr(channel));
                });
                // 阻塞等待响应
                SdkProto proto = responseFuture.waitResponse(timeoutMillis);
                if (null == proto) {
                    return Result.fail("get result fail, addr is " + NettyUtil.parseRemoteAddr(channel) + responseFuture.getCause());
                }
                return Result.success(proto);
            } catch (Exception e) {
                logger.error("invokeSync fail, addr is " + NettyUtil.parseRemoteAddr(channel), e);
                return Result.fail("invokeSync fail, addr is " + NettyUtil.parseRemoteAddr(channel) + e.getMessage());
            } finally {
                REPONSE_MAP.remove(rqid);
            }
        } else {
            NettyUtil.closeChannel(channel);
            return Result.fail("channel " + NettyUtil.parseRemoteAddr(channel) + "is not active!");
        }
    }

    @Override
    public void invokeAsync(long timeoutMillis, InvokeCallback invokeCallback) throws Exception {
        final Channel channel = channelFuture.channel();
        if (channel.isOpen() && channel.isActive()) {
            final SdkProto sdkProto = new SdkProto();
            final int rqid = sdkProto.getRqid();
            if (asyncSemaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)) {
                final ResponseFuture responseFuture = new ResponseFuture(timeoutMillis, invokeCallback, asyncSemaphore);
                REPONSE_MAP.put(rqid, responseFuture);
                try {
                    channelFuture.channel().writeAndFlush(sdkProto).addListener(channelFuture -> {
                        if (channelFuture.isSuccess()) {
                            return;
                        }

                        // 代码执行到些说明发送失败，需要释放资源
                        logger.error("send a request command to channel <{}> failed.",
                                NettyUtil.parseRemoteAddr(channel), channelFuture.cause());

                        REPONSE_MAP.remove(rqid);
                        responseFuture.setCause(channelFuture.cause());
                        responseFuture.putResponse(null);

                        responseFuture.executeInvokeCallback();
                        responseFuture.release();
                    });
                } catch (Exception e) {
                    responseFuture.release();
                    String msg = String.format("send a request to channel <%s> Exception",
                            NettyUtil.parseRemoteAddr(channel));

                    logger.error(msg, e);
                    throw new Exception(msg, e);
                }
            } else {
                String msg = String.format("invokeAsyncImpl tryAcquire semaphore timeout, %dms, waiting thread " +
                                "nums: %d semaphoreAsyncValue: %d",
                        timeoutMillis, this.asyncSemaphore.getQueueLength(), this.asyncSemaphore.availablePermits());
                logger.error(msg);
                throw new Exception(msg);
            }
        } else {
            NettyUtil.closeChannel(channel);
            throw new Exception(String.format("channel %s is not active!", NettyUtil.parseRemoteAddr(channel)));
        }
    }

    public Result<SdkProto> invoke() {
        return invoke(timeoutMillis);
    }

    public Result<SdkProto> invokeSync() {
        return invokeSync(timeoutMillis);
    }

    public void invokeAsync(InvokeCallback invokeCallback) throws Exception {
        invokeAsync(timeoutMillis, invokeCallback);
    }
}
