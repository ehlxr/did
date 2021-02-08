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

package io.github.ehlxr.did.client.handler;

import io.github.ehlxr.did.client.Client;
import io.github.ehlxr.did.client.ResponseFuture;
import io.github.ehlxr.did.common.NettyUtil;
import io.github.ehlxr.did.common.SdkProto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ehlxr
 * @since 2021-01-20 14:43.
 */
public class SdkClientHandler extends SimpleChannelInboundHandler<SdkProto> {
    private final Logger logger = LoggerFactory.getLogger(SdkClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SdkProto sdkProto) {
        logger.debug("sdk client handler receive sdkProto {}", sdkProto);

        final int rqid = sdkProto.getRqid();
        final ResponseFuture responseFuture = Client.REPONSE_MAP.get(rqid);
        if (responseFuture != null) {
            responseFuture.setSdkProto(sdkProto);
            responseFuture.release();
            Client.REPONSE_MAP.remove(rqid);

            // 异步请求，执行回调函数
            if (responseFuture.getInvokeCallback() != null) {
                responseFuture.executeInvokeCallback();
            } else {
                // 同步请求，返回数据并释放 CountDown
                responseFuture.putResponse(sdkProto);
            }
        } else {
            logger.warn("receive response {}, but not matched any request, address is {}",
                    sdkProto, NettyUtil.parseRemoteAddr(ctx.channel()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("SdkHandler error", cause);
        NettyUtil.closeChannel(ctx.channel());
    }
}
