package online.yardstrong.netty;

import online.yardstrong.netty.config.CustomNettyConfig;
import online.yardstrong.netty.factory.NettyClientFactory;
import online.yardstrong.netty.utils.IpUtil;

import java.util.Objects;

/**
 * 客户端启动类
 *
 * @author yuanqiang
 * @date 2021-10-15
 */
public class CustomNettyClient {
    public static void main(String[] args) throws Exception {
        if (System.getProperties().containsKey(CustomNettyConfig.LOGBACK_CONFIG_FILE)) {
            System.setProperty(CustomNettyConfig.LOGBACK_CONFIG_FILE,
                    Objects.requireNonNull(
                            CustomNettyServer.class.getClassLoader().getResource("logback-client.xml")
                    ).getPath());
        }
        NettyClientFactory.customClient(IpUtil.getFirstNoLoopBackIpv4Address(), CustomNettyConfig.DEFAULT_PORT);
    }
}
