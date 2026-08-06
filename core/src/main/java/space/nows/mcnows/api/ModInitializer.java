package space.nows.mcnows.api;

/** Stable main entrypoint contract for a Nows mod. */
@FunctionalInterface
public interface ModInitializer {
    void onInitialize(NowsContext context) throws Exception;
}
