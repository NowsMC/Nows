package space.nows.mcnows.mc.internal.mixin;

import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.injection.Inject;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PackRepositoryMixinContractTest {
    private static final String PACK_CONSTRUCTOR_DESCRIPTOR =
            "<init>(Lnet/minecraft/server/packs/repository/Pack$PackConstructor;[Lnet/minecraft/server/packs/repository/RepositorySource;)V";

    @Test
    void constructorInjectionTargetsMinecraftConstructorShape() throws Exception {
        Method method = Arrays.stream(PackRepositoryMixin.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals("nows$installModResourcePackSource"))
                .findFirst()
                .orElseThrow();
        Inject inject = method.getAnnotation(Inject.class);
        List<String> targets = List.of(inject.method());

        if (hasPackConstructorOverload()) {
            assertTrue(targets.contains(PACK_CONSTRUCTOR_DESCRIPTOR),
                    "PackRepositoryMixin must target the Pack$PackConstructor overload explicitly");
        } else {
            assertTrue(targets.contains("<init>") || targets.stream().anyMatch(target -> target.startsWith("<init>(")),
                    "PackRepositoryMixin must target a PackRepository constructor");
        }
    }

    private static boolean hasPackConstructorOverload() throws Exception {
        Class<?> repository = Class.forName("net.minecraft.server.packs.repository.PackRepository");
        Class<?> packConstructor;
        try {
            packConstructor = Class.forName("net.minecraft.server.packs.repository.Pack$PackConstructor");
        } catch (ClassNotFoundException ignored) {
            return false;
        }
        Class<?> repositorySource = Class.forName("net.minecraft.server.packs.repository.RepositorySource");
        Class<?> repositorySourceArray = Array.newInstance(repositorySource, 0).getClass();
        for (Constructor<?> constructor : repository.getConstructors()) {
            if (Arrays.equals(constructor.getParameterTypes(), new Class<?>[]{packConstructor, repositorySourceArray})) {
                return true;
            }
        }
        return false;
    }
}
