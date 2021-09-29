package online.yardstrong.netty.factory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
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

    /**
     * 启动服务
     *
     * @param channelHandler 句柄
     * @param port           端口
     * @throws Exception 异常
     */
    private static void startServer(ChannelHandler channelHandler, int port) throws Exception {
        if (port < 1) {
            port = CustomNettyConfig.DEFAULT_PORT;
        }

        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClient_%d", this.threadIndex.incrementAndGet()));
            }
        };
        EventLoopGroup workerGroup = CustomNettyConfig.NETTY_EPOLL_ENABLE ?
                new EpollEventLoopGroup(CustomNettyConfig.WORKER_THREADS_NUMBER, threadFactory) :
                new NioEventLoopGroup(CustomNettyConfig.WORKER_THREADS_NUMBER, threadFactory);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // determining the number of connections queued
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(channelHandler)
                    // open keep alive
                    .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);
            // Bind and start to accept incoming connections.
            ChannelFuture channelFuture = bootstrap.bind(port).sync();

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            channelFuture.channel().closeFuture().sync();
        } finally {
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
