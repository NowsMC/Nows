package space.nows.mcnows.example;

import foo.zaaarf.geb.GEB;
import net.minecraft.client.Minecraft;
import reactor.util.Logger;
import space.nows.mcnows.api.ModInitializer;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.integration.logging.NowsLog;

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
    }
}
