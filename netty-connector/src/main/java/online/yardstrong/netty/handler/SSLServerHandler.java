package online.yardstrong.netty.handler;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import online.yardstrong.netty.config.CustomNettyConfig;
import online.yardstrong.netty.utils.NettyByteBufUtil;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

/**
 * SSL Server Handler
 *
 * @date 2022-03-18
 */
public class SSLServerHandler extends SimpleChannelInboundHandler<String> {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(SSLServerHandler.class);

    public static ChannelHandler channelInitializer(SSLContext sslContext, boolean mutualAuth) {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {

                SSLEngine engine = sslContext.createSSLEngine();
                engine.setUseClientMode(false);
                if (mutualAuth) {
                    // 双向认证
                    engine.setNeedClientAuth(true);
                } else {
                    // 单向认证
                    engine.setNeedClientAuth(false);
                }

                ChannelPipeline pipeline = socketChannel.pipeline();

                pipeline.addLast("ssl", new SslHandler(engine));
                pipeline.addLast(new LineBasedFrameDecoder(1024));
                pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
                pipeline.addLast(new SSLServerHandler());
            }
        };
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        LOG.info("New client: {}", ctx.channel().remoteAddress());
        ctx.writeAndFlush(NettyByteBufUtil.write("hostname\n".getBytes(CustomNettyConfig.DEFAULT_CHARSET)));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ctx.writeAndFlush(NettyByteBufUtil.write("date\n".getBytes(CustomNettyConfig.DEFAULT_CHARSET)));
            }
        }, 0, 60000);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("Lost client: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        LOG.info("Lost client: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        ChannelConfig channelConfig = channel.config();

        if (!channel.isWritable()) {
            LOG.warn("{} is not writable, over high water level : {}", channel, channelConfig.getWriteBufferHighWaterMark());
            channelConfig.setAutoRead(false);
        } else {
            LOG.warn("{} is writable, to low water : {}", channel, channelConfig.getWriteBufferLowWaterMark());
            channelConfig.setAutoRead(true);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String message) {
        LOG.info("{}> {}", channelHandlerContext.channel().remoteAddress(), message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        LOG.error("Exception happened at client [" + remoteAddress.toString() + "]", cause);
        ctx.close();
    }
}
