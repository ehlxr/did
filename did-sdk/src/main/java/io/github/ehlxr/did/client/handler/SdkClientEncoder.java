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

import io.github.ehlxr.did.client.SdkClient;
import io.github.ehlxr.did.common.NettyUtil;
import io.github.ehlxr.did.common.SdkProto;
import io.github.ehlxr.did.common.Try;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ehlxr
 * @since 2021-01-20 14:43.
 */
public class SdkClientEncoder extends MessageToByteEncoder<SdkProto> {
    private final Logger logger = LoggerFactory.getLogger(SdkClient.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, SdkProto sdkProto, ByteBuf out) {
        Try.of(() -> {
            out.writeBytes(NettyUtil.toBytes(sdkProto));
        }).trap(e -> logger.error("encode error", e)).run();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        logger.error(String.format("SdkServerEncoder channel [%s] error and will be closed",
                NettyUtil.parseRemoteAddr(channel)), cause);

        NettyUtil.closeChannel(channel);
    }
}
