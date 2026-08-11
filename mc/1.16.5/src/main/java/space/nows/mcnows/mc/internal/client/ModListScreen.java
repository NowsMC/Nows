package space.nows.mcnows.mc.internal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.core.mod.ModContainer;
import space.nows.mcnows.core.mod.ModDependency;
import space.nows.mcnows.core.mod.ModDescriptor;
import space.nows.mcnows.mc.api.client.config.ConfigUi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public final class ModListScreen extends Screen {
    private static final ResourceLocation FALLBACK_ICON = ResourceLocation.tryParse("nows:textures/gui/mod_menu_icon.png");
    private static final int TOP = 36;
    private static final int BOTTOM = 34;
    private static final int GAP = 8;
    private static final int TEXT = 0xFFE7E7EF;
    private static final int MUTED = 0xFF9FA3AD;
    private static final int ACCENT = 0xFF8DBBFF;
    private static final int PANEL = 0xAA101217;
    private static final int PANEL_BORDER = 0xFF30343F;

    private final Screen parent;
    private final NowsContext context;

    private ModList modList;
    private ModDetailsPanel detailsPanel;

    public ModListScreen(Screen parent, NowsContext context) {
        super(new TextComponent("Nows Mods"));
        this.parent = parent;
        this.context = context;
    }

    @Override
    protected void init() {
        int contentTop = TOP;
        int contentHeight = Math.max(90, height - TOP - BOTTOM);
        int available = Math.max(220, width - 32);
        int totalWidth = Math.min(880, available);
        int left = (width - totalWidth) / 2;

        if (totalWidth < 520) {
            int listHeight = Math.max(90, Math.min(150, contentHeight / 3));
            int detailsHeight = Math.max(70, contentHeight - listHeight - GAP);
            modList = new ModList(minecraft, left, contentTop, totalWidth, listHeight, context);
            detailsPanel = new ModDetailsPanel(left, contentTop + listHeight + GAP, totalWidth, detailsHeight);
        } else {
            int listWidth = Math.min(260, Math.max(190, totalWidth / 3));
            int detailsWidth = totalWidth - listWidth - GAP;
            modList = new ModList(minecraft, left, contentTop, listWidth, contentHeight, context);
            detailsPanel = new ModDetailsPanel(left + listWidth + GAP, contentTop, detailsWidth, contentHeight);
        }
        detailsPanel.setMod(modList.selectedMod());

        children.add(modList);
        addButton(detailsPanel);
        addButton(new Button(
                width / 2 - 250,
                height - 26,
                120,
                20,
                new TextComponent("Configure"),
                button -> openSelectedConfig()));
        addButton(new Button(
                (width - 120) / 2,
                height - 26,
                120,
                20,
                new TextComponent("Done"),
                button -> onClose()));
    }

    @Override
    public void render(PoseStack graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        if (modList != null) {
            modList.render(graphics, mouseX, mouseY, delta);
        }
        super.render(graphics, mouseX, mouseY, delta);
        GuiComponent.drawCenteredString(graphics, font, getTitle(), width / 2, 10, 0xFFFFFFFF);
        GuiComponent.drawCenteredString(graphics, font,
                new TextComponent("Minecraft " + context.minecraftVersion()
                        + " / " + context.mods().size() + " Nows mod"
                        + (context.mods().size() == 1 ? "" : "s")),
                width / 2,
                21,
                0xFFB8B8C8);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void select(ModEntry entry) {
        modList.setSelected(entry);
        detailsPanel.setMod(entry.mod);
    }

    private void openSelectedConfig() {
        ModContainer selected = modList == null ? null : modList.selectedMod();
        if (selected == null) {
            return;
        }
        context.service(ConfigUi.class)
                .create(selected.descriptor().id(), this)
                .ifPresent(screen -> minecraft.setScreen(screen));
    }

    private final class ModList extends ObjectSelectionList<ModEntry> {
        private final int rowLeft;
        private final int rowWidth;

        ModList(Minecraft minecraft, int x, int y, int width, int height, NowsContext context) {
            super(minecraft, width, height, y, y + height, 42);
            setLeftPos(x);
            this.rowLeft = x + 4;
            this.rowWidth = width - 14;
            centerListVertically = false;
            List<ModEntry> entries = context.mods().stream()
                    .sorted(Comparator.comparing(mod -> mod.descriptor().id()))
                    .map(ModEntry::new)
                    .toList();
            replaceEntries(entries);
            if (!entries.isEmpty()) {
                setSelected(entries.get(0));
            }
        }

        ModContainer selectedMod() {
            ModEntry selected = getSelected();
            if (selected != null) {
                return selected.mod;
            }
            return children().isEmpty() ? null : children().get(0).mod;
        }

        @Override
        public int getRowLeft() {
            return rowLeft;
        }

        @Override
        public int getRowWidth() {
            return rowWidth;
        }

        @Override
        protected int getScrollbarPosition() {
            return x1 - 8;
        }
    }

    private final class ModEntry extends ObjectSelectionList.Entry<ModEntry> {
        private final ModContainer mod;
        private final ModDescriptor descriptor;

        ModEntry(ModContainer mod) {
            this.mod = mod;
            this.descriptor = mod.descriptor();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            select(this);
            return true;
        }

        @Override
        public void render(
                PoseStack graphics,
                int index,
                int top,
                int left,
                int width,
                int height,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta) {
            Font font = Minecraft.getInstance().font;
            int x = left + 6;
            int y = top + 7;
            ResourceLocation icon = icon(descriptor);
            Minecraft.getInstance().getTextureManager().bind(icon);
            GuiComponent.blit(graphics, x, y, 0, 0, 22, 22, 22, 22);
            GuiComponent.drawString(graphics, font, trim(font, descriptor.name(), width - 40), x + 30, y, TEXT);
            GuiComponent.drawString(graphics, font,
                    trim(font, descriptor.id() + " " + descriptor.version(), width - 40),
                    x + 30,
                    y + 12,
                    MUTED);
        }
    }

    private static final class ModDetailsPanel extends AbstractWidget {
        private ModContainer mod;
        private List<DetailLine> lines = List.of();
        private int contentHeight;

        ModDetailsPanel(int x, int y, int width, int height) {
            super(x, y, width, height, new TextComponent("Mod details"));
        }

        void setMod(ModContainer mod) {
            if (this.mod == mod) {
                return;
            }
            this.mod = mod;
            this.lines = buildLines(Minecraft.getInstance().font, width - 28, mod);
            this.contentHeight = calculateHeight(lines, width - 28) + 18;
        }

        private void renderBackground(PoseStack graphics) {
            GuiComponent.fill(graphics, x, y, x + getWidth(), y + getHeight(), PANEL);
            renderBorder(graphics, x, y, getWidth(), getHeight());
        }

        private void renderBorder(PoseStack graphics, int x, int y, int width, int height) {
            GuiComponent.fill(graphics, x, y, x + width, y + 1, PANEL_BORDER);
            GuiComponent.fill(graphics, x, y + height - 1, x + width, y + height, PANEL_BORDER);
            GuiComponent.fill(graphics, x, y, x + 1, y + height, PANEL_BORDER);
            GuiComponent.fill(graphics, x + width - 1, y, x + width, y + height, PANEL_BORDER);
        }

        @Override
        public void renderButton(PoseStack graphics, int mouseX, int mouseY, float delta) {
            renderBackground(graphics);
            int x = this.x + 12;
            int y = this.y + 10;
            for (DetailLine line : lines) {
                y = line.render(graphics, Minecraft.getInstance().font, x, y, getWidth() - 28);
            }
        }
    }

    private sealed interface DetailLine permits TextLine, WrappedLine, SpacerLine {
        int height(Font font, int width);

        int render(PoseStack graphics, Font font, int x, int y, int width);
    }

    private record TextLine(String text, int color, boolean shadow, int extraBottom) implements DetailLine {
        @Override
        public int height(Font font, int width) {
            return font.lineHeight + extraBottom;
        }

        @Override
        public int render(PoseStack graphics, Font font, int x, int y, int width) {
            GuiComponent.drawString(graphics, font, trim(font, text, width), x, y, color);
            return y + height(font, width);
        }
    }

    private record WrappedLine(String text, int color, int extraBottom) implements DetailLine {
        @Override
        public int height(Font font, int width) {
            return Math.max(font.lineHeight, font.split(new TextComponent(text), width).size() * font.lineHeight) + extraBottom;
        }

        @Override
        public int render(PoseStack graphics, Font font, int x, int y, int width) {
            for (var line : font.split(new TextComponent(text), width)) {
                font.draw(graphics, line, x, y, color);
                y += font.lineHeight;
            }
            return y + extraBottom;
        }
    }

    private record SpacerLine(int height) implements DetailLine {
        @Override
        public int height(Font font, int width) {
            return height;
        }

        @Override
        public int render(PoseStack graphics, Font font, int x, int y, int width) {
            return y + height;
        }
    }

    private static List<DetailLine> buildLines(Font font, int width, ModContainer mod) {
        if (mod == null) {
            return List.of(new TextLine("No mods loaded", MUTED, false, 0));
        }
        ModDescriptor descriptor = mod.descriptor();
        List<DetailLine> lines = new ArrayList<>();
        lines.add(new TextLine(descriptor.name(), 0xFFFFFFFF, true, 1));
        lines.add(new TextLine(descriptor.id() + " " + descriptor.version(), ACCENT, false, 6));
        if (!descriptor.description().isBlank()) {
            lines.add(new WrappedLine(descriptor.description(), TEXT, 8));
        }
        section(lines, "Identity");
        value(lines, "ID", descriptor.id());
        value(lines, "Name", descriptor.name());
        value(lines, "Version", descriptor.version());
        value(lines, "Minecraft", descriptor.minecraft());
        value(lines, "Side", descriptor.side().name().toLowerCase());
        value(lines, "Jar", mod.path().toString());
        value(lines, "Icon", descriptor.icon());
        section(lines, "People");
        values(lines, "Authors", descriptor.authors());
        values(lines, "Contributors", descriptor.contributors());
        values(lines, "Licenses", descriptor.licenses());
        section(lines, "Links");
        map(lines, descriptor.contacts());
        section(lines, "Compatibility");
        if (descriptor.dependencies().isEmpty()) {
            value(lines, "Dependencies", "none");
        } else {
            for (ModDependency dependency : descriptor.dependencies()) {
                lines.add(new WrappedLine(dependencyLabel(dependency), dependencyColor(dependency), 2));
            }
            lines.add(new SpacerLine(4));
        }
        section(lines, "Runtime Declarations");
        mapList(lines, descriptor.declarations());
        section(lines, "Properties");
        map(lines, descriptor.properties());
        if (!lines.isEmpty() && lines.get(lines.size() - 1) instanceof SpacerLine) {
            lines.remove(lines.size() - 1);
        }
        return lines;
    }

    private static void section(List<DetailLine> lines, String title) {
        lines.add(new SpacerLine(4));
        lines.add(new TextLine(title, 0xFFFFFFFF, true, 3));
    }

    private static void value(List<DetailLine> lines, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        lines.add(new WrappedLine(label + ": " + value, TEXT, 2));
    }

    private static void values(List<DetailLine> lines, String label, List<String> values) {
        value(lines, label, values.isEmpty() ? "none" : String.join(", ", values));
    }

    private static void map(List<DetailLine> lines, Map<String, String> values) {
        if (values.isEmpty()) {
            lines.add(new TextLine("none", MUTED, false, 2));
            return;
        }
        values.forEach((key, value) -> value(lines, key, value));
    }

    private static void mapList(List<DetailLine> lines, Map<String, List<String>> values) {
        if (values.isEmpty()) {
            lines.add(new TextLine("none", MUTED, false, 2));
            return;
        }
        values.forEach((key, list) -> value(lines, key, String.join(", ", list)));
    }

    private static String dependencyLabel(ModDependency dependency) {
        StringJoiner joiner = new StringJoiner(" ");
        joiner.add(kindLabel(dependency));
        joiner.add(dependency.id());
        if (!dependency.version().equals("*")) {
            joiner.add(dependency.version());
        }
        if (dependency.optional()) {
            joiner.add("(optional)");
        }
        if (!dependency.reason().isBlank()) {
            joiner.add("- " + dependency.reason());
        }
        return joiner.toString();
    }

    private static String kindLabel(ModDependency dependency) {
        if (dependency.required()) {
            return "requires";
        }
        if (dependency.conflict()) {
            return "incompatible with";
        }
        if (dependency.loadBefore()) {
            return "loads before";
        }
        if (dependency.loadAfter()) {
            return "loads after";
        }
        return dependency.kind();
    }

    private static int dependencyColor(ModDependency dependency) {
        if (dependency.conflict()) {
            return 0xFFFF9A9A;
        }
        if (dependency.orderOnly()) {
            return 0xFFFFD98D;
        }
        if (dependency.optional()) {
            return MUTED;
        }
        return TEXT;
    }

    private static int calculateHeight(List<DetailLine> lines, int width) {
        Font font = Minecraft.getInstance().font;
        int height = 0;
        for (DetailLine line : lines) {
            height += line.height(font, width);
        }
        return height;
    }

    private static ResourceLocation icon(ModDescriptor descriptor) {
        String icon = descriptor.icon();
        if (icon.startsWith("assets/")) {
            String rest = icon.substring("assets/".length());
            int slash = rest.indexOf('/');
            if (slash > 0 && slash + 1 < rest.length()) {
                return new ResourceLocation(rest.substring(0, slash), rest.substring(slash + 1));
            }
        }
        return FALLBACK_ICON;
    }

    private static String trim(Font font, String text, int width) {
        if (font.width(text) <= width) {
            return text;
        }
        return font.plainSubstrByWidth(text, Math.max(0, width - font.width("..."))) + "...";
    }
}
