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

package space.nows.mc.internal.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.junit.jupiter.api.Test;
import space.nows.mc.api.McVec3;
import space.nows.mc.api.client.config.ConfigScreenBuilder;
import space.nows.mc.api.client.config.ConfigUi;
import space.nows.mc.api.client.keybind.KeybindApi;
import space.nows.mc.api.client.keybind.KeybindRegistration;
import space.nows.mc.api.client.player.PlayerApi;
import space.nows.mc.api.command.CommandApi;
import space.nows.mc.api.command.CommandSpec;
import space.nows.mc.api.datapack.DataPacks;
import space.nows.mc.api.datapack.PackTarget;
import space.nows.mc.api.event.GameEvents;
import space.nows.mc.api.menu.MenuSpec;
import space.nows.mc.api.client.ui.ScreenProgressSpec;
import space.nows.mc.api.recipe.WorkstationRecipeSpec;
import space.nows.mc.api.client.ui.ScreenSpec;
import space.nows.mc.api.menu.MenuSlotRule;
import space.nows.mc.api.menu.MenuSlotSpec;
import space.nows.mc.api.nbt.NbtApi;
import space.nows.mc.api.nbt.NbtCompound;
import space.nows.mc.api.nbt.NbtValue;
import space.nows.mc.api.recipe.IngredientSpec;
import space.nows.mc.api.recipe.RecipeViewerApi;
import space.nows.mc.api.recipe.RecipeViewerLayout;
import space.nows.mc.api.recipe.RecipeViewerLayoutFactory;
import space.nows.mc.api.registry.BlockEntry;
import space.nows.mc.api.registry.BlockMaterial;
import space.nows.mc.api.registry.BlockSpec;
import space.nows.mc.api.registry.ItemStackSpec;
import space.nows.mc.api.registry.ItemSpec;
import space.nows.mc.api.registry.McItemStack;
import space.nows.mc.api.registry.RegistryApi;
import space.nows.mc.api.text.McText;
import space.nows.mc.api.text.NativeTextBridge;
import space.nows.mc.api.text.TextApi;
import space.nows.mc.internal.client.config.ConfigUiImpl;
import space.nows.mc.internal.client.keybind.KeybindApiImpl;
import space.nows.mc.internal.client.player.PlayerApiImpl;
import space.nows.mc.internal.command.CommandApiImpl;
import space.nows.mc.internal.datapack.DataPacksImpl;
import space.nows.mc.internal.event.GameEventsImpl;
import space.nows.mc.internal.nbt.NbtApiImpl;
import space.nows.mc.internal.recipe.RecipeViewerApiImpl;
import space.nows.mc.internal.text.TextApiImpl;

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

    @Test
    void publicAndInternalApisExposeStableWrapperMethods() {
        assertDoesNotThrow(() -> ConfigUi.class.getMethod("screen", McText.class));
        assertDoesNotThrow(() -> ConfigUiImpl.class.getMethod("screen", McText.class));
        assertDoesNotThrow(() -> ConfigUi.class.getMethod("screen", net.minecraft.client.gui.screens.Screen.class, McText.class));
        assertDoesNotThrow(() -> ConfigUiImpl.class.getMethod("screen", net.minecraft.client.gui.screens.Screen.class, McText.class));
        assertDoesNotThrow(() -> KeybindApi.class.getMethod("registerKeyboard", String.class, String.class, int.class));
        assertDoesNotThrow(() -> KeybindApiImpl.class.getMethod("registerKeyboard", String.class, String.class, int.class));
        assertDoesNotThrow(() -> KeybindRegistration.class.getMethod("keyMapping"));
        assertDoesNotThrow(() -> PlayerApi.class.getMethod("setPosition", McVec3.class));
        assertDoesNotThrow(() -> PlayerApi.class.getMethod("addItem", ItemStackSpec.class));
        assertDoesNotThrow(() -> PlayerApiImpl.class.getMethod("setPosition", McVec3.class));
        assertDoesNotThrow(() -> PlayerApiImpl.class.getMethod("addItem", ItemStackSpec.class));
        assertDoesNotThrow(() -> CommandApi.class.getMethod("register", CommandSpec.class));
        assertDoesNotThrow(() -> CommandApiImpl.class.getMethod("register", CommandSpec.class));
        assertDoesNotThrow(() -> DataPacks.class.getMethod("sources", PackTarget.class));
        assertDoesNotThrow(() -> DataPacksImpl.class.getMethod("sources", PackTarget.class));
        assertDoesNotThrow(() -> GameEvents.class.getMethod("clientTick", Runnable.class));
        assertDoesNotThrow(() -> GameEventsImpl.class.getMethod("clientTick", Runnable.class));
        assertDoesNotThrow(() -> NbtApi.class.getMethod("compound", NbtCompound.class));
        assertDoesNotThrow(() -> NbtApiImpl.class.getMethod("compound", NbtCompound.class));
        assertDoesNotThrow(() -> TextApi.class.getMethod("component", McText.class));
        assertDoesNotThrow(() -> TextApiImpl.class.getMethod("component", McText.class));
        assertDoesNotThrow(() -> NativeTextBridge.class.getMethod("nativeComponent", McText.class, Class.class));
        assertDoesNotThrow(() -> RegistryApi.class.getMethod("itemStack", ItemStackSpec.class));
        assertDoesNotThrow(() -> RegistryApiImpl.class.getMethod("itemStack", ItemStackSpec.class));
        assertDoesNotThrow(() -> RecipeViewerApi.class.getMethod(
                "registerCategory",
                String.class,
                Class.class,
                McText.class,
                McItemStack.class,
                RecipeViewerLayoutFactory.class));
        assertDoesNotThrow(() -> RecipeViewerApiImpl.class.getMethod(
                "registerCategory",
                String.class,
                Class.class,
                McText.class,
                McItemStack.class,
                RecipeViewerLayoutFactory.class));
        assertDoesNotThrow(() -> RecipeViewerApi.class.getMethod("registerCatalyst", String.class, McItemStack.class));
        assertDoesNotThrow(() -> RecipeViewerLayout.Builder.class.getMethod("inputStack", int.class, int.class, McItemStack.class));
        assertDoesNotThrow(() -> RecipeViewerLayout.Builder.class.getMethod("output", int.class, int.class, McItemStack.class));
        assertDoesNotThrow(() -> RecipeViewerLayout.Builder.class.getMethod("catalyst", int.class, int.class, McItemStack.class));
        assertDoesNotThrow(() -> MenuSpec.class.getMethod("builder", String.class));
        assertDoesNotThrow(() -> ScreenSpec.class.getMethod("builder", String.class, McText.class, String.class));
        assertDoesNotThrow(() -> WorkstationRecipeSpec.class.getMethod("builder", String.class, String.class));
        assertDoesNotThrow(() -> MenuSlotSpec.class.getMethod("fuel", int.class, int.class, int.class));
        assertDoesNotThrow(() -> ScreenProgressSpec.class.getMethod(
                "horizontal",
                String.class,
                int.class,
                int.class,
                int.class,
                int.class,
                int.class,
                int.class));
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
        net.minecraft.world.item.ItemStack stack = api.itemStack(ItemStackSpec.of("minecraft:stone", 2));
    }

    @SuppressWarnings("unused")
    private static void stableWrapperCallsCompileAgainstEveryVersion(
            ConfigUi config,
            KeybindApi keybinds,
            PlayerApi player,
            CommandApi commands,
            DataPacks packs,
            GameEvents events,
            NbtApi nbt,
            TextApi text,
            RecipeViewerApi recipes
    ) throws Exception {
        ConfigScreenBuilder screen = config.screen(McText.literal("Options"))
                .category("General")
                .booleanOption("Enabled", true, true, "Toggle the feature", value -> {})
                .intOption("Amount", 4, 4, 0, 16, "Numeric value", value -> {})
                .done()
                .saving(() -> {});
        ConfigScreenBuilder parentScreen = config.screen(null, McText.literal("Options"));
        KeybindRegistration keybind = keybinds.registerKeyboard("key.nows.contract", "key.categories.nows", 65);
        Object nativeKeybind = keybind.keyMapping();
        McVec3 position = player.stablePosition();
        player.setPosition(McVec3.of(1.0D, 2.0D, 3.0D));
        player.setVelocity(McVec3.of(0.0D, 0.1D, 0.0D));
        player.addItem(ItemStackSpec.of("minecraft:stone", 1));
        player.sendSystemMessage(McText.literal("Hello"));
        commands.register(CommandSpec.literal("nows_contract").executes(() -> {}).build());
        java.nio.file.Path assets = packs.modAssetsDirectory("nows_contract");
        java.util.List<Object> sources = packs.sources(PackTarget.CLIENT_RESOURCES);
        events.clientTick(() -> {});
        NbtCompound compound = nbt.stableCompound()
                .putString("name", "contract")
                .putInt("value", 1);
        Object nativeNbt = nbt.tag(NbtValue.compound(compound));
        Object nativeText = text.component(McText.translatable("nows.contract"));
        recipes.registerCategory(
                "nows_contract",
                Object.class,
                McText.literal("Contract"),
                McItemStack.of("minecraft:stone"),
                (recipe, layout) -> layout
                        .inputStack(0, 0, McItemStack.of("minecraft:stone"))
                        .output(18, 0, McItemStack.of("minecraft:stone"))
                        .catalyst(36, 0, McItemStack.of("minecraft:crafting_table"))
                        .build());
        recipes.registerCatalyst("nows_contract", McItemStack.of("minecraft:stone"));
        MenuSpec menu = MenuSpec.builder("nows_contract:stove")
                .block("nows_contract:stove")
                .containerSlotCount(4)
                .input(0, 44, 17)
                .fuel(1, 44, 49)
                .output(2, 116, 17)
                .byproduct(3, 116, 49)
                .data("lit_time", 0)
                .data("lit_duration", 1)
                .data("cooking_progress", 2)
                .data("cooking_total_time", 3)
                .build();
        ScreenSpec screenSpec = ScreenSpec
                .builder("nows_contract:stove", McText.translatable("screen.nows_contract.stove"), "nows_contract:textures/gui/stove.png")
                .progress(ScreenProgressSpec.horizontal("cooking", 79, 35, 24, 17, 2, 3))
                .build();
        WorkstationRecipeSpec workstationRecipe = WorkstationRecipeSpec.builder("nows_contract:fried_stone", "nows_contract:stove_cooking")
                .ingredient(IngredientSpec.item("minecraft:stone"))
                .fuel(IngredientSpec.tag("minecraft:logs_that_burn"))
                .result(McItemStack.of("minecraft:smooth_stone"))
                .byproduct(McItemStack.of("minecraft:charcoal"), 0.25F)
                .experience(0.35F)
                .cookingTime(100)
                .combined(false)
                .build();
        MenuSlotRule outputRule = menu.slots().get(2).rule();
    }
}
