package online.yardstrong.netty.handler;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import online.yardstrong.netty.codec.CustomDataDecoder;
import online.yardstrong.netty.codec.CustomDataEncoder;
import online.yardstrong.netty.codec.CustomDataV1;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 自定义服务
 *
 * @author yuanqiang
 * @date 2021-10-15
 */
@ChannelHandler.Sharable
public class CustomServerHandler extends ChannelInboundHandlerAdapter {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(CustomServerHandler.class);

    private static class CustomDataEncoderSingleInstance {
        public static final CustomDataEncoder INSTANCE = new CustomDataEncoder();
    }
    public static ChannelHandler channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline()
                        .addLast(new IdleStateHandler(0, 0, 1, TimeUnit.MINUTES))
                        .addLast("decoder", new CustomDataDecoder())
                        .addLast("encoder", CustomDataEncoderSingleInstance.INSTANCE)
                        .addLast("handler", new CustomServerHandler());
            }
        };
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            CustomDataV1 dataV1 = (CustomDataV1) msg;
            LOG.info("Receive message : {}", new String(dataV1.getBody()));
        } finally {
            ReferenceCountUtil.release(msg);
        }
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
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 心跳闲置状态事件
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        LOG.error("Exception happened at client [" + remoteAddress.toString() + "]", cause);
        ctx.close();
    }
}
