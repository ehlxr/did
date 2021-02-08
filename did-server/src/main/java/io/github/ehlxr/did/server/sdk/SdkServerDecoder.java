package io.github.ehlxr.did.server.sdk;

import io.github.ehlxr.did.common.NettyUtil;
import io.github.ehlxr.did.common.Try;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ehlxr
 */
public class SdkServerDecoder extends FixedLengthFrameDecoder {
    private static final Logger logger = LoggerFactory.getLogger(SdkServerDecoder.class);

    SdkServerDecoder(int frameLength) {
        super(frameLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) {
        return Try.of(() -> {
            ByteBuf buf = (ByteBuf) super.decode(ctx, in);

            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);

            buf.release();
            return NettyUtil.toObject(bytes);
        }).trap(e -> logger.error("decode error", e)).get();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        logger.error("SdkServerDecoder channel [{}] error and will be closed", NettyUtil.parseRemoteAddr(channel), cause);
        NettyUtil.closeChannel(channel);
    }
}
