package online.yardstrong.netty.factory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import online.yardstrong.netty.handler.CustomClientHandler;
import online.yardstrong.netty.handler.SSLClientHandler;
import online.yardstrong.netty.handler.TCPClientHandler;
import online.yardstrong.netty.utils.SSLContextUtil;

import java.io.InputStream;

/**
 * Netty-Client
 *
 * @author yardstrong
 */
public class NettyTCPClientFactory {

    /**
     * 启动Netty客户端
     *
     * @param channelHandler 句柄
     * @param host           host
     * @param port           port
     */
    private static void startSocketClient(ChannelHandler channelHandler, String host, int port) throws Exception {
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


    /**
     * custom client
     *
     * @param host host
     * @param port port
     * @throws Exception error
     */
    public static void customClient(String host, int port) throws Exception {
        startCustomClient(CustomClientHandler.channelInitializer(), host, port);
    }

    /**
     * tcp client
     *
     * @param host host
     * @param port port
     * @throws Exception error
     */
    public static void tcpClient(String host, int port) throws Exception {
        startSocketClient(TCPClientHandler.channelInitializer(), host, port);
    }

    /**
     * ssl tcp client
     *
     * @param host host
     * @param port port
     */
    public static void sslClient(String host, int port) throws Exception {
        try (InputStream clientKey = NettyTCPServerFactory.class.getResourceAsStream("ssl/client.keytab");
             InputStream clientTrust = NettyTCPServerFactory.class.getResourceAsStream("ssl/client.keytab")) {
            startSocketClient(SSLClientHandler.channelInitializer(SSLContextUtil.initSSLContextDoubleAuth(
                    clientKey, clientTrust, "clientPassDemo", "clientPassDemo")), host, port);
        }
    }
}
