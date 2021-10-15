package online.yardstrong.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import online.yardstrong.netty.utils.ExceptionUtil;
import online.yardstrong.netty.utils.NettyByteBufUtil;

/**
 * Time-Client
 * sharable注解的ChannelHandler必须是线程安全的
 *
 * @author yardstrong
 */
@ChannelHandler.Sharable
public class TimeClientHandler extends ChannelInboundHandlerAdapter {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(TimeClientHandler.class);

    public static ChannelHandler channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new TimeClientHandler());
            }
        };
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            LOG.info("Time: " + NettyByteBufUtil.readStringAndRelease((ByteBuf) msg));
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error(ExceptionUtil.translateToString(cause));
        ctx.close();
    }



    /**
     * channel write changed
     *
     * @param ctx channel handler context
     */
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
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 心跳闲置状态事件
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
