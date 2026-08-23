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

package space.nows.mc.api.command;

import space.nows.mc.api.McBlockPos;
import space.nows.mc.api.text.McText;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/** Stable command execution context independent of Brigadier and CommandSourceStack. */
public record CommandExecutionContext(
        String sourceName,
        int permissionLevel,
        Map<String, Object> arguments,
        Consumer<McText> feedback
) {
    public CommandExecutionContext {
        sourceName = sourceName == null ? "" : sourceName;
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
        feedback = feedback == null ? ignored -> {} : feedback;
    }

    public Optional<String> string(String name) {
        return value(name, String.class);
    }

    public Optional<Integer> integer(String name) {
        return value(name, Integer.class);
    }

    public Optional<Double> decimal(String name) {
        return value(name, Double.class);
    }

    public Optional<Boolean> bool(String name) {
        return value(name, Boolean.class);
    }

    public Optional<McBlockPos> blockPos(String name) {
        return value(name, McBlockPos.class);
    }

    public void reply(McText message) {
        feedback.accept(message);
    }

    private <T> Optional<T> value(String name, Class<T> type) {
        Object value = arguments.get(name);
        return type.isInstance(value) ? Optional.of(type.cast(value)) : Optional.empty();
    }
}
