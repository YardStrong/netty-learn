package online.yardstrong.netty.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import online.yardstrong.netty.config.CustomNettyConfig;

/**
 * 业务处理器
 * @author yardstrong
 */
public class CustomHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static ChannelHandler channelInitializer() {
        return new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline()
                        .addLast("decoder", new HttpRequestDecoder())
                        .addLast("encoder", new HttpResponseEncoder())
                        .addLast("aggregator", new HttpObjectAggregator(512 * 1024))
                        .addLast("handler", new CustomHttpHandler());
            }
        };
    }


    /**
     * 业务逻辑助理
     *
     * @param channelHandlerContext 上下文
     * @param fullHttpRequest       请求
     * @throws Exception 异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer("{\"code\":200, \"message\": \"OK\"}".getBytes(CustomNettyConfig.DEFAULT_CHARSET)));

        HttpHeaders headers = response.headers();
        headers.add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + "; charset=" + CustomNettyConfig.DEFAULT_CHARSET.name());
        headers.add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        headers.add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        channelHandlerContext.write(response);
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
     * @throws Exception 其他异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) throws Exception {
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
