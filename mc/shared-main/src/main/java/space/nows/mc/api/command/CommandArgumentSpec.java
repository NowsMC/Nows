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

import java.util.Objects;

/** Stable command argument declaration for generated commands. */
public record CommandArgumentSpec(String name, Type type, boolean optional) {
    public CommandArgumentSpec {
        Objects.requireNonNull(name, "name");
        if (name.isBlank() || name.indexOf(' ') >= 0) {
            throw new IllegalArgumentException("argument name must be a single non-blank word");
        }
        if (type == null) {
            type = Type.STRING;
        }
    }

    public static CommandArgumentSpec string(String name) {
        return new CommandArgumentSpec(name, Type.STRING, false);
    }

    public static CommandArgumentSpec integer(String name) {
        return new CommandArgumentSpec(name, Type.INTEGER, false);
    }

    public static CommandArgumentSpec decimal(String name) {
        return new CommandArgumentSpec(name, Type.DOUBLE, false);
    }

    public static CommandArgumentSpec bool(String name) {
        return new CommandArgumentSpec(name, Type.BOOLEAN, false);
    }

    public static CommandArgumentSpec player(String name) {
        return new CommandArgumentSpec(name, Type.PLAYER, false);
    }

    public static CommandArgumentSpec blockPos(String name) {
        return new CommandArgumentSpec(name, Type.BLOCK_POS, false);
    }

    public CommandArgumentSpec asOptional() {
        return new CommandArgumentSpec(name, type, true);
    }

    public enum Type {
        STRING,
        WORD,
        GREEDY_STRING,
        INTEGER,
        DOUBLE,
        BOOLEAN,
        PLAYER,
        BLOCK_POS
    }
}
