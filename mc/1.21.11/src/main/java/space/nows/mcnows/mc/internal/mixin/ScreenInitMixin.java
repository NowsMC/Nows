package space.nows.mcnows.mc.internal.mixin;

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

@Mixin(value = Screen.class, remap = false)
public abstract class ScreenInitMixin {
    @Inject(method = "init(II)V", at = @At("TAIL"), remap = false)
    private void nows$initTitleUi(int width, int height, CallbackInfo ci) {
        if (!((Object) this instanceof TitleScreen)) {
            return;
        }
        Screen screen = (Screen) (Object) this;
        UiImpl.INSTANCE.titleScreenImpl().addButtons(
                new ScreenContext(
                        width,
                        height,
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
                        (title, initializer, renderer) -> net.minecraft.client.Minecraft.getInstance().setScreen(
                                new SimpleScreen(Component.literal(title), initializer, renderer)),
                        () -> net.minecraft.client.Minecraft.getInstance().setScreen(null),
                        context -> net.minecraft.client.Minecraft.getInstance().setScreen(
                                new ModListScreen(screen, context))));
    }
}
