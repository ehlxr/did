package io.github.ehlxr.did.server.sdk;

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
 */
public class SdkServerEncoder extends MessageToByteEncoder<SdkProto> {
    private static final Logger logger = LoggerFactory.getLogger(SdkServerEncoder.class);


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, SdkProto sdkProto, ByteBuf out) {
        Try.of(() -> {
            out.writeBytes(NettyUtil.toBytes(sdkProto));
        }).trap(e -> logger.error("encode error", e)).run();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        logger.error("SdkServerEncoder channel [{}] error and will be closed", NettyUtil.parseRemoteAddr(channel), cause);
        NettyUtil.closeChannel(channel);
    }
}
