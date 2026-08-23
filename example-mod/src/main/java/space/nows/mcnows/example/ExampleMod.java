package space.nows.mcnows.example;

import foo.zaaarf.geb.GEB;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import reactor.util.Logger;
import space.nows.mcnows.api.ModInitializer;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.integration.logging.NowsLog;
import space.nows.mcnows.integration.network.NetworkDirection;
import space.nows.mcnows.integration.network.NowsNetworking;
import space.nows.mcnows.mc.api.MinecraftApi;
import space.nows.mcnows.mc.api.registry.RegistryApi;

public final class ExampleMod implements ModInitializer {
    private static final Logger LOG = NowsLog.get(ExampleMod.class);
    private static boolean exampleFeatureEnabled = true;
    private static int exampleCooldown = 1000;

    @Override
    public void onInitialize(NowsContext context) {
        // Minecraft 26.x ships unobfuscated, so this is the runtime name directly.
        // Do not force Minecraft's singleton to initialize before Main.main starts.
        LOG.info("Nows Example: Mojang-named class = {}", Minecraft.class.getName());
        LOG.info("Nows Example: Mods = {}", context.mods().size());
        LOG.info("Nows Example: Runtime side = {}", context.side().metadataName());
        LOG.info("Nows Example: GEB = {}", context.service(GEB.class).getClass().getName());
        LOG.info("Nows Example: Minecraft registries API = {}",
                MinecraftApi.registries(context).getClass().getName());
        RegistryApi registries = MinecraftApi.registries(context);
        registries.registerVariableRangeSound("nows_example:ping");
        LOG.info("Nows Example: Nows pack directory = {}",
                MinecraftApi.dataPacks(context).nowsPackDirectory());
        MinecraftApi.configUi(context).register("nows_example", parent ->
                MinecraftApi.configUi(context)
                        .screen(parent, Component.literal("Nows Example Settings"))
                        .category(Component.literal("General"))
                        .booleanOption(
                                Component.literal("Example Feature"),
                                exampleFeatureEnabled,
                                true,
                                Component.literal("Toggle a sample mod setting."),
                                value -> exampleFeatureEnabled = value)
                        .intOption(
                                Component.literal("Example Cooldown"),
                                exampleCooldown,
                                1000,
                                0,
                                60000,
                                Component.literal("Sample cooldown in milliseconds."),
                                value -> exampleCooldown = value)
                        .done()
                        .saving(() -> LOG.info("Nows Example: saved config feature={} cooldown={}",
                                exampleFeatureEnabled, exampleCooldown))
                        .build());
        MinecraftApi.events(context).serverLevelTick((server, level) -> {
            if (level.getGameTime() % 6000L == 0L) {
                LOG.debug("Nows Example: server-level tick callback on {}", level.dimension());
            }
        });
        NowsNetworking networking = NowsNetworking.service(context);
        networking.registerHandler("nows_example:main", NetworkDirection.CLIENTBOUND, (packetContext, payload) ->
                LOG.info("Nows Example: network packet {} bytes on {}",
                        payload.size(), packetContext.channel()));
        LOG.info("Nows Example: Network channels = {}", networking.channels().size());
    }
}
