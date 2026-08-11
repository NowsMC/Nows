package space.nows.mcnows.mc.api.client.player;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public interface PlayerApi {
    Optional<LocalPlayer> current();

    default LocalPlayer requireCurrent() {
        return current().orElseThrow(() -> new IllegalStateException("No local Minecraft player is available"));
    }

    PlayerSnapshot snapshot();

    String name();

    Vec3 position();

    Vec3 velocity();

    float health();

    float maxHealth();

    int food();

    float saturation();

    int level();

    int totalExperience();

    float experienceProgress();

    int selectedHotbarSlot();

    ItemStack selectedItem();

    void setHealth(float health);

    void heal(float amount);

    void setFood(int food);

    void setSaturation(float saturation);

    void setExperience(float progress, int level, int totalExperience);

    void addExperiencePoints(int points);

    void addExperienceLevels(int levels);

    void setPosition(double x, double y, double z);

    default void setPosition(Vec3 position) {
        setPosition(position.x(), position.y(), position.z());
    }

    void setRotation(float yaw, float pitch);

    void setVelocity(double x, double y, double z);

    default void setVelocity(Vec3 velocity) {
        setVelocity(velocity.x(), velocity.y(), velocity.z());
    }

    void setFlying(boolean flying);

    void setMayFly(boolean mayFly);

    void setInvulnerable(boolean invulnerable);

    void setFlyingSpeed(float speed);

    void setWalkingSpeed(float speed);

    boolean addItem(ItemStack item);

    void setInventoryItem(int slot, ItemStack item);

    void setSelectedHotbarSlot(int slot);

    void sendSystemMessage(Component message);

    void sendOverlayMessage(Component message);
}
