package io.github.ehlxr.did.server.sdk;

import io.github.ehlxr.did.common.NettyUtil;
import io.github.ehlxr.did.common.Try;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author ehlxr
 */
public class SdkServerDecoder extends ByteToMessageDecoder {
    private final Logger logger = LoggerFactory.getLogger(SdkServerDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        Try.of(() -> {
            byte[] bytes = new byte[in.readableBytes()];
            in.readBytes(bytes);

            out.add(NettyUtil.toObject(bytes));
        }).trap(e -> logger.error("decode error", e)).run();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        logger.error("SdkServerDecoder channel [{}] error and will be closed", NettyUtil.parseRemoteAddr(channel), cause);
        NettyUtil.closeChannel(channel);
    }
}