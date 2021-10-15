package online.yardstrong.netty;

import online.yardstrong.netty.config.CustomNettyConfig;
import online.yardstrong.netty.factory.NettyServerFactory;

import java.util.Objects;

/**
 * 服务端启动类
 *
 * @author yardstrong
 */
public class CustomNettyServer {

    public static void main(String[] args) throws Exception {
        if (System.getProperties().containsKey(CustomNettyConfig.LOGBACK_CONFIG_FILE)) {
            System.setProperty(CustomNettyConfig.LOGBACK_CONFIG_FILE,
                    Objects.requireNonNull(
                            CustomNettyServer.class.getClassLoader().getResource("logback-server.xml")
                    ).getPath());
        }
        NettyServerFactory.customServer(CustomNettyConfig.DEFAULT_PORT);
    }
}
