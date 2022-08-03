package online.yardstrong.netty.utils;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工具
 *
 * @author yuanqiang
 * @date 2021-10-12
 */
public class ThreadUtil {

    private static final InternalLogger LOG = InternalLoggerFactory.getInstance(ThreadUtil.class);

    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    /**
     * newScheduledThreadPool
     * @param threadNamePrefix 线程名前缀
     * @param corePoolSize 线程池大小
     * @return 线程池
     */
    public static ScheduledExecutorService newScheduledThreadPool(String threadNamePrefix, int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize, new ThreadFactory() {
            final AtomicInteger count = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, threadNamePrefix + count.incrementAndGet());
            }
        });
    }

    /**
     * 打印死锁线程信息
     */
    public static void printDeadLockThreadInfo() {
        long[] deadLockedThreadIds = threadMXBean.findDeadlockedThreads();
        ThreadInfo[] deadLockThreads = threadMXBean.getThreadInfo(deadLockedThreadIds, false, false);
        LOG.debug("Total threads number: {}, dead lock thread number: {}", threadMXBean.getThreadCount(), deadLockedThreadIds.length);

        for (ThreadInfo threadInfo : deadLockThreads) {
            if (threadInfo != null) {
                LOG.info("ThreadId: {}, ThreadName: {}, ThreadStatus: {}, LockName: {}, LockOwner: {}, BlockedNum: {}, WaitNum: {}",
                        threadInfo.getThreadId(),
                        threadInfo.getThreadName(),
                        threadInfo.getThreadState().name(),
                        threadInfo.getLockName(),
                        threadInfo.getLockOwnerName(),
                        threadInfo.getBlockedCount(),
                        threadInfo.getWaitedCount());

                StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
                for (StackTraceElement stackTraceElement : stackTraceElements) {
                    LOG.info("Dead locked at {}.{}({}:{})", stackTraceElement.getClassName(), stackTraceElement.getMethodName(), stackTraceElement.getFileName(), stackTraceElement.getLineNumber());
                }
            }
        }
    }
}
