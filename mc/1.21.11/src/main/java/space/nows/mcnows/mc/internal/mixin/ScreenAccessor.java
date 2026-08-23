package space.nows.mcnows.mc.internal.mixin;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = Screen.class, remap = false)
public interface ScreenAccessor {
    @Invoker("addRenderableWidget")
    <T extends GuiEventListener & Renderable & NarratableEntry> T nows$addRenderableWidget(T widget);
}
