package io.github.ehlxr.did.server.http;

import io.github.ehlxr.did.common.Constants;
import io.github.ehlxr.did.common.NettyUtil;
import io.github.ehlxr.did.common.Result;
import io.github.ehlxr.did.common.SdkProto;
import io.github.ehlxr.did.core.SnowFlake;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author ehlxr
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 通过信号量来控制流量
     */
    private final Semaphore semaphore = new Semaphore(Constants.HANDLE_HTTP_TPS);
    private final SnowFlake snowFlake;

    public HttpServerHandler(SnowFlake snowFlake) {
        this.snowFlake = snowFlake;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        Result<?> result;
        HttpResponseStatus status;

        if (!"/did".equals(request.uri())) {
            result = Result.fail(HttpResponseStatus.NOT_FOUND.code(), HttpResponseStatus.NOT_FOUND.reasonPhrase());
            response.setStatus(HttpResponseStatus.NOT_FOUND)
                    .content().writeBytes(result.toString().getBytes());

            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        if (semaphore.tryAcquire(Constants.ACQUIRE_TIMEOUTMILLIS, TimeUnit.MILLISECONDS)) {
            try {
                long id = snowFlake.nextId();
                logger.info("HttpServerHandler id is: {}", id);

                status = HttpResponseStatus.OK;
                result = Result.success(SdkProto.newBuilder().did(id).build());
            } catch (Exception e) {
                semaphore.release();
                logger.error("HttpServerHandler error", e);

                status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
                result = Result.fail(status.code(), status.reasonPhrase());
            }
        } else {
            String info = String.format("HttpServerHandler tryAcquire semaphore timeout, %dms, waiting thread " + "nums: %d availablePermit: %d",
                    Constants.ACQUIRE_TIMEOUTMILLIS, this.semaphore.getQueueLength(), this.semaphore.availablePermits());
            logger.warn(info);

            status = HttpResponseStatus.SERVICE_UNAVAILABLE;
            result = Result.fail(status.code(), info);
        }

        response.setStatus(status)
                .content().writeBytes(result.toString().getBytes());

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        logger.error("HttpServerHandler channel [{}] error and will be closed", NettyUtil.parseRemoteAddr(channel), cause);
        NettyUtil.closeChannel(channel);
    }
}