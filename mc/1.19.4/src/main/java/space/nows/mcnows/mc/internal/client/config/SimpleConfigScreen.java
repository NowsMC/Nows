package space.nows.mcnows.mc.internal.client.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import space.nows.mcnows.mc.api.client.config.ConfigCategorySpec;
import space.nows.mcnows.mc.api.client.config.ConfigOptionSpec;
import space.nows.mcnows.mc.api.client.config.ConfigScreenSpec;

public final class SimpleConfigScreen extends Screen {
    private static final int TEXT = 0xFFE7E7EF;
    private static final int MUTED = 0xFF9FA3AD;

    private final Screen parent;
    private final ConfigScreenSpec spec;

    public SimpleConfigScreen(Screen parent, ConfigScreenSpec spec) {
        super(spec.title());
        this.parent = parent;
        this.spec = spec;
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(420, width - 32);
        int left = (width - contentWidth) / 2;
        int y = 38;
        for (ConfigCategorySpec category : spec.categories()) {
            y += 18;
            for (ConfigOptionSpec option : category.options()) {
                addOptionButton(left, y, contentWidth, option);
                y += 24;
            }
            y += 8;
        }
        addRenderableWidget(Button.builder(Component.literal("Save"), button -> saveAndClose())
                .bounds(width / 2 - 124, height - 28, 118, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> onClose())
                .bounds(width / 2 + 6, height - 28, 118, 20)
                .build());
    }

    @Override
    public void render(PoseStack graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        GuiComponent.drawCenteredString(graphics, font, getTitle(), width / 2, 12, 0xFFFFFFFF);
        int contentWidth = Math.min(420, width - 32);
        int left = (width - contentWidth) / 2;
        int y = 38;
        for (ConfigCategorySpec category : spec.categories()) {
            GuiComponent.drawString(graphics, font, category.title(), left, y, TEXT);
            y += 18 + category.options().size() * 24 + 8;
        }
        ConfigOptionSpec hovered = hoveredOption(mouseX, mouseY);
        if (hovered != null && hovered.tooltip() != null) {
            GuiComponent.drawString(graphics, font, hovered.tooltip(), left, height - 44, MUTED);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void saveAndClose() {
        for (ConfigCategorySpec category : spec.categories()) {
            for (ConfigOptionSpec option : category.options()) {
                option.save();
            }
        }
        spec.savingRunnable().run();
        onClose();
    }

    private void addOptionButton(int left, int y, int contentWidth, ConfigOptionSpec option) {
        if (option.type() == ConfigOptionSpec.Type.BOOLEAN) {
            addRenderableWidget(Button.builder(booleanLabel(option), button -> {
                        option.toggle();
                        button.setMessage(booleanLabel(option));
                    })
                    .bounds(left, y, contentWidth, 20)
                    .build());
            return;
        }
        addRenderableWidget(Button.builder(Component.literal("-"), button -> {
                    option.add(-1);
                    rebuildWidgets();
                })
                .bounds(left, y, 28, 20)
                .build());
        addRenderableWidget(Button.builder(intLabel(option), button -> {
                    option.reset();
                    rebuildWidgets();
                })
                .bounds(left + 32, y, contentWidth - 64, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> {
                    option.add(1);
                    rebuildWidgets();
                })
                .bounds(left + contentWidth - 28, y, 28, 20)
                .build());
    }

    private ConfigOptionSpec hoveredOption(int mouseX, int mouseY) {
        int contentWidth = Math.min(420, width - 32);
        int left = (width - contentWidth) / 2;
        int y = 56;
        for (ConfigCategorySpec category : spec.categories()) {
            for (ConfigOptionSpec option : category.options()) {
                if (mouseX >= left && mouseX <= left + contentWidth && mouseY >= y && mouseY <= y + 20) {
                    return option;
                }
                y += 24;
            }
            y += 26;
        }
        return null;
    }

    private static Component booleanLabel(ConfigOptionSpec option) {
        return Component.literal(option.label().getString() + ": " + (option.booleanValue() ? "ON" : "OFF"));
    }

    private static Component intLabel(ConfigOptionSpec option) {
        return Component.literal(option.label().getString() + ": " + option.intValue());
    }
}
