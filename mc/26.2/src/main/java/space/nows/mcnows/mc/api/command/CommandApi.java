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
