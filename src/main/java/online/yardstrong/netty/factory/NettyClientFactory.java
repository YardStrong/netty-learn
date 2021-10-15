package online.yardstrong.netty.factory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import online.yardstrong.netty.handler.CustomClientHandler;

/**
 * Netty-Client
 * @author yardstrong
 */
public class NettyClientFactory {

    /**
     * 启动Netty客户端
     *
     * @param channelHandler 句柄
     * @param host           host
     * @param port           port
     */
    private static void startTimeClient(ChannelHandler channelHandler, String host, int port) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(channelHandler)
                    .option(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * 启动Netty客户端
     *
     * @param channelHandler 句柄
     * @param host           host
     * @param port           port
     */
    private static void startCustomClient(ChannelHandler channelHandler, String host, int port) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    /* whether keep alive */
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    /* whether tpc delay */
                    .option(ChannelOption.TCP_NODELAY, true)
                    /* send buffer size */
                    .option(ChannelOption.SO_SNDBUF, 65535)
                    /* receive buffer size */
                    .option(ChannelOption.SO_RCVBUF, 65535)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                    .handler(channelHandler);
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        startCustomClient(CustomClientHandler.channelInitializer(), "127.0.0.1", 8080);
    }
}
