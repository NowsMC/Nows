package space.nows.mcnows.mc.internal.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import space.nows.mcnows.mc.api.command.CommandApi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class CommandApiImpl implements CommandApi {
    private final List<Consumer<CommandDispatcher<CommandSourceStack>>> registrations = new ArrayList<>();

    @Override
    public void register(Consumer<CommandDispatcher<CommandSourceStack>> registration) {
        registrations.add(registration);
    }

    @Override
    public List<Consumer<CommandDispatcher<CommandSourceStack>>> registrations() {
        return List.copyOf(registrations);
    }

    @Override
    public void applyTo(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (Consumer<CommandDispatcher<CommandSourceStack>> registration : registrations) {
            registration.accept(dispatcher);
        }
    }
}
