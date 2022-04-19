package online.yardstrong.netty.handler;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import online.yardstrong.netty.config.CustomNettyConfig;
import online.yardstrong.netty.utils.IpUtil;
import online.yardstrong.netty.utils.NettyByteBufUtil;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Socket Client Handler
 */
public class TCPClientHandler extends SimpleChannelInboundHandler<String> {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(TCPClientHandler.class);


    public static ChannelHandler channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                ChannelPipeline pipeline = socketChannel.pipeline();

                pipeline.addLast(new LineBasedFrameDecoder(1024));
                pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
                pipeline.addLast(new TCPClientHandler());
            }
        };
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
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String message) throws Exception {
        LOG.info("{}> {}", channelHandlerContext.channel().remoteAddress(), message);
        if ("hostname".equals(message.trim())) {
            LOG.info("hostname-------------" + IpUtil.getLocalHost());
            channelHandlerContext.writeAndFlush(NettyByteBufUtil.write(IpUtil.getLocalHost()));
        } else if ("date".equals(message.trim())) {
            LOG.info("date-------------" + new Date().toString());
            channelHandlerContext.writeAndFlush(NettyByteBufUtil.write(new Date().toString()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        LOG.error("Exception happened at client [" + remoteAddress.toString() + "]", cause);
        ctx.close();
    }
}
