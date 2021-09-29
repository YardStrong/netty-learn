package online.yardstrong.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;
import online.yardstrong.netty.utils.NettyByteBufUtil;

/**
 * Time-Client
 *
 * @author yardstrong
 */
public class TimeClientHandler extends ChannelInboundHandlerAdapter {

    public static ChannelHandler channelHandler() {
        return new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new TimeClientHandler());
            }
        };
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            System.out.println("Time: " + NettyByteBufUtil.readStringAndRelease((ByteBuf) msg));
            ctx.close();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
