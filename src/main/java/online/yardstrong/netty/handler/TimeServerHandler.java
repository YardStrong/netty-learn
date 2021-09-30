package online.yardstrong.netty.handler;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import online.yardstrong.netty.config.CustomNettyConfig;
import online.yardstrong.netty.utils.NettyByteBufUtil;

/**
 * Time Server
 * sharable注解的ChannelHandler必须是线程安全的
 *
 * @author yardstrong
 */
@ChannelHandler.Sharable
public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(TimeServerHandler.class);

    public static ChannelHandler channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline().addLast(new TimeServerHandler());
            }
        };
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final ChannelFuture channelFuture = ctx.writeAndFlush(NettyByteBufUtil.write(
                String.valueOf(System.currentTimeMillis()).getBytes(CustomNettyConfig.DEFAULT_CHARSET)));
        channelFuture.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }



    /**
     * channel write changed
     *
     * @param ctx channel handler context
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
        Channel ch = ctx.channel();
        ChannelConfig config = ch.config();

        if (!ch.isWritable()) {
            LOG.warn("{} is not writable, over high water level : {}", ch, config.getWriteBufferHighWaterMark());
            config.setAutoRead(false);
        } else {
            LOG.warn("{} is writable, to low water : {}", ch, config.getWriteBufferLowWaterMark());
            config.setAutoRead(true);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
