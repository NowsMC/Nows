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

import java.util.Objects;

/** Stable simple command registration translated to Brigadier by each Minecraft adapter. */
public final class CommandSpec {
    private final String literal;
    private final Runnable executor;
    private final int result;

    private CommandSpec(Builder builder) {
        this.literal = requireLiteral(builder.literal);
        this.executor = builder.executor == null ? () -> {} : builder.executor;
        this.result = builder.result;
    }

    public static Builder literal(String literal) {
        return new Builder(literal);
    }

    public String literal() {
        return literal;
    }

    public Runnable executor() {
        return executor;
    }

    public int result() {
        return result;
    }

    private static String requireLiteral(String literal) {
        Objects.requireNonNull(literal, "literal");
        if (literal.isBlank() || literal.indexOf(' ') >= 0) {
            throw new IllegalArgumentException("Command literal must be a single non-blank word");
        }
        return literal;
    }

    public static final class Builder {
        private final String literal;
        private Runnable executor;
        private int result = 1;

        private Builder(String literal) {
            this.literal = literal;
        }

        public Builder executes(Runnable executor) {
            this.executor = executor;
            return this;
        }

        public Builder result(int result) {
            this.result = result;
            return this;
        }

        public CommandSpec build() {
            return new CommandSpec(this);
        }
    }
}
