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

package space.nows.mc.api.nbt;

/** Stable NBT value wrapper translated by each Minecraft adapter. */
public record NbtValue(Type type, Object value) {
    public enum Type {
        END,
        BYTE,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        STRING,
        LIST,
        COMPOUND
    }

    public static NbtValue end() {
        return new NbtValue(Type.END, null);
    }

    public static NbtValue byteValue(byte value) {
        return new NbtValue(Type.BYTE, value);
    }

    public static NbtValue bool(boolean value) {
        return byteValue((byte) (value ? 1 : 0));
    }

    public static NbtValue shortValue(short value) {
        return new NbtValue(Type.SHORT, value);
    }

    public static NbtValue integer(int value) {
        return new NbtValue(Type.INT, value);
    }

    public static NbtValue longValue(long value) {
        return new NbtValue(Type.LONG, value);
    }

    public static NbtValue floatValue(float value) {
        return new NbtValue(Type.FLOAT, value);
    }

    public static NbtValue doubleValue(double value) {
        return new NbtValue(Type.DOUBLE, value);
    }

    public static NbtValue string(String value) {
        return new NbtValue(Type.STRING, value == null ? "" : value);
    }

    public static NbtValue list(NbtList value) {
        return new NbtValue(Type.LIST, value == null ? new NbtList() : value);
    }

    public static NbtValue compound(NbtCompound value) {
        return new NbtValue(Type.COMPOUND, value == null ? new NbtCompound() : value);
    }
}
