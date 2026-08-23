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

package space.nows.mc.internal.nbt;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import space.nows.mc.api.nbt.NbtApi;
import space.nows.mc.api.nbt.NbtCompound;
import space.nows.mc.api.nbt.NbtList;
import space.nows.mc.api.nbt.NbtValue;

public enum NbtApiImpl implements NbtApi {
    INSTANCE;

    @Override
    public CompoundTag compound() {
        return new CompoundTag();
    }

    @Override
    public ListTag list() {
        return new ListTag();
    }


    public CompoundTag compound(NbtCompound value) {
        CompoundTag tag = compound();
        for (var entry : value.values().entrySet()) {
            put(tag, entry.getKey(), tag(entry.getValue()));
        }
        return tag;
    }


    public ListTag list(NbtList value) {
        ListTag tag = list();
        for (NbtValue child : value.values()) {
            add(tag, tag(child));
        }
        return tag;
    }


    public Tag tag(NbtValue value) {
        if (value == null) {
            return stringTag("");
        }
        return switch (value.type()) {
            case END -> stringTag("");
            case BYTE -> byteTag((Byte) value.value());
            case SHORT -> shortTag((Short) value.value());
            case INT -> intTag((Integer) value.value());
            case LONG -> longTag((Long) value.value());
            case FLOAT -> floatTag((Float) value.value());
            case DOUBLE -> doubleTag((Double) value.value());
            case STRING -> stringTag((String) value.value());
            case LIST -> list((NbtList) value.value());
            case COMPOUND -> compound((NbtCompound) value.value());
        };
    }

    @Override
    public ByteTag byteTag(byte value) {
        return ByteTag.valueOf(value);
    }

    @Override
    public ByteTag booleanTag(boolean value) {
        return ByteTag.valueOf(value);
    }

    @Override
    public ShortTag shortTag(short value) {
        return ShortTag.valueOf(value);
    }

    @Override
    public IntTag intTag(int value) {
        return IntTag.valueOf(value);
    }

    @Override
    public LongTag longTag(long value) {
        return LongTag.valueOf(value);
    }

    @Override
    public FloatTag floatTag(float value) {
        return FloatTag.valueOf(value);
    }

    @Override
    public DoubleTag doubleTag(double value) {
        return DoubleTag.valueOf(value);
    }

    @Override
    public StringTag stringTag(String value) {
        return StringTag.valueOf(value);
    }

    @Override
    public void put(CompoundTag tag, String key, Tag value) {
        tag.put(key, value);
    }

    @Override
    public CompoundTag putCompound(CompoundTag tag, String key) {
        CompoundTag child = compound();
        tag.put(key, child);
        return child;
    }

    @Override
    public ListTag putList(CompoundTag tag, String key) {
        ListTag child = list();
        tag.put(key, child);
        return child;
    }

    @Override
    public void putString(CompoundTag tag, String key, String value) {
        tag.putString(key, value);
    }

    @Override
    public void putInt(CompoundTag tag, String key, int value) {
        tag.putInt(key, value);
    }

    @Override
    public void putLong(CompoundTag tag, String key, long value) {
        tag.putLong(key, value);
    }

    @Override
    public void putDouble(CompoundTag tag, String key, double value) {
        tag.putDouble(key, value);
    }

    @Override
    public void putBoolean(CompoundTag tag, String key, boolean value) {
        tag.putBoolean(key, value);
    }

    @Override
    public boolean contains(CompoundTag tag, String key) {
        return tag.contains(key);
    }

    @Override
    public void remove(CompoundTag tag, String key) {
        tag.remove(key);
    }

    @Override
    public CompoundTag copy(CompoundTag tag) {
        return tag.copy();
    }

    @Override
    public CompoundTag merge(CompoundTag target, CompoundTag source) {
        return target.merge(source);
    }

    @Override
    public String getString(CompoundTag tag, String key, String fallback) {
        return tag.contains(key) ? tag.getString(key) : fallback;
    }

    @Override
    public int getInt(CompoundTag tag, String key, int fallback) {
        return tag.contains(key) ? tag.getInt(key) : fallback;
    }

    @Override
    public long getLong(CompoundTag tag, String key, long fallback) {
        return tag.contains(key) ? tag.getLong(key) : fallback;
    }

    @Override
    public double getDouble(CompoundTag tag, String key, double fallback) {
        return tag.contains(key) ? tag.getDouble(key) : fallback;
    }

    @Override
    public boolean getBoolean(CompoundTag tag, String key, boolean fallback) {
        return tag.contains(key) ? tag.getBoolean(key) : fallback;
    }

    @Override
    public CompoundTag getCompound(CompoundTag tag, String key) {
        return tag.contains(key) ? tag.getCompound(key) : compound();
    }

    @Override
    public ListTag getList(CompoundTag tag, String key) {
        return tag.contains(key) ? tag.getList(key, END) : list();
    }

    @Override
    public void add(ListTag list, Tag value) {
        list.add(value);
    }

    @Override
    public void addString(ListTag list, String value) {
        list.add(stringTag(value));
    }

    @Override
    public void addInt(ListTag list, int value) {
        list.add(intTag(value));
    }

    @Override
    public void addCompound(ListTag list, CompoundTag value) {
        list.add(value);
    }

    @Override
    public String getString(ListTag list, int index, String fallback) {
        return index >= 0 && index < list.size() ? list.getString(index) : fallback;
    }

    @Override
    public int getInt(ListTag list, int index, int fallback) {
        return index >= 0 && index < list.size() ? list.getInt(index) : fallback;
    }

    @Override
    public CompoundTag getCompound(ListTag list, int index) {
        return index >= 0 && index < list.size() ? list.getCompound(index) : compound();
    }
}
