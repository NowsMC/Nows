package space.nows.mcnows.integration.network;

import java.nio.ByteBuffer;
import java.util.Arrays;

/** Immutable binary packet payload. */
public final class NetworkPayload {
    private static final NetworkPayload EMPTY = new NetworkPayload(new byte[0]);

    private final byte[] bytes;

    private NetworkPayload(byte[] bytes) {
        this.bytes = bytes;
    }

    public int size() {
        return bytes.length;
    }

    public byte[] bytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    public ByteBuffer buffer() {
        return ByteBuffer.wrap(bytes()).asReadOnlyBuffer();
    }

    public static NetworkPayload empty() {
        return EMPTY;
    }

    public static NetworkPayload of(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return EMPTY;
        }
        return new NetworkPayload(Arrays.copyOf(bytes, bytes.length));
    }

    public static NetworkPayload of(ByteBuffer buffer) {
        if (buffer == null || !buffer.hasRemaining()) {
            return EMPTY;
        }
        ByteBuffer copy = buffer.slice();
        byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return of(bytes);
    }
}
