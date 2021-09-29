package online.yardstrong.netty.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author yardstrong
 */
public class NettyByteBufUtil {
    /**
     * 读取ByteBuf
     *
     * @param in ByteBuf
     * @return byte[]
     */
    public static byte[] readBytesAndRelease(ByteBuf in) {
        byte[] byteArray = new byte[in.readableBytes()];
        in.readBytes(byteArray);
        return byteArray;
    }

    /**
     * 读取ByteBuf
     *
     * @param in ByteBuf
     * @return string
     */
    public static String readStringAndRelease(ByteBuf in) {
        byte[] byteArray = new byte[in.readableBytes()];
        in.readBytes(byteArray);
        return new String(byteArray);
    }

    /**
     * 写入ByteBuf
     *
     * @param data byte[]
     * @return ByteBuf
     */
    public static ByteBuf write(byte[] data) {
        return Unpooled.copiedBuffer(data);
    }
}
