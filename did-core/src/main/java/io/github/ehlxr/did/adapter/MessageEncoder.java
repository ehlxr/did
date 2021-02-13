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
import io.github.ehlxr.did.common.Try;
import io.github.ehlxr.did.serializer.SerializerHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.Objects;

/**
 * @author ehlxr
 * @since 2021-02-08 22:12.
 */
public class MessageEncoder extends MessageToByteEncoder<Message<SdkProto>> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message<SdkProto> msg, ByteBuf out) {
        Objects.requireNonNull(msg, "encode failed 'cause of recive message is null");
        SdkProto content = msg.getContent();
        byte[] bytes = Try.<SdkProto, byte[]>of(SerializerHolder.get()::serializer).apply(content).get();

        out.writeByte(msg.getType());
        out.writeByte(msg.getFlag());
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}

