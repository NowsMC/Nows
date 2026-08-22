package space.nows.mcnows.mc.api.registry;

/** Stable item stack request translated to Minecraft ItemStack by each registry/player adapter. */
public record ItemStackSpec(String itemId, int count) {
    public ItemStackSpec {
        ItemSpec.requireId(itemId);
        if (count < 1 || count > 99) {
            throw new IllegalArgumentException("count must be between 1 and 99");
        }
    }

    public static ItemStackSpec of(String itemId) {
        return new ItemStackSpec(itemId, 1);
    }

    public static ItemStackSpec of(String itemId, int count) {
        return new ItemStackSpec(itemId, count);
    }
}
