package online.yardstrong.netty.handler;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import online.yardstrong.netty.config.CustomNettyConfig;
import online.yardstrong.netty.utils.NettyByteBufUtil;

/**
 * Time Server
 * @author yardstrong
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {

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
}
