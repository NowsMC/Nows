package space.nows.mcnows.api;

/** Stable bytecode hook. Runs immediately before a class is defined by Nows. */
@FunctionalInterface
public interface ClassTransformer {
    byte[] transform(String className, byte[] classBytes) throws Exception;
}
