package online.yardstrong.netty.factory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import online.yardstrong.netty.handler.TimeClientHandler;

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
    private static void startClient(ChannelHandler channelHandler, String host, int port) throws Exception {
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

    public static void main(String[] args) throws Exception {
        startClient(TimeClientHandler.channelHandler(), "127.0.0.1", 8080);
    }
}
