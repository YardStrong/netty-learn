package online.yardstrong.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * 解码，非线程安全
 *
 * @author yuanqiang
 * @date 2021-10-14
 */
public class CustomDataDecoder extends ReplayingDecoder<CustomDataDecoder.CustomDataReadCommand> {

    /**
     * 缓存
     */
    CustomDataV1 dataCache;
    int dataContextLength;
    int dataBodyLength;

    public CustomDataDecoder() {
        super(CustomDataReadCommand.MAGIC);
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

        switch (state()) {
            case MAGIC:
                CustomDataV1.checkMagic(in.readByte());
                checkpoint(CustomDataReadCommand.VERSION);
            case VERSION:
                CustomDataV1.checkVersion(in.readByte());
                checkpoint(CustomDataReadCommand.OPAQUE);
            case OPAQUE:
                dataCache = new CustomDataV1();
                dataContextLength = 0;
                dataBodyLength = 0;
                dataCache.setOpaque(in.readLong());
                checkpoint(CustomDataReadCommand.COMMAND);
            case COMMAND:
                dataCache.setCommand(in.readByte());
                checkpoint(CustomDataReadCommand.CONTEXT_LENGTH);
            case CONTEXT_LENGTH:
                dataContextLength = in.readInt();
                checkpoint(CustomDataReadCommand.CONTEXT);
            case CONTEXT:
                if (0 != dataContextLength) {
                    byte[] context = new byte[dataContextLength];
                    in.readBytes(context);
                    dataCache.setByteArrayContext(context);
                }
                checkpoint(CustomDataReadCommand.BODY_LENGTH);
            case BODY_LENGTH:
                dataBodyLength = in.readInt();
                checkpoint(CustomDataReadCommand.BODY);
            case BODY:
                if (0 != dataBodyLength) {
                    byte[] body = new byte[dataBodyLength];
                    in.readBytes(body);
                    dataCache.setBody(body);
                }

                out.add(dataCache);
                checkpoint(CustomDataReadCommand.MAGIC);
        }
    }

    /**
     * 命令模式
     */
    public enum CustomDataReadCommand {
        /**
         * 标识码
         */
        MAGIC,
        /**
         * 版本
         */
        VERSION,
        /**
         * 唯一标识
         */
        OPAQUE,
        /**
         * 请求方式
         */
        COMMAND,
        /**
         * Context长度
         */
        CONTEXT_LENGTH,
        /**
         * 上下文
         */
        CONTEXT,
        /**
         * BODY长度
         */
        BODY_LENGTH,
        /**
         * 数据体
         */
        BODY
    }
}
