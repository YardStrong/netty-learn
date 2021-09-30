package online.yardstrong.netty;

import online.yardstrong.netty.config.CustomNettyConfig;
import online.yardstrong.netty.factory.NettyServerFactory;

/**
 * 启动类
 * @author yardstrong
 */
public class Main {
    public static void main(String[] args) throws Exception {
        NettyServerFactory.httpServer(CustomNettyConfig.DEFAULT_PORT);
    }
}
