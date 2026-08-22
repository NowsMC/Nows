package space.nows.mcnows.mc.api.client.player;

import space.nows.mcnows.mc.api.McVec3;

import java.util.UUID;

public record PlayerSnapshot(
        UUID id,
        String name,
        McVec3 position,
        McVec3 velocity,
        float yaw,
        float pitch,
        float health,
        float maxHealth,
        int food,
        float saturation,
        int level,
        int totalExperience,
        float experienceProgress,
        boolean creative,
        boolean spectator,
        boolean flying,
        boolean mayFly,
        int selectedHotbarSlot
) {}
