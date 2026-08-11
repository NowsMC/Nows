package space.nows.mcnows.mc.api.client.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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

    public ConfigCategoryBuilder category(Component title) {
        ConfigCategorySpec category = new ConfigCategorySpec(title, new ArrayList<>());
        categories.add(category);
        return new ConfigCategoryBuilder(this, category);
    }

    public ConfigScreenBuilder saving(Runnable savingRunnable) {
        this.savingRunnable = savingRunnable == null ? () -> {} : savingRunnable;
        return this;
    }

    public Screen build() {
        return factory.apply(new ConfigScreenSpec(parent, title, List.copyOf(categories), savingRunnable));
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

        public ConfigScreenBuilder done() {
            return owner;
        }
    }
}
