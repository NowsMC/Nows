package space.nows.mcnows.example;

import foo.zaaarf.geb.GEB;
import net.minecraft.client.Minecraft;
import space.nows.mcnows.api.ModInitializer;
import space.nows.mcnows.api.NowsContext;

public final class ExampleMod implements ModInitializer {
    @Override
    public void onInitialize(NowsContext context) {
        // Minecraft 26.x ships unobfuscated, so this is the runtime name directly.
        // Do not force Minecraft's singleton to initialize before Main.main starts.
        System.out.println("[Nows Example] Mojang-named class = " + Minecraft.class.getName());
        System.out.println("[Nows Example] Mods = " + context.mods().size());
        System.out.println("[Nows Example] GEB = " + context.service(GEB.class).getClass().getName());
    }
}