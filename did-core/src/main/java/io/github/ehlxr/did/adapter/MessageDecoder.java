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

package io.github.ehlxr.did.adapter;

import io.github.ehlxr.did.SdkProto;
import io.github.ehlxr.did.common.Constants;
import io.github.ehlxr.did.common.NettyUtil;
import io.github.ehlxr.did.serializer.SerializerHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ehlxr
 * @since 2021-02-08 22:09.
 */
public class MessageDecoder extends LengthFieldBasedFrameDecoder {
    private static final int HEADER_SIZE = 6;
    private static final Logger logger = LoggerFactory.getLogger(MessageDecoder.class);

    /**
     * @param maxFrameLength      帧的最大长度
     * @param lengthFieldOffset   length 字段偏移的地址
     * @param lengthFieldLength   length 字段所占的字节长
     * @param lengthAdjustment    修改帧数据长度字段中定义的值，可以为负数 因为有时候我们习惯把头部记入长度,若为负数,则说明要推后多少个字段
     * @param initialBytesToStrip 解析时候跳过多少个长度
     * @param failFast            true，当 frame 长度超过 maxFrameLength 时立即报 TooLongFrameException 异常，
     *                            false，读取完整个帧再报异
     */

    public MessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                          int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }

    public MessageDecoder() {
        super(Constants.MAX_FRAME_LENGTH, Constants.LENGTH_FIELD_OFFSET, Constants.LENGTH_FIELD_LENGTH,
                Constants.LENGTH_ADJUSTMENT, Constants.INITIAL_BYTES_TO_STRIP, false);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 在这里调用父类的方法,实现指得到想要的部分,我在这里全部都要,也可以只要 body 部分
        in = (ByteBuf) super.decode(ctx, in);

        if (in == null) {
            return null;
        }
        if (in.readableBytes() < HEADER_SIZE) {
            throw new IllegalArgumentException("decode failed 'cause of insufficient readable bytes");
        }
        // 读取 type 字段
        byte type = in.readByte();
        // 读取 flag 字段
        byte flag = in.readByte();
        // 读取 length 字段
        int length = in.readInt();

        if (in.readableBytes() != length) {
            throw new IllegalArgumentException("the length of the readable bytes does not match the actual length");
        }
        // 读取 body
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);

        return Message.newBuilder()
                .type(type)
                .flag(flag)
                .length(bytes.length)
                .content(SerializerHolder.get().deserializer(bytes, SdkProto.class))
                .build();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        logger.error("channel {} will be closed, 'cause of ", NettyUtil.parseRemoteAddr(channel), cause);
        NettyUtil.closeChannel(channel);
    }
}