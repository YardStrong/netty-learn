package online.yardstrong.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码
 *
 * @author yuanqiang
 * @date 2021-10-14
 */
@ChannelHandler.Sharable
public class CustomDataEncoder extends MessageToByteEncoder<CustomDataV1> {
    @Override
    protected void encode(ChannelHandlerContext ctx, CustomDataV1 msg, ByteBuf out) {
        if (null == msg) {
            throw new RuntimeException("Encode msg is null");
        }

        out.writeByte(CustomDataV1.MAGIC);
        out.writeByte(CustomDataV1.VERSION);
        out.writeLong(msg.getOpaque());
        out.writeByte(msg.getCommandOrdinal());
        out.writeInt(msg.getContextLength());
        if (0 != msg.getContextLength()) {
            out.writeBytes(msg.getContext());
        }
        out.writeInt(msg.getBodyLength());
        if (0 != msg.getBodyLength()) {
            out.writeBytes(msg.getBody());
        }
    }
}
