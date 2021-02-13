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

package io.github.ehlxr.did.server.http;

import io.github.ehlxr.did.SdkProto;
import io.github.ehlxr.did.generator.SnowFlake;
import io.github.ehlxr.did.common.Constants;
import io.github.ehlxr.did.common.NettyUtil;
import io.github.ehlxr.did.common.Result;
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
        logger.debug("http server handler receive request {}", request);

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
            long id = snowFlake.nextId();

            status = HttpResponseStatus.OK;
            result = Result.success(SdkProto.newBuilder().did(id).build());
        } else {
            String info = String.format("HttpServerHandler tryAcquire semaphore timeout, %dms, waiting thread " + "nums: %d availablePermit: %d",
                    Constants.ACQUIRE_TIMEOUTMILLIS, this.semaphore.getQueueLength(), this.semaphore.availablePermits());
            logger.error(info);

            status = HttpResponseStatus.SERVICE_UNAVAILABLE;
            result = Result.fail(status.code(), info);
        }

        response.setStatus(status)
                .content().writeBytes(result.toString().getBytes());

        logger.debug("http server handler write response {} result {} to channel", status, result);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        logger.error("channel {} will be closed, 'cause of ", NettyUtil.parseRemoteAddr(channel), cause);
        NettyUtil.closeChannel(channel);
    }
}
