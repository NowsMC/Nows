package space.nows.mcnows.mc.api.client.config;

import net.minecraft.network.chat.Component;

import java.util.List;

public record ConfigCategorySpec(Component title, List<ConfigOptionSpec> options) {
}
