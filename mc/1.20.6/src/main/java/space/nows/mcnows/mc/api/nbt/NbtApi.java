package space.nows.mcnows.mc.api.nbt;

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
import space.nows.mcnows.mc.api.nbt.NbtCompound;
import space.nows.mcnows.mc.api.nbt.NbtList;
import space.nows.mcnows.mc.api.nbt.NbtValue;

/** Version-backed helpers for common NBT reads and writes. */
public interface NbtApi {
    byte END = 0;
    byte BYTE = 1;
    byte SHORT = 2;
    byte INT = 3;
    byte LONG = 4;
    byte FLOAT = 5;
    byte DOUBLE = 6;
    byte BYTE_ARRAY = 7;
    byte STRING = 8;
    byte LIST = 9;
    byte COMPOUND = 10;
    byte INT_ARRAY = 11;
    byte LONG_ARRAY = 12;

    CompoundTag compound();

    default NbtCompound stableCompound() {
        return new NbtCompound();
    }

    CompoundTag compound(NbtCompound value);


    ListTag list();

    default NbtList stableList() {
        return new NbtList();
    }

    ListTag list(NbtList value);

    Tag tag(NbtValue value);


    ByteTag byteTag(byte value);

    ByteTag booleanTag(boolean value);

    ShortTag shortTag(short value);

    IntTag intTag(int value);

    LongTag longTag(long value);

    FloatTag floatTag(float value);

    DoubleTag doubleTag(double value);

    StringTag stringTag(String value);

    void put(CompoundTag tag, String key, Tag value);

    CompoundTag putCompound(CompoundTag tag, String key);

    ListTag putList(CompoundTag tag, String key);

    void putString(CompoundTag tag, String key, String value);

    void putInt(CompoundTag tag, String key, int value);

    void putLong(CompoundTag tag, String key, long value);

    void putDouble(CompoundTag tag, String key, double value);

    void putBoolean(CompoundTag tag, String key, boolean value);

    boolean contains(CompoundTag tag, String key);

    void remove(CompoundTag tag, String key);

    CompoundTag copy(CompoundTag tag);

    CompoundTag merge(CompoundTag target, CompoundTag source);

    String getString(CompoundTag tag, String key, String fallback);

    int getInt(CompoundTag tag, String key, int fallback);

    long getLong(CompoundTag tag, String key, long fallback);

    double getDouble(CompoundTag tag, String key, double fallback);

    boolean getBoolean(CompoundTag tag, String key, boolean fallback);

    CompoundTag getCompound(CompoundTag tag, String key);

    ListTag getList(CompoundTag tag, String key);

    void add(ListTag list, Tag value);

    void addString(ListTag list, String value);

    void addInt(ListTag list, int value);

    void addCompound(ListTag list, CompoundTag value);

    String getString(ListTag list, int index, String fallback);

    int getInt(ListTag list, int index, int fallback);

    CompoundTag getCompound(ListTag list, int index);
}
