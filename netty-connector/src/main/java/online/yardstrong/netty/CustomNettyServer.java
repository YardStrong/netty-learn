package online.yardstrong.netty;

import online.yardstrong.netty.config.CustomNettyConfig;
import online.yardstrong.netty.factory.NettyTCPServerFactory;

import java.util.Objects;

/**
 * 服务端启动类
 *
 * @author yardstrong
 */
public class CustomNettyServer {

    public static void main(String[] args) throws Exception {
        if (!System.getProperties().containsKey(CustomNettyConfig.LOGBACK_CONFIG_FILE)) {
            System.setProperty(CustomNettyConfig.LOGBACK_CONFIG_FILE, "classpath:config/logback-server.xml");
        }
        NettyTCPServerFactory.sslServer(CustomNettyConfig.DEFAULT_PORT, false);
    }
}
