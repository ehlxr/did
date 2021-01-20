package cn.ceres.did.server;

import cn.ceres.did.core.SnowFlake;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ehlxr
 */
public abstract class BaseServer implements Server {
    protected Logger logger = LoggerFactory.getLogger(BaseServer.class);

    protected DefaultEventLoopGroup defLoopGroup;
    protected NioEventLoopGroup bossGroup;
    protected NioEventLoopGroup workGroup;
    protected ChannelFuture channelFuture;
    protected ServerBootstrap serverBootstrap;
    protected int port;
    protected SnowFlake snowFlake;

    public void init() {
        defLoopGroup = new DefaultEventLoopGroup(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
            private final AtomicInteger index = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "DEFAULTEVENTLOOPGROUP_" + index.incrementAndGet());
            }
        });
        bossGroup = new NioEventLoopGroup(1, new ThreadFactory() {
            private final AtomicInteger index = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "BOSS_" + index.incrementAndGet());
            }
        });
        workGroup = new NioEventLoopGroup(new ThreadFactory() {
            private final AtomicInteger index = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "WORK_" + index.incrementAndGet());
            }
        });

        serverBootstrap = new ServerBootstrap();
    }

    @Override
    public void shutdown() {
        try {
            // 同步阻塞 shutdownGracefully 完成
            if (defLoopGroup != null) {
                defLoopGroup.shutdownGracefully().sync();
            }
            bossGroup.shutdownGracefully().sync();
            workGroup.shutdownGracefully().sync();
        } catch (Exception e) {
            logger.error("Server EventLoopGroup shutdown error.", e);
        }
    }
}
