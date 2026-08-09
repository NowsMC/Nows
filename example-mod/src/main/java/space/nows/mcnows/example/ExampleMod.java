package space.nows.mcnows.example;

import foo.zaaarf.geb.GEB;
import net.minecraft.client.Minecraft;
import reactor.util.Logger;
import space.nows.mcnows.api.ModInitializer;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.integration.logging.NowsLog;
import space.nows.mcnows.integration.network.NetworkDirection;
import space.nows.mcnows.integration.network.NowsNetworking;

public final class ExampleMod implements ModInitializer {
    private static final Logger LOG = NowsLog.get(ExampleMod.class);

    @Override
    public void onInitialize(NowsContext context) {
        // Minecraft 26.x ships unobfuscated, so this is the runtime name directly.
        // Do not force Minecraft's singleton to initialize before Main.main starts.
        LOG.info("Nows Example: Mojang-named class = {}", Minecraft.class.getName());
        LOG.info("Nows Example: Mods = {}", context.mods().size());
        LOG.info("Nows Example: Runtime side = {}", context.side().metadataName());
        LOG.info("Nows Example: GEB = {}", context.service(GEB.class).getClass().getName());
        NowsNetworking networking = NowsNetworking.service(context);
        networking.registerHandler("nows_example:main", NetworkDirection.CLIENTBOUND, (packetContext, payload) ->
                LOG.info("Nows Example: network packet {} bytes on {}",
                        payload.size(), packetContext.channel()));
        LOG.info("Nows Example: Network channels = {}", networking.channels().size());
    }
}
