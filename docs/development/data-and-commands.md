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

Use `PackTarget.SERVER_DATA` for data packs and `PackTarget.CLIENT_RESOURCES` for assets. `CommandSpec` builds a Brigadier literal with optional `CommandArgumentSpec` arguments (string, word, greedy string, integer, decimal, boolean, player and block position), an optional permission level, and a callback that receives the parsed values, the source name and a reply function. Player arguments are passed as the player name, and an optional argument that was not typed is absent from the context. The lower-level Brigadier dispatcher callback remains available for commands that need native source details.

Generated JSON follows the selected Minecraft version. Recipes use the item stack objects of 1.20.5 and the plain id ingredients of 1.21.2 where those versions require them, and recipes and tags are written to the folder names that version reads (`recipes` and `tags/items` up to 1.20.6, `recipe` and `tags/item` from 1.21).
