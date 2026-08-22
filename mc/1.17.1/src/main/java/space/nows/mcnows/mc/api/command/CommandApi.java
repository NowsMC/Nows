package space.nows.mcnows.mc.api.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;
import java.util.function.Consumer;

/** Collects command registrations until a Minecraft command dispatcher is available. */
public interface CommandApi {
    void register(Consumer<CommandDispatcher<CommandSourceStack>> registration);

    void register(CommandSpec spec);


    List<Consumer<CommandDispatcher<CommandSourceStack>>> registrations();

    void applyTo(CommandDispatcher<CommandSourceStack> dispatcher);
}
