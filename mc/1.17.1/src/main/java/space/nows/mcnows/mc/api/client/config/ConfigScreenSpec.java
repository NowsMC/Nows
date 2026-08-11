package space.nows.mcnows.mc.api.client.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public record ConfigScreenSpec(
        Screen parent,
        Component title,
        List<ConfigCategorySpec> categories,
        Runnable savingRunnable
) {
}
