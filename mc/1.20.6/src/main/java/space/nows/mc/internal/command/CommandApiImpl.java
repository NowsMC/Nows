/*
 * Copyright 2026 TamKungZ_ (Nows MC — https://nows.space)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.nows.mc.internal.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import space.nows.mc.api.McBlockPos;
import space.nows.mc.api.command.CommandApi;
import space.nows.mc.api.command.CommandArgumentSpec;
import space.nows.mc.api.command.CommandExecutionContext;
import space.nows.mc.api.command.CommandSpec;
import space.nows.mc.api.text.NativeTextBridge;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class CommandApiImpl implements CommandApi {
    private final List<Consumer<CommandDispatcher<CommandSourceStack>>> registrations = new ArrayList<>();

    @Override
    public void register(Consumer<CommandDispatcher<CommandSourceStack>> registration) {
        registrations.add(registration);
    }


    public void register(CommandSpec spec) {
        register(dispatcher -> dispatcher.register(build(spec)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> build(CommandSpec spec) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(spec.literal());
        if (spec.permissionLevel() > 0) {
            root.requires(permission(spec.permissionLevel()));
        }
        Command<CommandSourceStack> run = context -> execute(spec, context);
        List<CommandArgumentSpec> arguments = spec.arguments();
        if (arguments.stream().allMatch(CommandArgumentSpec::optional)) {
            root.executes(run);
        }
        // Chain the arguments from the last to the first. A node can run the command when every argument
        // after it is optional.
        ArgumentBuilder<CommandSourceStack, ?> tail = null;
        for (int index = arguments.size() - 1; index >= 0; index--) {
            CommandArgumentSpec argument = arguments.get(index);
            RequiredArgumentBuilder<CommandSourceStack, ?> node =
                    Commands.argument(argument.name(), argumentType(argument.type()));
            if (tail != null) {
                node.then(tail);
            }
            if (arguments.subList(index + 1, arguments.size()).stream().allMatch(CommandArgumentSpec::optional)) {
                node.executes(run);
            }
            tail = node;
        }
        return tail == null ? root : root.then(tail);
    }

    private static int execute(CommandSpec spec, CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        if (spec.contextExecutor() == null) {
            spec.executor().run();
            return spec.result();
        }
        CommandSourceStack source = context.getSource();
        Map<String, Object> values = new LinkedHashMap<>();
        for (CommandArgumentSpec argument : spec.arguments()) {
            boolean supplied = context.getNodes().stream()
                    .anyMatch(parsed -> parsed.getNode().getName().equals(argument.name()));
            if (supplied) {
                values.put(argument.name(), read(argument, context));
            }
        }
        return spec.contextExecutor().apply(new CommandExecutionContext(
                source.getTextName(), spec.permissionLevel(), values,
                message -> reply(source, NativeTextBridge.nativeComponent(message, Component.class))));
    }

    private static ArgumentType<?> argumentType(CommandArgumentSpec.Type type) {
        return switch (type) {
            case STRING -> StringArgumentType.string();
            case WORD -> StringArgumentType.word();
            case GREEDY_STRING -> StringArgumentType.greedyString();
            case INTEGER -> IntegerArgumentType.integer();
            case DOUBLE -> DoubleArgumentType.doubleArg();
            case BOOLEAN -> BoolArgumentType.bool();
            case PLAYER -> EntityArgument.player();
            case BLOCK_POS -> BlockPosArgument.blockPos();
        };
    }

    private static Object read(CommandArgumentSpec argument, CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        String name = argument.name();
        return switch (argument.type()) {
            case STRING, WORD, GREEDY_STRING -> StringArgumentType.getString(context, name);
            case INTEGER -> IntegerArgumentType.getInteger(context, name);
            case DOUBLE -> DoubleArgumentType.getDouble(context, name);
            case BOOLEAN -> BoolArgumentType.getBool(context, name);
            case PLAYER -> EntityArgument.getPlayer(context, name).getName().getString();
            case BLOCK_POS -> {
                BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, name);
                yield McBlockPos.of(pos.getX(), pos.getY(), pos.getZ());
            }
        };
    }

    private static void reply(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> message, false);
    }

    private static Predicate<CommandSourceStack> permission(int level) {
        return source -> source.hasPermission(level);
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
