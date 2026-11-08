package space.nows.fixture.consumer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.item.ItemStack;
import space.nows.integration.network.NetworkDirection;
import space.nows.integration.network.NowsNetworking;
import space.nows.mc.api.McBlockPos;
import space.nows.mc.api.McDirection;
import space.nows.mc.api.McId;
import space.nows.mc.api.McVec3;
import space.nows.mc.api.MinecraftApi;
import space.nows.mc.api.client.config.ConfigUi;
import space.nows.mc.api.client.keybind.KeybindApi;
import space.nows.mc.api.client.keybind.KeybindRegistration;
import space.nows.mc.api.client.ui.Ui;
import space.nows.mc.api.command.CommandApi;
import space.nows.mc.api.command.CommandArgumentSpec;
import space.nows.mc.api.command.CommandSpec;
import space.nows.mc.api.datagen.DataGen;
import space.nows.mc.api.datapack.DataPacks;
import space.nows.mc.api.datapack.PackTarget;
import space.nows.mc.api.event.GameEvents;
import space.nows.mc.api.nbt.NbtApi;
import space.nows.mc.api.nbt.NbtCompound;
import space.nows.mc.api.nbt.NbtList;
import space.nows.mc.api.nbt.NbtValue;
import space.nows.mc.api.recipe.IngredientSpec;
import space.nows.mc.api.recipe.RecipeSpec;
import space.nows.mc.api.recipe.RecipeViewerApi;
import space.nows.mc.api.registry.BlockEntry;
import space.nows.mc.api.registry.BlockMaterial;
import space.nows.mc.api.registry.BlockSpec;
import space.nows.mc.api.registry.ItemSpec;
import space.nows.mc.api.registry.ItemStackSpec;
import space.nows.mc.api.registry.McItemStack;
import space.nows.mc.api.registry.RegistryApi;
import space.nows.mc.api.text.McText;
import space.nows.mc.api.text.TextApi;
import space.nows.platform.api.ModInitializer;
import space.nows.platform.api.NowsContext;
import space.nows.platform.core.mod.ModDescriptor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Calls the documented Nows API the way an outside mod would and asserts an observable result for each call.
 * The same source is compiled for every adapter, so it only uses Nows-owned types in its own signatures.
 */
public final class ConsumerFixture implements ModInitializer {
    static final String ID = "nows_consumer_fixture";
    private static final String CHANNEL = ID + ":main";
    private static final int FINALIZE_AFTER_TICKS = 400;
    private static final int FINALIZE_HARD_LIMIT_TICKS = 3000;

    /** Areas that cannot be exercised from the main menu. They are reported, never silently dropped. */
    private static final Map<String, String> NOT_EXERCISABLE = Map.of(
            "networking.transport", "no adapter installs a NetworkTransport, so send() cannot deliver to another side",
            "players.current", "needs a loaded world",
            "events.serverTick", "needs a running integrated server",
            "events.levelTick", "needs a running integrated server",
            "registry.blockEntityAndMenu", "needs native block entity and menu classes that differ per Minecraft version");

    /** Every API area this fixture is responsible for. A catalogue entry with no result is a failure. */
    private static final List<String> CATALOGUE = List.of(
            "metadata.descriptor", "lifecycle.listener", "lifecycle.minecraftStarting", "config.files",
            "values.ids", "values.geometry", "text.component", "nbt.roundtrip",
            "registry.item", "registry.itemStack", "registry.block", "registry.blockItemStack",
            "registry.sound", "registry.effect",
            "command.dispatch", "datagen.json", "datagen.recipe", "datagen.recipeAccepted", "datapack.directories",
            "recipeViewer.catalyst", "recipeViewer.catalystStack", "keybind.register", "networking.channel", "networking.receive",
            "events.clientTick", "ui.titleScreen", "configUi.register", "configUi.screen",
            "networking.transport", "players.current", "events.serverTick", "events.levelTick",
            "registry.blockEntityAndMenu");

    private static final Map<String, String> RESULTS = new LinkedHashMap<>();
    private static final AtomicInteger TICKS = new AtomicInteger();
    private static volatile boolean titleScreenSeen;
    private static volatile boolean finished;
    private static boolean laterRan;
    private static NowsContext context;
    private static String adapter;

    @Override
    public void onInitialize(NowsContext ctx) {
        context = ctx;
        adapter = ctx.minecraftVersion();
        check("metadata.descriptor", () -> {
            ModDescriptor descriptor = ctx.requireModDescriptor(ID);
            eq("loaded", true, ctx.isModLoaded(ID));
            eq("name", "Nows Consumer Fixture", descriptor.name());
            eq("version", "1.0.0", descriptor.version());
            eq("expected-minecraft", Optional.of(ctx.minecraftVersion()), descriptor.property("expected-minecraft"));
            eq("client side", true, ctx.isClient());
            return "id=" + descriptor.id();
        });
        check("lifecycle.listener", () -> {
            eq("bootstrapReady fired before entrypoint", true, FixtureListener.bootstrapReady);
            eq("register fired before entrypoint", true, FixtureListener.register);
            return "declared listener received both events";
        });
        check("config.files", () -> {
            Properties written = new Properties();
            written.setProperty("alpha", "one=two");
            written.setProperty("unicode", "é中文");
            ctx.configs().saveProperties(ID, "roundtrip", written, "fixture");
            Properties read = ctx.configs().loadProperties(ID, "roundtrip");
            eq("alpha", "one=two", read.getProperty("alpha"));
            eq("unicode", "é中文", read.getProperty("unicode"));
            eq("size", 2, read.size());
            return "file=" + ctx.configs().file(ID, "roundtrip").getFileName();
        });
        check("values.ids", () -> {
            eq("namespace", "a", McId.of("a:b/c").namespace());
            eq("path", "b/c", McId.of("a:b/c").path());
            eq("default namespace", "minecraft", McId.of("stone").namespace());
            raises("uppercase id rejected", IllegalArgumentException.class, () -> McId.of("Bad Id"));
            return "ok";
        });
        check("values.geometry", () -> {
            eq("offset", McBlockPos.of(2, 3, 4), McBlockPos.of(1, 2, 3).offset(1, 1, 1));
            eq("center", McVec3.of(0.5D, 0.5D, 0.5D), McBlockPos.of(0, 0, 0).center());
            eq("relative up", McBlockPos.of(0, 1, 0), McBlockPos.of(0, 0, 0).relative(McDirection.UP));
            eq("step", 2, McDirection.EAST.stepX() + McDirection.UP.stepY());
            return "ok";
        });
        check("text.component", () -> {
            TextApi text = MinecraftApi.text(ctx);
            eq("literal", "hello fixture", text.component(McText.literal("hello fixture")).getString());
            String translated = text.component(McText.translatable("nows_consumer_fixture.missing", 1)).getString();
            eq("missing translation falls back to key", true, translated.contains("nows_consumer_fixture.missing"));
            return "ok";
        });
        check("nbt.roundtrip", () -> {
            NbtApi nbt = MinecraftApi.nbt(ctx);
            NbtCompound inner = new NbtCompound().putString("inner", "v").putLong("big", Long.MAX_VALUE);
            NbtList list = new NbtList().addString("s0").addString("s1");
            NbtCompound source = new NbtCompound()
                    .putString("name", "fixture")
                    .putInt("count", -42)
                    .putLong("long", 9_007_199_254_740_993L)
                    .putDouble("double", 0.1D)
                    .putBoolean("flag", true)
                    .put("nested", NbtValue.compound(inner))
                    .put("list", NbtValue.list(list));
            eq("name", "fixture", nbt.getString(nbt.compound(source), "name", "MISSING"));
            eq("count", -42, nbt.getInt(nbt.compound(source), "count", 0));
            eq("long keeps 2^53+1", 9_007_199_254_740_993L, nbt.getLong(nbt.compound(source), "long", 0L));
            eq("double", 0.1D, nbt.getDouble(nbt.compound(source), "double", 0.0D));
            eq("flag", true, nbt.getBoolean(nbt.compound(source), "flag", false));
            eq("nested.inner", "v", nbt.getString(nbt.getCompound(nbt.compound(source), "nested"), "inner", "MISSING"));
            eq("nested.big", Long.MAX_VALUE, nbt.getLong(nbt.getCompound(nbt.compound(source), "nested"), "big", 0L));
            eq("list[0]", "s0", nbt.getString(nbt.getList(nbt.compound(source), "list"), 0, "MISSING"));
            eq("list[1]", "s1", nbt.getString(nbt.getList(nbt.compound(source), "list"), 1, "MISSING"));
            eq("absent key uses default", "dflt", nbt.getString(nbt.compound(source), "absent", "dflt"));
            return "7 typed fields, nested compound and list";
        });
        // Registration is what a mod does in its entrypoint. Observation happens after Minecraft bootstrapped.
        Object[] widget = new Object[1];
        staged("registry.item", () -> {
            widget[0] = MinecraftApi.registries(ctx).registerItem(
                    ItemSpec.builder(ID + ":widget").maxStackSize(16).build());
            eq("registered", true, widget[0] != null);
            return "registered in entrypoint";
        }, () -> {
            RegistryApi registries = MinecraftApi.registries(ctx);
            eq("lookup returns the same instance", widget[0], registries.item(ID + ":widget").orElse(null));
            eq("unknown item absent", Optional.empty(), registries.item(ID + ":does_not_exist"));
            return "id=" + ID + ":widget";
        });
        stackObservation("registry.itemStack", () -> {
            RegistryApi registries = MinecraftApi.registries(ctx);
            ItemStack stack = registries.itemStack(ItemStackSpec.of(ID + ":widget", 3));
            eq("stack item", widget[0], stack.getItem());
            eq("stack count", 3, stack.getCount());
            eq("spec maxStackSize reaches the game item", 16, stack.getMaxStackSize());
            return "maxStackSize=16 on the stack";
        });
        BlockEntry[] machine = new BlockEntry[1];
        staged("registry.block", () -> {
            machine[0] = MinecraftApi.registries(ctx).registerBlockWithItem(BlockSpec.builder(ID + ":machine")
                    .material(BlockMaterial.METAL)
                    .strength(2.0F, 6.0F)
                    .requiresCorrectTool()
                    .item(ItemSpec.builder(ID + ":machine").maxStackSize(32).build())
                    .build());
            eq("block registered", true, machine[0].block() != null);
            return "registered in entrypoint";
        }, () -> {
            RegistryApi registries = MinecraftApi.registries(ctx);
            eq("block lookup", machine[0].block(), registries.block(ID + ":machine").orElse(null));
            eq("item lookup", machine[0].item(), registries.item(ID + ":machine").orElse(null));
            eq("block item points at block", machine[0].block(), machine[0].item().getBlock());
            return "id=" + ID + ":machine";
        });
        stackObservation("registry.blockItemStack", () -> {
            eq("item spec reaches the game item", 32, MinecraftApi.registries(ctx)
                    .itemStack(ItemStackSpec.of(ID + ":machine", 1)).getMaxStackSize());
            return "maxStackSize=32 on the stack";
        });
        staged("registry.sound", () -> {
            eq("registered", true, MinecraftApi.registries(ctx).registerVariableRangeSound(ID + ":ping") != null);
            return "registered in entrypoint";
        }, () -> "no later observation available through the API");
        staged("registry.effect", () -> {
            Object effect = MinecraftApi.registries(ctx).registerSimpleMobEffect(
                    ID + ":glow", net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 0x33CCFF);
            eq("registered", true, effect != null);
            return "registered in entrypoint";
        }, () -> "no later observation available through the API");
        staged("command.dispatch", () -> {
            CommandApi commands = MinecraftApi.commands(ctx);
            commands.register(CommandSpec.literal("nows_fixture")
                    .argument(CommandArgumentSpec.integer("n"))
                    .executes(command -> 42)
                    .build());
            eq("registration recorded", true, commands.registrations().size() >= 1);
            return "registered in entrypoint";
        }, () -> {
            // A real CommandSourceStack cannot be built without version-specific constructors, so assert the
            // Brigadier tree that the adapter produced instead of running the command.
            CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
            MinecraftApi.commands(ctx).applyTo(dispatcher);
            eq("literal present in dispatcher", true, dispatcher.getRoot().getChild("nows_fixture") != null);
            ParseResults<CommandSourceStack> withArgument = dispatcher.parse("nows_fixture 7", null);
            eq("no parse errors", true, withArgument.getExceptions().isEmpty());
            eq("input fully consumed", false, withArgument.getReader().canRead());
            eq("literal then argument node", 2, withArgument.getContext().getNodes().size());
            eq("integer argument parsed", 7, withArgument.getContext().getArguments().get("n").getResult());
            eq("command runs with the argument", true, withArgument.getContext().getCommand() != null);
            eq("required argument missing means not executable", true,
                    dispatcher.parse("nows_fixture", null).getContext().getCommand() == null);
            eq("non integer rejected", false,
                    dispatcher.parse("nows_fixture abc", null).getExceptions().isEmpty());
            return "tree=literal+integer(n)";
        });
        check("datagen.json", () -> {
            DataGen dataGen = MinecraftApi.dataGen(ctx);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("a", 1);
            payload.put("b", List.of("x", "y"));
            dataGen.writeJson(ID + "/probe.json", payload);
            Path file = dataGen.path(ID + "/probe.json");
            Object parsed = dataGen.moshi().adapter(Object.class)
                    .fromJson(Files.readString(file, StandardCharsets.UTF_8));
            eq("parsed back", Map.of("a", 1.0D, "b", List.of("x", "y")), parsed);
            dataGen.writeText(ID + "/probe.txt", "line1\nline2");
            eq("text exact", "line1\nline2", Files.readString(dataGen.path(ID + "/probe.txt"), StandardCharsets.UTF_8));
            return "file=" + file.getFileName();
        });
        check("datagen.recipe", () -> {
            DataGen dataGen = MinecraftApi.dataGen(ctx);
            RecipeSpec recipe = RecipeSpec.shapeless(ID + ":widget_recipe", McItemStack.of(ID + ":widget", 2))
                    .ingredient(IngredientSpec.item("minecraft:stone"))
                    .build();
            dataGen.writeRecipe(recipe);
            Path file = dataGen.path(dataGen.recipePath(ID + ":widget_recipe"));
            eq("recipe file written", true, Files.isRegularFile(file));
            String text = Files.readString(file, StandardCharsets.UTF_8);
            Object parsed = dataGen.moshi().adapter(Object.class).fromJson(text);
            eq("recipe type", "minecraft:crafting_shapeless", ((Map<?, ?>) parsed).get("type"));
            return "json=" + text.replaceAll("\\s+", "");
        });
        stackObservation("datagen.recipeAccepted", () -> {
            DataGen dataGen = MinecraftApi.dataGen(ctx);
            McItemStack stick = McItemStack.of("minecraft:stick", 2);
            Map<String, RecipeSpec> specs = new LinkedHashMap<>();
            specs.put("shapeless", RecipeSpec.shapeless(ID + ":shapeless", stick)
                    .ingredient(IngredientSpec.item("minecraft:stone")).build());
            specs.put("shapeless with several items", RecipeSpec.shapeless(ID + ":several", stick)
                    .ingredient(IngredientSpec.items(List.of("minecraft:stone", "minecraft:dirt"))).build());
            specs.put("shapeless with a tag", RecipeSpec.shapeless(ID + ":tagged", stick)
                    .ingredient(IngredientSpec.tag("minecraft:planks")).build());
            specs.put("shaped", RecipeSpec.shaped(ID + ":shaped", McItemStack.of("minecraft:stick", 4))
                    .pattern("AA").pattern("AA").key('A', IngredientSpec.item("minecraft:stone")).build());
            specs.put("smelting", RecipeSpec.smelting(ID + ":smelting", McItemStack.of("minecraft:stick"))
                    .ingredient(IngredientSpec.item("minecraft:stone")).experience(0.1F).cookingTime(200).build());
            specs.put("stonecutting", RecipeSpec.stonecutting(ID + ":cutting", stick)
                    .ingredient(IngredientSpec.item("minecraft:stone")).build());
            List<String> rejected = new ArrayList<>();
            for (Map.Entry<String, RecipeSpec> spec : specs.entrySet()) {
                String json = dataGen.jsonAdapter(Object.class).toJson(dataGen.recipeJson(spec.getValue()));
                String reason = RecipeParserProbe.rejectionByGame(json);
                if (reason != null) {
                    rejected.add(spec.getKey() + " -> " + reason);
                }
            }
            eq("recipes the game rejected", List.of(), rejected);
            return specs.size() + " recipe kinds accepted by the game's own parser";
        });
        check("datapack.directories", () -> {
            DataPacks packs = MinecraftApi.dataPacks(ctx);
            Path base = packs.modPackDirectory(ID);
            eq("data dir is the mod namespace under data", base.resolve("data").resolve(ID), packs.modDataDirectory(ID));
            eq("assets dir is the mod namespace under assets", base.resolve("assets").resolve(ID),
                    packs.modAssetsDirectory(ID));
            eq("under game dir", true, base.startsWith(packs.gameDirectory()));
            eq("server sources list", true, packs.sources(PackTarget.SERVER_DATA) != null);
            eq("client sources list", true, packs.sources(PackTarget.CLIENT_RESOURCES) != null);
            return "base=" + packs.gameDirectory().relativize(base).toString().replace('\\', '/');
        });
        staged("recipeViewer.catalyst", () -> {
            RecipeViewerApi viewer = MinecraftApi.recipeViewer(ctx);
            viewer.registerCategory(ID + ":cat", String.class, McText.literal("Fixture"),
                    McItemStack.of("minecraft:stone"), (recipe, layout) -> layout.build());
            viewer.registerCatalyst(ID + ":cat", "minecraft:stone");
            eq("category known", true, viewer.category(ID + ":cat").isPresent());
            return "category and catalyst registered in entrypoint";
        }, () -> {
            eq("category still known", true, MinecraftApi.recipeViewer(ctx).category(ID + ":cat").isPresent());
            return "ok";
        });
        stackObservation("recipeViewer.catalystStack", () -> {
            RecipeViewerApi viewer = MinecraftApi.recipeViewer(ctx);
            eq("one catalyst", 1, viewer.catalysts(ID + ":cat").size());
            eq("catalyst is the stone item", MinecraftApi.registries(ctx).item("minecraft:stone").orElse(null),
                    viewer.catalysts(ID + ":cat").get(0).getItem());
            eq("category icon resolves", true, viewer.category(ID + ":cat").get().icon() != null);
            return "catalyst and icon resolve to stacks";
        });
        check("keybind.register", () -> {
            KeybindApi keybinds = MinecraftApi.keybinds(ctx);
            String name = "key." + ID + ".probe";
            KeybindRegistration registration = keybinds.registerKeyboard(name, ID, 74);
            eq("lookup", true, keybinds.keybind(name).isPresent());
            eq("default key code", 74, registration.defaultKeyCode());
            eq("not pressed", false, registration.isDown());
            eq("no pending click", false, registration.consumeClick());
            eq("native mapping name", name, ((KeyMapping) registration.keyMapping()).getName());
            eq("listed", true, keybinds.keybinds().stream().anyMatch(k -> k.id().equals(name)));
            return "code=74";
        });
        check("networking.channel", () -> {
            NowsNetworking networking = NowsNetworking.service(ctx);
            eq("declared channel registered by the loader", true, networking.channel(CHANNEL).isPresent());
            return "channel=" + CHANNEL;
        });
        check("networking.receive", () -> {
            NowsNetworking networking = NowsNetworking.service(ctx);
            byte[][] seen = new byte[1][];
            networking.registerHandler(CHANNEL, NetworkDirection.CLIENTBOUND, (packet, payload) -> {
                ByteBuf buffer = payload.buffer();
                byte[] copy = new byte[buffer.readableBytes()];
                buffer.getBytes(buffer.readerIndex(), copy);
                seen[0] = copy;
            });
            int handlers = networking.receive(CHANNEL, NetworkDirection.CLIENTBOUND, new byte[]{9, 8, 7});
            eq("handlers invoked", 1, handlers);
            eq("payload bytes", List.of((byte) 9, (byte) 8, (byte) 7), List.of(seen[0][0], seen[0][1], seen[0][2]));
            eq("payload length", 3, seen[0].length);
            eq("no transport means canSend is false", false, networking.canSend(CHANNEL, NetworkDirection.SERVERBOUND));
            return "inbound dispatch only";
        });
        check("configUi.register", () -> {
            ConfigUi configUi = MinecraftApi.configUi(ctx);
            configUi.register(ID, parent -> configUi.screen(parent, McText.literal("Fixture Settings"))
                    .category(McText.literal("General"))
                    .booleanOption(McText.literal("Enabled"), true, true, McText.literal("tip"), value -> { })
                    .intOption(McText.literal("Cooldown"), 5, 5, 0, 10, McText.literal("tip"), value -> { })
                    .done()
                    .build());
            eq("registered", true, configUi.has(ID));
            return "factory registered";
        });

        GameEvents events = MinecraftApi.events(ctx);
        events.clientTick(() -> {
            int tick = TICKS.incrementAndGet();
            // Native item stacks need data components that newer versions bind only after resources loaded,
            // so observation waits for the title screen.
            if (titleScreenSeen && !laterRan) {
                laterRan = true;
                for (Runnable later : LATER) {
                    later.run();
                }
                check("configUi.screen", () -> {
                    Optional<Screen> screen = MinecraftApi.configUi(ctx).create(ID, null);
                    eq("screen created", true, screen.isPresent());
                    eq("screen title", "Fixture Settings", screen.get().getTitle().getString());
                    return "title=Fixture Settings";
                });
            }
            // Wait for the title screen (resource loading time varies), but never past the hard limit.
            if (((tick >= FINALIZE_AFTER_TICKS && titleScreenSeen) || tick >= FINALIZE_HARD_LIMIT_TICKS) && !finished) {
                finish();
            }
        });
        Ui ui = MinecraftApi.ui(ctx);
        ui.titleScreen().addButton(title -> {
            if (title.width() > 0 && title.height() > 0) {
                titleScreenSeen = true;
            }
        });
    }

    private static void finish() {
        finished = true;
        check("events.clientTick", () -> {
            eq("callback ran", true, TICKS.get() >= FINALIZE_AFTER_TICKS);
            return "ticks=" + TICKS.get();
        });
        check("ui.titleScreen", () -> {
            eq("title screen invoked the registered button factory with real dimensions", true, titleScreenSeen);
            return "seen";
        });
        check("lifecycle.minecraftStarting", () -> {
            eq("event fired", true, FixtureListener.minecraftStarting);
            return "seen";
        });
        int pass = 0;
        int fail = 0;
        int notExercised = 0;
        List<String> lines = new ArrayList<>();
        for (String api : CATALOGUE) {
            String result = RESULTS.get(api);
            if (result == null) {
                String reason = NOT_EXERCISABLE.get(api);
                result = reason != null ? "NOT_EXERCISED " + reason : "FAIL never exercised";
            }
            if (result.startsWith("PASS")) {
                pass++;
            } else if (result.startsWith("NOT_EXERCISED")) {
                notExercised++;
            } else {
                fail++; // FAIL and SKIPPED both count against the run
            }
            lines.add("NOWS_FIXTURE api=" + api + " adapter=" + adapter + " " + result);
        }
        lines.add("NOWS_FIXTURE_SUMMARY adapter=" + adapter + " total=" + CATALOGUE.size()
                + " pass=" + pass + " fail=" + fail + " notExercised=" + notExercised);
        for (String line : lines) {
            System.out.println(line);
        }
        try {
            Files.write(context.gameDirectory().resolve("nows-fixture-results.txt"), lines, StandardCharsets.UTF_8);
        } catch (java.io.IOException failure) {
            System.out.println("NOWS_FIXTURE_SUMMARY adapter=" + adapter + " fail=1 reason=cannot write results file: " + failure);
        }
    }

    private interface Check {
        String run() throws Exception;
    }

    private static final List<Runnable> LATER = new ArrayList<>();

    /**
     * Observes a native item stack once the main menu is up. Newer Minecraft versions bind item data components
     * only when a world is joined, so only that exact error is reported as not exercisable. Anything else fails.
     */
    private static void stackObservation(String api, Check check) {
        if (skippedByRequest(api)) {
            RESULTS.put(api, "SKIPPED requested with -Dnows.fixture.skip");
            return;
        }
        LATER.add(() -> {
            try {
                RESULTS.put(api, "PASS " + check.run());
            } catch (AssertionError failure) {
                RESULTS.put(api, "FAIL " + failure.getMessage());
            } catch (Throwable failure) {
                RESULTS.put(api, componentsUnbound(failure)
                        ? "NOT_EXERCISED Minecraft binds item components only after a world is joined"
                        : "FAIL " + describe(failure));
            }
        });
    }

    /** Runs {@code entry} now, as a mod entrypoint would, and {@code later} once the game has bootstrapped. */
    private static void staged(String api, Check entry, Check later) {
        if (skippedByRequest(api)) {
            RESULTS.put(api, "SKIPPED requested with -Dnows.fixture.skip");
            return;
        }
        String entryResult;
        try {
            entryResult = entry.run();
        } catch (Throwable failure) {
            RESULTS.put(api, "FAIL in entrypoint " + describe(failure));
            return;
        }
        LATER.add(() -> {
            try {
                RESULTS.put(api, "PASS " + entryResult + "; " + later.run());
            } catch (AssertionError failure) {
                RESULTS.put(api, "FAIL after bootstrap " + failure.getMessage());
            } catch (Throwable failure) {
                RESULTS.put(api, "FAIL after bootstrap " + describe(failure));
            }
        });
    }

    /** Diagnostic only: lets a baseline run keep going past a call that is known to crash the game. */
    private static boolean skippedByRequest(String api) {
        String requested = System.getProperty("nows.fixture.skip", "");
        return List.of(requested.split(",")).contains(api);
    }

    private static void check(String api, Check check) {
        if (skippedByRequest(api)) {
            RESULTS.put(api, "SKIPPED requested with -Dnows.fixture.skip");
            return;
        }
        String result;
        try {
            result = "PASS " + check.run();
        } catch (AssertionError failure) {
            result = "FAIL " + failure.getMessage();
        } catch (Throwable failure) {
            result = "FAIL " + describe(failure);
        }
        RESULTS.put(api, result.replace('\n', ' '));
    }

    private static boolean componentsUnbound(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof NullPointerException && String.valueOf(cause.getMessage()).contains("Components not bound yet")) {
                return true;
            }
            if (cause instanceof java.lang.reflect.InvocationTargetException target && target.getTargetException() != null
                    && target.getTargetException() != cause) {
                cause = target.getTargetException();
                if (componentsUnbound(cause)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Exception type, message and the first frames inside Nows or Minecraft, so a failure can be located. */
    private static String describe(Throwable failure) {
        StringBuilder text = new StringBuilder(failure.getClass().getName()).append(": ").append(failure.getMessage());
        int frames = 0;
        for (StackTraceElement frame : failure.getStackTrace()) {
            String owner = frame.getClassName();
            if ((owner.startsWith("space.nows.mc") || owner.startsWith("net.minecraft")) && frames++ < 4) {
                text.append(" @").append(owner.substring(owner.lastIndexOf('.') + 1))
                        .append('.').append(frame.getMethodName()).append(':').append(frame.getLineNumber());
            }
        }
        return text.toString();
    }

    private static void eq(String what, Object expected, Object actual) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(what + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void raises(String what, Class<? extends Throwable> type, Runnable action) {
        try {
            action.run();
        } catch (Throwable thrown) {
            if (type.isInstance(thrown)) {
                return;
            }
            throw new AssertionError(what + ": expected " + type.getSimpleName() + " but got " + thrown);
        }
        throw new AssertionError(what + ": expected " + type.getSimpleName() + " but nothing was thrown");
    }
}
