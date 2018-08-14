package cn.ceres.did.client;

import cn.ceres.did.sdk.SdkProto;
import cn.ceres.did.common.NettyUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ehlxr
 */
public abstract class AbstractClient implements Client {
    private Logger logger = LoggerFactory.getLogger(AbstractClient.class);

    private final Semaphore asyncSemaphore = new Semaphore(100000);
    private final Semaphore onewaySemaphore = new Semaphore(100000);

    ConcurrentMap<Integer, ResponseFuture> asyncResponse;
    NioEventLoopGroup workGroup;
    ChannelFuture channelFuture;
    Bootstrap bootstrap;

    public void init() {
        asyncResponse = new ConcurrentHashMap<>(16);
        workGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 10, new ThreadFactory() {
            private AtomicInteger index = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "WORK_" + index.incrementAndGet());
            }
        });

        bootstrap = new Bootstrap();
    }

    @Override
    public void shutdown() {
        if (workGroup != null) {
            workGroup.shutdownGracefully();
        }
    }


    @Override
    public SdkProto invokeSync(SdkProto sdkProto, long timeoutMillis) throws Exception {
        final Channel channel = channelFuture.channel();
        if (channel.isActive()) {
            final int rqid = sdkProto.getRqid();
            try {
                final ResponseFuture responseFuture = new ResponseFuture(rqid, timeoutMillis, null, null);
                asyncResponse.put(rqid, responseFuture);
                channel.writeAndFlush(sdkProto).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) {
                        if (channelFuture.isSuccess()) {
                            //发送成功后立即跳出
                            responseFuture.setIsSendStateOk(true);
                            return;
                        }
                        // 代码执行到此说明发送失败，需要释放资源
                        asyncResponse.remove(rqid);
                        responseFuture.putResponse(null);
                        responseFuture.setCause(channelFuture.cause());
                        logger.warn("send a request command to channel <" + NettyUtil.parseRemoteAddr(channel) + "> failed.");
                    }
                });
                // 阻塞等待响应
                SdkProto resultProto = responseFuture.waitResponse(timeoutMillis);
                if (null == resultProto) {
                    if (responseFuture.isSendStateOk()) {
                        throw new Exception(NettyUtil.parseRemoteAddr(channel) + timeoutMillis + responseFuture.getCause());
                    } else {
                        throw new Exception(NettyUtil.parseRemoteAddr(channel), responseFuture.getCause());
                    }
                }
                return resultProto;
            } catch (Exception e) {
                logger.error("invokeSync fail, addr is " + NettyUtil.parseRemoteAddr(channel), e);
                throw new Exception(NettyUtil.parseRemoteAddr(channel), e);
            } finally {
                asyncResponse.remove(rqid);
            }
        } else {
            NettyUtil.closeChannel(channel);
            throw new Exception(NettyUtil.parseRemoteAddr(channel));
        }
    }

    @Override
    public void invokeAsync(SdkProto sdkProto, long timeoutMillis, final InvokeCallback invokeCallback) throws Exception {
        final Channel channel = channelFuture.channel();
        if (channel.isOpen() && channel.isActive()) {
            final int rqid = sdkProto.getRqid();
            boolean acquired = asyncSemaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
            if (acquired) {
                final ResponseFuture responseFuture = new ResponseFuture(rqid, timeoutMillis, invokeCallback, asyncSemaphore);
                asyncResponse.put(rqid, responseFuture);
                try {
                    channelFuture.channel().writeAndFlush(sdkProto).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) {
                            if (channelFuture.isSuccess()) {
                                responseFuture.setIsSendStateOk(true);
                                return;
                            }
                            // 代码执行到些说明发送失败，需要释放资源
                            asyncResponse.remove(rqid);
                            responseFuture.setCause(channelFuture.cause());
                            responseFuture.putResponse(null);

                            try {
                                responseFuture.executeInvokeCallback();
                            } catch (Exception e) {
                                logger.warn("excute callback in writeAndFlush addListener, and callback throw", e);
                            } finally {
                                responseFuture.release();
                            }
                            logger.warn("send a request command to channel <" + NettyUtil.parseRemoteAddr(channel) + "> failed.", channelFuture.cause());
                        }
                    });
                } catch (Exception e) {
                    responseFuture.release();
                    logger.warn("send a request to channel <" + NettyUtil.parseRemoteAddr(channel) + "> Exception", e);
                    throw new Exception(NettyUtil.parseRemoteAddr(channel), e);
                }
            } else {
                String info = String.format("invokeAsyncImpl tryAcquire semaphore timeout, %dms, waiting thread " + "nums: %d semaphoreAsyncValue: %d",
                        timeoutMillis, this.asyncSemaphore.getQueueLength(), this.asyncSemaphore.availablePermits());
                logger.warn(info);
                throw new Exception(info);
            }
        } else {
            NettyUtil.closeChannel(channel);
            throw new Exception(NettyUtil.parseRemoteAddr(channel));
        }
    }

    @Override
    public void invokeOneWay(SdkProto sdkProto, long timeoutMillis) throws Exception {
        final Channel channel = channelFuture.channel();
        if (channel.isActive()) {
            final int rqid = sdkProto.getRqid();
            boolean acquired = onewaySemaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
            if (acquired) {
                try {
                    channelFuture.channel().writeAndFlush(sdkProto).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) {
                            onewaySemaphore.release();
                            if (!channelFuture.isSuccess()) {
                                logger.warn("send a request command to channel <" + NettyUtil.parseRemoteAddr(channel) + "> failed.");
                            }
                        }
                    });
                } catch (Exception e) {
                    logger.warn("send a request to channel <" + NettyUtil.parseRemoteAddr(channel) + "> Exception");
                    throw new Exception(NettyUtil.parseRemoteAddr(channel), e);
                } finally {
                    asyncResponse.remove(rqid);
                }
            } else {
                String info = String.format("invokeAsyncImpl tryAcquire semaphore timeout, %dms, waiting thread " + "nums: %d semaphoreAsyncValue: %d", timeoutMillis, this.asyncSemaphore.getQueueLength(), this.asyncSemaphore.availablePermits());
                logger.warn(info);
                throw new Exception(info);
            }
        } else {
            NettyUtil.closeChannel(channel);
            throw new Exception(NettyUtil.parseRemoteAddr(channel));
        }
    }
}
