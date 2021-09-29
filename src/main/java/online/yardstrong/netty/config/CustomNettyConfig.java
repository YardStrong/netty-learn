package online.yardstrong.netty.config;

import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLoggerFactory;
import online.yardstrong.netty.utils.IpUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Netty 配置
 *
 * @author yardstrong
 */
public class CustomNettyConfig {

    /**
     *  cpus
     */
    public static final int CPUS = Runtime.getRuntime().availableProcessors();

    /**
     * number of netty worker threads
     */
    public static final int WORKER_THREADS_NUMBER = SystemPropertyUtil.getInt("io.netty.eventLoopThreads", CPUS);

    /**
     * default charset
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * default port
     */
    public static final int DEFAULT_PORT = 8080;

    /**
     * about： epoll、select、poll(blocking)
     * netty epoll enable switch
     */
    public static final boolean NETTY_EPOLL_ENABLE = SystemPropertyUtil.getBoolean("netty.epoll.enable", false);

    /**
     * local ipv4
     */
    public static final String LOCAL_IP = IpUtil.getFirstNoLoopBackIpv4Address();

    /**
     * OS Name
     */
    public static final String OS_NAME = System.getProperty("os.name");

    /**
     * 设置日志框架，netty默认的InternalLoggerFactory会自己查找当前引入的日志框架<br/>
     * 原生支持common-logging、jdk-logger、log4j、log4j2、slf4j五种框架<br/>
     * 其中common-logging被标记为@Deprecated
     *
     * @param defaultLogFactory log factory
     */
    public static void setDefaultLogFactory(InternalLoggerFactory defaultLogFactory) {
        InternalLoggerFactory.setDefaultFactory(defaultLogFactory);
    }
}