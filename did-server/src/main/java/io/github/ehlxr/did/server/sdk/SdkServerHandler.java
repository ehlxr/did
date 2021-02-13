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

package io.github.ehlxr.did.server.sdk;

import io.github.ehlxr.did.SdkProto;
import io.github.ehlxr.did.adapter.Message;
import io.github.ehlxr.did.common.Constants;
import io.github.ehlxr.did.common.NettyUtil;
import io.github.ehlxr.did.generator.SnowFlake;
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
            SdkProto sdkProto = message.getContent();
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
        logger.error("channel {} will be closed, 'cause of ", NettyUtil.parseRemoteAddr(channel), cause);
        NettyUtil.closeChannel(channel);
    }
}
