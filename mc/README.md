# Minecraft version policy

This directory owns small, version-specific runtime policy for Minecraft versions Nows supports directly.

Keep code that depends on Minecraft API shape out of `core/`. Put stable launch behavior in `minecraft/`, and put per-version facts in `mc/<minecraft-version>/nows-minecraft.properties` so the runtime can select the right policy without hardcoding every version in Java.

The `minecraft` module packages this directory into `META-INF/nows/mc/`.

Do not add `net.minecraft.` to parent-first classloading. Minecraft classes belong to the game classloader and should come from the selected vanilla client jar/inherited Launcher profile.
