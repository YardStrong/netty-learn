package online.yardstrong.device;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceModel implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(DeviceModel.class);

    private final String identity;
    private final ChannelHandlerContext socket;

    private Status status = Status.Free;
    private final Queue<Task> execQueue = new LinkedList<>();
    private StringBuffer messageCache;
    private static final int CACHE_MAX_SIZE = 4096;
    private static final int QUEUE_MAX_SIZE = 5;

    public DeviceModel(String identity, ChannelHandlerContext socket) {
        this.identity = identity;
        this.socket = socket;
    }

    @Override
    public void onData(String message) {
        if (this.messageCache.length() < CACHE_MAX_SIZE) {
            this.messageCache.append(message);
        }
    }

    public void exec(String taskId, String command, Callback callback) {
        if (null == socket) {
            callback.call(new RuntimeException("Socket lost"), null);
            return;
        }
        if (this.execQueue.size() == 0 && Status.Free.equals(this.status)) {
            execDirect(taskId, command, callback);
            return;
        }
        if (this.execQueue.size() >= QUEUE_MAX_SIZE) {
            callback.call(new RuntimeException("Queue busy"), null);
            return;
        }
        log.info("Id: {}, task: {}, cmd: {} into queue", this.identity, taskId, command);
        // 进入等待队列 TODO 添加超时处理
        this.execQueue.add(new Task(taskId, command, callback));
    }

    private void execDirect(String taskId, String command, Callback callback) {
        if (null == socket) {
            callback.call(new RuntimeException("Socket lost"), null);
            return;
        }
        log.info("Id: {}, task: {}, cmd: {} ready to execute", this.identity, taskId, command);
        this.status = Status.Running;

        String prefix = taskId + "-start";
        String suffix = taskId + "-end";

        try {
            socket.writeAndFlush(prefix + command + suffix);
            // 等待返回
            waitResult(taskId, callback);
        } catch (Exception e) {
            callback.call(e, null);
        } finally {
            this.status = Status.Free;
        }

        execNext();
    }

    private void execNext() {
        if (execQueue.size() != 0 && Status.Free.equals(this.status)) {
            Task task = execQueue.poll();
            execDirect(task.taskId, task.command, task.callback);
        }
    }

    private void waitResult(String taskId, Callback callback) throws Exception {
        this.messageCache = new StringBuffer();
        int count = 0;

        Pattern resultPattern = Pattern.compile("(?i)" + taskId + "-start([\\s\\S]+?)" + taskId + "-end");
        do {
            Matcher matcher = resultPattern.matcher(this.messageCache.toString());
            if (matcher.find()) {
                callback.call(null, matcher.group(1));
                break;
            }
            Thread.sleep(50L);
        } while (count++ < 60);
    }

    private static class Task {
        String taskId;
        String command;
        Callback callback;
        Task(String taskId, String command, Callback callback) {
            this.taskId = taskId;
            this.command = command;
            this.callback = callback;
        }
    }


    private enum Status {
        Free,
        Running
    }

    public static interface Callback {
        void call(Throwable throwable, String message);
    }
}
