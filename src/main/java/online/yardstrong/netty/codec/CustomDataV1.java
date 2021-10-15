package online.yardstrong.netty.codec;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 传输数据
 *
 * @author yuanqiang
 * @date 2021-10-15
 */
public class CustomDataV1 {

    private static final AtomicLong REQUEST_ID = new AtomicLong(1);

    /**
     * API标识码
     */
    public static final byte MAGIC = (byte) 0xbe;
    /**
     * API版本
     */
    public static final byte VERSION = 1;
    /**
     * API请求标识
     */
    private long opaque;
    /**
     * 命令
     */
    private Command command;
    /**
     * 上下文
     */
    private byte[] context;
    /**
     * 数据
     */
    private byte[] body;

    public CustomDataV1() {
        opaque = REQUEST_ID.getAndIncrement();
    }

    public long getOpaque() {
        return opaque;
    }

    public void setOpaque(long opaque) {
        this.opaque = opaque;
    }

    public Command getCommand() {
        return command;
    }

    public int getCommandOrdinal() {
        return command.ordinal();
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public void setCommand(byte command) {
        this.command = Command.valueOf(command);
    }

    public byte[] getContext() {
        return context;
    }

    public void setByteArrayContext(byte[] byteArrayContext) {
        this.context = byteArrayContext;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public int getContextLength() {
        return null != context ? context.length : 0;
    }

    public int getBodyLength() {
        return null != body ? body.length : 0;
    }

    /**
     * 校验标识码
     *
     * @param magic 标识码
     */
    public static void checkMagic(byte magic) {
        if (CustomDataV1.MAGIC != magic) {
            throw new IllegalArgumentException("Invalid magic: " + magic);
        }
    }

    /**
     * 校验版本
     *
     * @param version 版本
     */
    public static void checkVersion(byte version) {
        if (CustomDataV1.VERSION != version) {
            throw new IllegalArgumentException("Invalid version: " + version);
        }
    }

    /**
     * 请求指令
     */
    public enum Command {
        /**
         * HEART_BEAT
         */
        HEART_BEAT;


        public static Command valueOf(byte value) {
            for(Command ct : Command.values()){
                if(ct.ordinal() == value){
                    return ct;
                }
            }
            return null;
        }
    }


    @Override
    public String toString() {
        return "CustomDataV1{" +
                "opaque=" + opaque +
                ", command=" + command +
                ", context=" + Arrays.toString(context) +
                ", body=" + Arrays.toString(body) +
                '}';
    }
}
