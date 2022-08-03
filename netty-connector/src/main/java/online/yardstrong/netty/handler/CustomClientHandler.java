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
import online.yardstrong.netty.utils.ExceptionUtil;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 客户端
 *
 * @author yuanqiang
 * @date 2021-10-15
 */
public class CustomClientHandler extends ChannelInboundHandlerAdapter {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(CustomClientHandler.class);

    private static class CustomDataEncoderSingleInstance {
        public static final CustomDataEncoder INSTANCE = new CustomDataEncoder();
    }
    public static ChannelHandler channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline()
                        .addLast(new IdleStateHandler(0, 10, 0, TimeUnit.SECONDS))
                        .addLast("decoder", new CustomDataDecoder())
                        .addLast("encoder", CustomClientHandler.CustomDataEncoderSingleInstance.INSTANCE)
                        .addLast("handler", new CustomClientHandler());
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
        if (evt instanceof IdleStateEvent) {
            CustomDataV1 data = new CustomDataV1();
            data.setCommand(CustomDataV1.Command.HEART_BEAT);
            data.setBody("heart_beat".getBytes(StandardCharsets.UTF_8));
            ctx.writeAndFlush(data).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            LOG.info("Send heart beat ...");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error(ExceptionUtil.translateToString(cause));
        ctx.close();
    }
}
