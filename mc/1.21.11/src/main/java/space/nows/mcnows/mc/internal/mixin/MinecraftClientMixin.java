package space.nows.mcnows.mc.internal.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.nows.mcnows.mc.api.client.ui.ButtonSink;
import space.nows.mcnows.mc.api.client.ui.ScreenContext;
import space.nows.mcnows.mc.internal.client.IconButton;
import space.nows.mcnows.mc.internal.client.ModListScreen;
import space.nows.mcnows.mc.internal.client.SimpleScreen;
import space.nows.mcnows.mc.internal.client.UiImpl;
import space.nows.mcnows.mc.internal.event.GameEventsImpl;

@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftClientMixin {
    @Inject(method = "tick()V", at = @At("TAIL"), remap = false)
    private void nows$tickClient(CallbackInfo ci) {
        GameEventsImpl.INSTANCE.dispatchClientTick((Minecraft) (Object) this);
    }

    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("TAIL"), remap = false)
    private void nows$initTitleUi(Screen screen, CallbackInfo ci) {
        if (!(screen instanceof TitleScreen)) {
            return;
        }
        UiImpl.INSTANCE.titleScreenImpl().addButtons(
                new ScreenContext(
                        screen.width,
                        screen.height,
                        new ButtonSink() {
                            @Override
                            public void addButton(int x, int y, int width, int height, String message, Runnable onPress) {
                                ((ScreenAccessor) screen).nows$addRenderableWidget(
                                        Button.builder(Component.literal(message), button -> onPress.run())
                                                .bounds(x, y, width, height)
                                                .build());
                            }

                            @Override
                            public void addIconButton(
                                    int x, int y, int width, int height, String icon, String message, Runnable onPress) {
                                ((ScreenAccessor) screen).nows$addRenderableWidget(new IconButton(
                                        x,
                                        y,
                                        width,
                                        height,
                                        Identifier.tryParse(icon),
                                        Component.literal(message),
                                        onPress));
                            }
                        },
                        (title, initializer, renderer) -> Minecraft.getInstance().setScreen(
                                new SimpleScreen(Component.literal(title), initializer, renderer)),
                        () -> Minecraft.getInstance().setScreen(null),
                        context -> Minecraft.getInstance().setScreen(new ModListScreen(screen, context))));
    }
}
