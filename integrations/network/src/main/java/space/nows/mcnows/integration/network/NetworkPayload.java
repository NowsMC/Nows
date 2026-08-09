package space.nows.mcnows.integration.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;
import java.util.Arrays;

/** Binary packet payload backed by Minecraft's Netty ByteBuf stack. */
public final class NetworkPayload {
    private static final NetworkPayload EMPTY = new NetworkPayload(Unpooled.EMPTY_BUFFER.asReadOnly());

    private final ByteBuf buffer;

    private NetworkPayload(ByteBuf buffer) {
        this.buffer = buffer.asReadOnly();
    }

    public int size() {
        return buffer.readableBytes();
    }

    public ByteBuf buffer() {
        return buffer.asReadOnly().slice(buffer.readerIndex(), buffer.readableBytes());
    }

    public byte[] bytes() {
        ByteBuf copy = buffer();
        byte[] bytes = new byte[copy.readableBytes()];
        copy.readBytes(bytes);
        return bytes;
    }

    public ByteBuffer byteBuffer() {
        return buffer().nioBuffer().asReadOnlyBuffer();
    }

    public static NetworkPayload empty() {
        return EMPTY;
    }

    public static NetworkPayload of(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return EMPTY;
        }
        return new NetworkPayload(Unpooled.wrappedBuffer(Arrays.copyOf(bytes, bytes.length)));
    }

    public static NetworkPayload of(ByteBuffer buffer) {
        if (buffer == null || !buffer.hasRemaining()) {
            return EMPTY;
        }
        return new NetworkPayload(Unpooled.copiedBuffer(buffer.slice()));
    }

    public static NetworkPayload of(ByteBuf buffer) {
        if (buffer == null || !buffer.isReadable()) {
            return EMPTY;
        }
        return new NetworkPayload(Unpooled.copiedBuffer(buffer.slice()));
    }

    public static NetworkPayload wrap(ByteBuf buffer) {
        if (buffer == null || !buffer.isReadable()) {
            return EMPTY;
        }
        return new NetworkPayload(buffer.slice());
    }
}
