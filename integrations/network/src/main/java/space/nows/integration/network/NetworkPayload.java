/*
 * Copyright 2026 TamKungZ_ (Nows MC — https://nows.space)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.nows.integration.network;

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
