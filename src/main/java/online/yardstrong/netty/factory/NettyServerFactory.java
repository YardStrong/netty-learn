package online.yardstrong.netty.factory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import online.yardstrong.netty.config.CustomNettyConfig;
import online.yardstrong.netty.handler.CustomDiscardHandler;
import online.yardstrong.netty.handler.CustomHttpHandler;
import online.yardstrong.netty.handler.TimeServerHandler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Netty server 工厂
 *
 * @author yardstrong
 */
public class NettyServerFactory {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(NettyServerFactory.class);

    /**
     * 启动服务
     *
     * @param channelHandler 句柄
     * @param port           端口
     * @throws Exception 异常
     */
    private static void startServer(ChannelHandler channelHandler, final int port) throws Exception {
        final int startPort = (port > 0) ? port : CustomNettyConfig.DEFAULT_PORT;

        // netty-boss线程工厂
        ThreadFactory bossThreadFactory = new ThreadFactory() {
            private final AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyServerBossThread_%d", this.threadIndex.incrementAndGet()));
            }
        };
        // netty-worker线程工厂
        ThreadFactory workerThreadFactory = new ThreadFactory() {
            private final AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyServerWorkerThread_%d", this.threadIndex.incrementAndGet()));
            }
        };

        // threads number
        final int workerThreadsNumber = CustomNettyConfig.WORKER_THREADS_NUMBER;
        LOG.info("Worker Threads Number: {}", workerThreadsNumber);

        // worker group
        final boolean nettyEpollEnable = CustomNettyConfig.NETTY_EPOLL_ENABLE;
        LOG.info("Netty Epoll enable: {}", nettyEpollEnable);
        EventLoopGroup bossGroup = nettyEpollEnable ?
                new EpollEventLoopGroup(1, workerThreadFactory) :
                new NioEventLoopGroup(1, workerThreadFactory);
        EventLoopGroup workerGroup = nettyEpollEnable ?
                new EpollEventLoopGroup(workerThreadsNumber, workerThreadFactory) :
                new NioEventLoopGroup(workerThreadsNumber, workerThreadFactory);

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(nettyEpollEnable ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    /* port can be used soon after released */
                    .option(ChannelOption.SO_REUSEADDR, true)
                    /* init the server connectable queue */
                    .option(ChannelOption.SO_BACKLOG, 128)
                    /* whether keep alive */
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    /* whether tpc delay */
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    /* send buffer size */
                    .childOption(ChannelOption.SO_SNDBUF, 65535)
                    /* receive buffer size */
                    .childOption(ChannelOption.SO_RCVBUF, 65535)
                    /* child handler */
                    .childHandler(channelHandler)
                    /* open keep alive */
                    .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);

            // Bind and start to accept incoming connections.
            ChannelFuture channelFuture;
            try {
                channelFuture = serverBootstrap.bind(startPort).sync();
            } catch (Exception e) {
                LOG.error("NettyRemotingServer bind fail {}, exit", e.getMessage(), e);
                throw new RuntimeException(String.format("NettyRemotingServer bind %s fail", startPort));
            }
            if (channelFuture.isSuccess()) {
                LOG.info("NettyRemotingServer bind success at port : {}", startPort);
            } else if (channelFuture.cause() != null) {
                throw new RuntimeException(String.format("NettyRemotingServer bind %s fail", startPort), channelFuture.cause());
            } else {
                throw new RuntimeException(String.format("NettyRemotingServer bind %s fail", startPort));
            }

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully shut down your server.
            ChannelFuture shutdownFuture = channelFuture.channel().closeFuture().sync();
            shutdownFuture.addListener((ChannelFutureListener) future -> {
                if (shutdownFuture == future) {
                    LOG.info("Server shutdown");
                }
            });
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }






    /**
     * http-server
     *
     * @param port port
     */
    public static void httpServer(int port) throws Exception {
        startServer(CustomHttpHandler.channelInitializer(), port);
    }

    /**
     * discord-server
     *
     * @param port port
     */
    public static void discordServer(int port) throws Exception {
        startServer(CustomDiscardHandler.channelInitializer(), port);
    }

    /**
     * time-server
     *
     * @param port port
     */
    public static void timeServer(int port) throws Exception {
        startServer(TimeServerHandler.channelInitializer(), port);
    }
}
