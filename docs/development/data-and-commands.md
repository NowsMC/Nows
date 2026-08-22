# Data Packs, Commands And Generated Data

Datapack and resource-pack sources are exposed through the version adapter:

```java
DataPacks packs = MinecraftApi.dataPacks(context);
Path modPackDir = packs.modPackDirectory("my_mod");
Path dataDir = packs.modDataDirectory("my_mod");
Path assetsDir = packs.modAssetsDirectory("my_mod");
List<Object> serverSources = packs.sources(PackTarget.SERVER_DATA);
```

Commands and generated JSON data are collected through the adapter as well:

```java
MinecraftApi.commands(context).register(CommandSpec.literal("my_mod")
        .executes(MyCommands::run)
        .result(1)
        .build());

DataGen dataGen = MinecraftApi.dataGen(context);
dataGen.writeJson(dataGen.recipePath("my_mod:widget"), Map.class, Map.of(
        "type", "minecraft:crafting_shapeless",
        "ingredients", List.of(Map.of("item", "minecraft:stone")),
        "result", Map.of("id", "my_mod:widget", "count", 1)
));
```

Use `PackTarget.SERVER_DATA` for data packs and `PackTarget.CLIENT_RESOURCES` for assets. `CommandSpec` covers simple literal commands through a stable Nows-owned value; the lower-level Brigadier dispatcher callback remains available for commands that need arguments, permissions or native source details.
