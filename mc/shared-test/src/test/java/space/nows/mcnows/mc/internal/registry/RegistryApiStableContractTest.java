package space.nows.mcnows.mc.internal.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.junit.jupiter.api.Test;
import space.nows.mcnows.mc.api.registry.BlockEntry;
import space.nows.mcnows.mc.api.registry.BlockMaterial;
import space.nows.mcnows.mc.api.registry.BlockSpec;
import space.nows.mcnows.mc.api.registry.ItemSpec;
import space.nows.mcnows.mc.api.registry.RegistryApi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

class RegistryApiStableContractTest {
    @Test
    void publicRegistryApiIsBackedByInternalImplementation() {
        RegistryApi api = new RegistryApiImpl();

        assertSame(RegistryApiImpl.class, api.getClass());
    }

    @Test
    void publicAndInternalRegistryApisExposeStableItemAndBlockSpecMethods() {
        assertDoesNotThrow(() -> RegistryApi.class.getMethod("registerItem", ItemSpec.class));
        assertDoesNotThrow(() -> RegistryApi.class.getMethod("registerBlock", BlockSpec.class));
        assertDoesNotThrow(() -> RegistryApi.class.getMethod("registerBlockWithItem", BlockSpec.class));
        assertDoesNotThrow(() -> RegistryApiImpl.class.getMethod("registerItem", ItemSpec.class));
        assertDoesNotThrow(() -> RegistryApiImpl.class.getMethod("registerBlock", BlockSpec.class));
        assertDoesNotThrow(() -> RegistryApiImpl.class.getMethod("registerBlockWithItem", BlockSpec.class));
    }

    @SuppressWarnings("unused")
    private static void stableItemAndBlockSpecCallsCompileAgainstEveryVersion(RegistryApi api) {
        Item item = api.registerItem(ItemSpec.builder("nows:contract_item")
                .maxStackSize(16)
                .fireResistant()
                .build());
        Block block = api.registerBlock(BlockSpec.builder("nows:contract_block")
                .material(BlockMaterial.METAL)
                .strength(2.0F, 6.0F)
                .requiresCorrectTool()
                .noOcclusion()
                .build());
        BlockEntry entry = api.registerBlockWithItem(BlockSpec.builder("nows:contract_block_with_item")
                .strength(1.5F, 3.0F)
                .item(ItemSpec.builder("nows:contract_block_with_item").maxStackSize(32).build())
                .build());
    }
}
