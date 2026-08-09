package space.nows.mcnows.mc.internal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.core.mod.ModContainer;
import space.nows.mcnows.core.mod.ModDescriptor;

import java.util.Comparator;
import java.util.List;

public final class NowsModListScreen extends Screen {
    private static final Identifier ICON = Identifier.parse("nows_api_mod:icon.png");

    private final Screen parent;
    private final NowsContext context;

    public NowsModListScreen(Screen parent, NowsContext context) {
        super(Component.literal("Nows Mods"));
        this.parent = parent;
        this.context = context;
    }

    @Override
    protected void init() {
        addRenderableWidget(new ModList(minecraft, width, height - 74, 34, context));
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds((width - 120) / 2, height - 28, 120, 20)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, getTitle(), width / 2, 12, 0xFFFFFFFF);
        graphics.centeredText(font,
                Component.literal("Minecraft " + context.minecraftVersion()
                        + " / API " + context.requireModDescriptor("nows_api_mod").version()),
                width / 2,
                23,
                0xFFB8B8C8);
    }

    @Override
    public void onClose() {
        minecraft.setScreenAndShow(parent);
    }

    private static final class ModList extends ObjectSelectionList<ModEntry> {
        ModList(Minecraft minecraft, int width, int height, int y, NowsContext context) {
            super(minecraft, width, height, y, 36);
            centerListVertically = false;
            List<ModEntry> entries = context.mods().stream()
                    .sorted(Comparator.comparing(mod -> mod.descriptor().id()))
                    .map(ModEntry::new)
                    .toList();
            replaceEntries(entries);
        }

        @Override
        public int getRowWidth() {
            return Math.min(420, getWidth() - 80);
        }

        @Override
        protected int scrollBarX() {
            return getWidth() / 2 + getRowWidth() / 2 + 8;
        }
    }

    private static final class ModEntry extends ObjectSelectionList.Entry<ModEntry> {
        private final ModDescriptor descriptor;

        ModEntry(ModContainer mod) {
            this.descriptor = mod.descriptor();
        }

        @Override
        public Component getNarration() {
            return Component.literal(descriptor.name() + " " + descriptor.version());
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
            Font font = Minecraft.getInstance().font;
            int x = getContentX() + 6;
            int y = getContentY() + 6;
            graphics.blit(RenderPipelines.GUI_TEXTURED, ICON, x, y, 0, 0, 20, 20, 20, 20);
            graphics.text(font, descriptor.name(), x + 28, y + 1, 0xFFFFFFFF, true);
            graphics.text(font, descriptor.id() + " " + descriptor.version(), x + 28, y + 12, 0xFF9FA3AD, true);
        }
    }
}
