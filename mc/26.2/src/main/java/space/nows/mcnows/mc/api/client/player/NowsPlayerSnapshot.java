package space.nows.mcnows.mc.api.client.player;

import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record NowsPlayerSnapshot(
        UUID id,
        String name,
        Vec3 position,
        Vec3 velocity,
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
