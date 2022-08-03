package online.yardstrong.netty.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import online.yardstrong.netty.config.CustomNettyConfig;
import online.yardstrong.netty.utils.ExceptionUtil;

/**
 * 业务处理器
 * sharable注解的ChannelHandler必须是线程安全的（sharable注解并不会实现单例模式，需要自己手动实现）
 *
 * @author yardstrong
 */
@ChannelHandler.Sharable
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(HttpServerHandler.class);

    public static ChannelHandler channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline()
                        .addLast("decoder", new HttpRequestDecoder())
                        .addLast("encoder", new HttpResponseEncoder())
                        .addLast("aggregator", new HttpObjectAggregator(512 * 1024))
                        .addLast("handler", new HttpServerHandler());
            }
        };
    }


    /**
     * 业务逻辑处理
     *
     * @param channelHandlerContext 上下文
     * @param fullHttpRequest       请求
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer("{\"code\":200, \"message\": \"OK\"}".getBytes(CustomNettyConfig.DEFAULT_CHARSET)));

        HttpHeaders headers = response.headers();
        headers.add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + "; charset=" + CustomNettyConfig.DEFAULT_CHARSET.name());
        headers.add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        headers.add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext channelHandlerContext) throws Exception {
        super.channelReadComplete(channelHandlerContext);
        channelHandlerContext.flush();
    }

    /**
     * 异常捕捉
     *
     * @param channelHandlerContext 上下文
     * @param cause                 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
        LOG.error(ExceptionUtil.translateToString(cause));

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer("{\"code\":500, \"message\": \"SERVER_INNER_ERROR\"}".getBytes(CustomNettyConfig.DEFAULT_CHARSET)));

        HttpHeaders headers = response.headers();
        headers.add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + "; charset=UTF-8");
        headers.add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        headers.add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        channelHandlerContext.write(response);
    }
}
