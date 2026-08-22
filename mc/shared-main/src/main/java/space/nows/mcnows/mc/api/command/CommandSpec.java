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
