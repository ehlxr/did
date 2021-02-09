package io.github.ehlxr.did.server.sdk;

import io.github.ehlxr.did.SdkProto;
import io.github.ehlxr.did.SnowFlake;
import io.github.ehlxr.did.adapter.Message;
import io.github.ehlxr.did.common.Constants;
import io.github.ehlxr.did.common.NettyUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 通过雪花算法生成唯一 ID，写入 Channel 返回
 *
 * @author ehlxr
 */
public class SdkServerHandler extends SimpleChannelInboundHandler<Message<SdkProto>> {
    private static final Logger logger = LoggerFactory.getLogger(SdkServerHandler.class);
    /**
     * 通过信号量来控制流量
     */
    private final Semaphore semaphore = new Semaphore(Constants.HANDLE_SDK_TPS);
    private final SnowFlake snowFlake;

    SdkServerHandler(SnowFlake snowFlake) {
        this.snowFlake = snowFlake;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message<SdkProto> message) throws Exception {
        logger.debug("sdk server handler receive message {}", message);

        if (semaphore.tryAcquire(Constants.ACQUIRE_TIMEOUTMILLIS, TimeUnit.MILLISECONDS)) {
            SdkProto sdkProto = message.content(SdkProto.class);
            sdkProto.setDid(snowFlake.nextId());

            message.setContent(sdkProto);
            semaphore.release();
        } else {
            logger.error("tryAcquire timeout after {}ms, {} threads waiting to acquire, {} permits available in this semaphore",
                    Constants.ACQUIRE_TIMEOUTMILLIS, this.semaphore.getQueueLength(), this.semaphore.availablePermits());
        }

        logger.debug("sdk server handler write message {} to channel", message);
        ctx.channel().
                writeAndFlush(message).
                addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        logger.error("SdkServerHandler channel [{}] error and will be closed", NettyUtil.parseRemoteAddr(channel), cause);
        NettyUtil.closeChannel(channel);
    }
}
