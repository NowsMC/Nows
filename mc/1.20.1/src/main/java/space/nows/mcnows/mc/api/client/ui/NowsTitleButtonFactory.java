package space.nows.mcnows.mc.api.client.ui;

import net.minecraft.client.gui.components.Button;

@FunctionalInterface
public interface NowsTitleButtonFactory {
    Button create(NowsScreenContext context);
}
