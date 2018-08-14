package cn.ceres.did.sdk;

import cn.ceres.did.common.NettyUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ehlxr
 */
public class SdkClientEncoder extends MessageToByteEncoder<SdkProto> {
    private static final Logger logger = LoggerFactory.getLogger(SdkClientEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, SdkProto sdkProto, ByteBuf out) {
        out.writeInt(sdkProto.getRqid());
        out.writeLong(sdkProto.getDid());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        logger.error(String.format("SdkServerEncoder channel [%s] error and will be closed", NettyUtil.parseRemoteAddr(channel)), cause);
        NettyUtil.closeChannel(channel);
    }
}
