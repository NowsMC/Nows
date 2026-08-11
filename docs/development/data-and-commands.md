# Data Packs, Commands And Generated Data

Datapack and resource-pack sources are exposed through the version adapter:

```java
DataPacks packs = MinecraftApi.dataPacks(context);
Path modPackDir = packs.modPackDirectory("my_mod");
packs.registerSource(PackType.SERVER_DATA, repositoryConsumer -> {
    // Create or forward Minecraft RepositorySource packs for this version.
});
```

Commands and generated JSON data are collected through the adapter as well:

```java
MinecraftApi.commands(context).register(dispatcher ->
        dispatcher.register(Commands.literal("my_mod").executes(command -> 1)));

DataGen dataGen = MinecraftApi.dataGen(context);
dataGen.writeJson(dataGen.recipePath("my_mod:widget"), Map.class, Map.of(
        "type", "minecraft:crafting_shapeless",
        "ingredients", List.of(Map.of("item", "minecraft:stone")),
        "result", Map.of("id", "my_mod:widget", "count", 1)
));
```
