package space.nows.mcnows.mc.api.client.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import space.nows.mcnows.mc.api.text.McText;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

public final class ConfigScreenBuilder {
    private final Screen parent;
    private final Component title;
    private final Function<ConfigScreenSpec, Screen> factory;
    private final List<ConfigCategorySpec> categories = new ArrayList<>();
    private Runnable savingRunnable = () -> {};

    public ConfigScreenBuilder(Screen parent, Component title, Function<ConfigScreenSpec, Screen> factory) {
        this.parent = parent;
        this.title = title;
        this.factory = factory;
    }

    public ConfigScreenBuilder(Screen parent, McText title, Function<ConfigScreenSpec, Screen> factory) {
        this(parent, component(title), factory);
    }

    public ConfigCategoryBuilder category(Component title) {
        ConfigCategorySpec category = new ConfigCategorySpec(title, new ArrayList<>());
        categories.add(category);
        return new ConfigCategoryBuilder(this, category);
    }

    public ConfigCategoryBuilder category(McText title) {
        return category(component(title));
    }

    public ConfigCategoryBuilder category(String title) {
        return category(McText.literal(title));
    }

    public ConfigScreenBuilder saving(Runnable savingRunnable) {
        this.savingRunnable = savingRunnable == null ? () -> {} : savingRunnable;
        return this;
    }

    public Screen build() {
        return factory.apply(new ConfigScreenSpec(parent, title, List.copyOf(categories), savingRunnable));
    }

    private static Component component(McText text) {
        if (text == null) {
            return Component.literal("");
        }
        return switch (text.type()) {
            case LITERAL -> Component.literal(text.value());
            case TRANSLATABLE -> Component.translatable(text.value(), text.args());
            case KEYBIND -> Component.keybind(text.value());
        };
    }

    public static final class ConfigCategoryBuilder {

        private final ConfigScreenBuilder owner;
        private final ConfigCategorySpec category;

        private ConfigCategoryBuilder(ConfigScreenBuilder owner, ConfigCategorySpec category) {
            this.owner = owner;
            this.category = category;
        }

        public ConfigCategoryBuilder booleanOption(
                Component label,
                boolean value,
                boolean defaultValue,
                Component tooltip,
                Consumer<Boolean> saveConsumer
        ) {
            category.options().add(ConfigOptionSpec.bool(label, value, defaultValue, tooltip, saveConsumer));
            return this;
        }

        public ConfigCategoryBuilder booleanOption(
                McText label,
                boolean value,
                boolean defaultValue,
                McText tooltip,
                Consumer<Boolean> saveConsumer
        ) {
            category.options().add(ConfigOptionSpec.bool(label, value, defaultValue, tooltip, saveConsumer));
            return this;
        }

        public ConfigCategoryBuilder booleanOption(
                String label,
                boolean value,
                boolean defaultValue,
                String tooltip,
                Consumer<Boolean> saveConsumer
        ) {
            return booleanOption(McText.literal(label), value, defaultValue, McText.literal(tooltip), saveConsumer);
        }

        public ConfigCategoryBuilder intOption(
                Component label,
                int value,
                int defaultValue,
                int min,
                int max,
                Component tooltip,
                IntConsumer saveConsumer
        ) {
            category.options().add(ConfigOptionSpec.integer(label, value, defaultValue, min, max, tooltip, saveConsumer));
            return this;
        }

        public ConfigCategoryBuilder intOption(
                McText label,
                int value,
                int defaultValue,
                int min,
                int max,
                McText tooltip,
                IntConsumer saveConsumer
        ) {
            category.options().add(ConfigOptionSpec.integer(label, value, defaultValue, min, max, tooltip, saveConsumer));
            return this;
        }

        public ConfigCategoryBuilder intOption(
                String label,
                int value,
                int defaultValue,
                int min,
                int max,
                String tooltip,
                IntConsumer saveConsumer
        ) {
            return intOption(McText.literal(label), value, defaultValue, min, max, McText.literal(tooltip), saveConsumer);
        }

        public ConfigScreenBuilder done() {
            return owner;
        }
    }
}
