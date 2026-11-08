package space.nows.fixture.consumer;

import java.util.Optional;

/**
 * Gives generated recipes to Minecraft's own parser. The entry point differs by version and one source has to compile
 * for every adapter, so it is found by reflection. This is verification code, not production code.
 */
final class RecipeParserProbe {
    private RecipeParserProbe() {
    }

    static String rejectionByGame(String json) throws Exception {
        ClassLoader loader = RecipeParserProbe.class.getClassLoader();
        Class<?> jsonParser = Class.forName("com.google.gson.JsonParser", true, loader);
        Object element;
        try {
            element = jsonParser.getMethod("parseString", String.class).invoke(null, json);
        } catch (NoSuchMethodException beforeGson286) {
            element = jsonParser.getMethod("parse", String.class).invoke(jsonParser.getConstructor().newInstance(), json);
        }
        Class<?> recipe = Class.forName("net.minecraft.world.item.crafting.Recipe", true, loader);
        try {
            Object codec = recipe.getField("CODEC").get(null);
            Class<?> dynamicOps = Class.forName("com.mojang.serialization.DynamicOps", true, loader);
            Object jsonOps = Class.forName("com.mojang.serialization.JsonOps", true, loader).getField("INSTANCE").get(null);
            Object lookup = Class.forName("net.minecraft.data.registries.VanillaRegistries", true, loader)
                    .getMethod("createLookup").invoke(null);
            Object ops = Class.forName("net.minecraft.resources.RegistryOps", true, loader)
                    .getMethod("create", dynamicOps, Class.forName("net.minecraft.core.HolderLookup$Provider", true, loader))
                    .invoke(null, jsonOps, lookup);
            Object result = Class.forName("com.mojang.serialization.Codec", true, loader)
                    .getMethod("parse", dynamicOps, Object.class).invoke(codec, ops, element);
            Class<?> dataResult = Class.forName("com.mojang.serialization.DataResult", true, loader);
            if (((Optional<?>) dataResult.getMethod("result").invoke(result)).isPresent()) {
                return null;
            }
            Object error = ((Optional<?>) dataResult.getMethod("error").invoke(result)).orElseThrow();
            java.lang.reflect.Method message = error.getClass().getMethod("message");
            message.setAccessible(true);
            return String.valueOf(message.invoke(error));
        } catch (NoSuchFieldException beforeCodecs) {
            Class<?> manager = Class.forName("net.minecraft.world.item.crafting.RecipeManager", true, loader);
            Class<?> location = Class.forName("net.minecraft.resources.ResourceLocation", true, loader);
            Object id = location.getConstructor(String.class, String.class).newInstance("probe", "recipe");
            try {
                manager.getMethod("fromJson", location, Class.forName("com.google.gson.JsonObject", true, loader))
                        .invoke(null, id, element);
                return null;
            } catch (java.lang.reflect.InvocationTargetException rejected) {
                return String.valueOf(rejected.getTargetException());
            }
        }
    }
}
