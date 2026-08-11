package space.nows.mcnows.mc.internal.client.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import space.nows.mcnows.mc.api.client.player.PlayerApi;
import space.nows.mcnows.mc.api.client.player.PlayerSnapshot;

import java.util.Optional;

public final class PlayerApiImpl implements PlayerApi {
    @Override
    public Optional<LocalPlayer> current() {
        return Optional.ofNullable(Minecraft.getInstance().player);
    }

    @Override
    public PlayerSnapshot snapshot() {
        LocalPlayer player = requireCurrent();
        Abilities abilities = player.getAbilities();
        return new PlayerSnapshot(
                player.getUUID(),
                player.getScoreboardName(),
                player.position(),
                player.getDeltaMovement(),
                player.getYRot(),
                player.getXRot(),
                player.getHealth(),
                player.getMaxHealth(),
                player.getFoodData().getFoodLevel(),
                player.getFoodData().getSaturationLevel(),
                player.experienceLevel,
                player.totalExperience,
                player.experienceProgress,
                player.isCreative(),
                player.isSpectator(),
                abilities.flying,
                abilities.mayfly,
                player.getInventory().getSelectedSlot()
        );
    }

    @Override
    public String name() {
        return requireCurrent().getScoreboardName();
    }

    @Override
    public Vec3 position() {
        return requireCurrent().position();
    }

    @Override
    public Vec3 velocity() {
        return requireCurrent().getDeltaMovement();
    }

    @Override
    public float health() {
        return requireCurrent().getHealth();
    }

    @Override
    public float maxHealth() {
        return requireCurrent().getMaxHealth();
    }

    @Override
    public int food() {
        return requireCurrent().getFoodData().getFoodLevel();
    }

    @Override
    public float saturation() {
        return requireCurrent().getFoodData().getSaturationLevel();
    }

    @Override
    public int level() {
        return requireCurrent().experienceLevel;
    }

    @Override
    public int totalExperience() {
        return requireCurrent().totalExperience;
    }

    @Override
    public float experienceProgress() {
        return requireCurrent().experienceProgress;
    }

    @Override
    public int selectedHotbarSlot() {
        return requireCurrent().getInventory().getSelectedSlot();
    }

    @Override
    public ItemStack selectedItem() {
        return requireCurrent().getInventory().getSelectedItem();
    }

    @Override
    public void setHealth(float health) {
        requireCurrent().setHealth(health);
    }

    @Override
    public void heal(float amount) {
        requireCurrent().heal(amount);
    }

    @Override
    public void setFood(int food) {
        requireCurrent().getFoodData().setFoodLevel(food);
    }

    @Override
    public void setSaturation(float saturation) {
        requireCurrent().getFoodData().setSaturation(saturation);
    }

    @Override
    public void setExperience(float progress, int level, int totalExperience) {
        requireCurrent().setExperienceValues(progress, level, totalExperience);
    }

    @Override
    public void addExperiencePoints(int points) {
        requireCurrent().giveExperiencePoints(points);
    }

    @Override
    public void addExperienceLevels(int levels) {
        requireCurrent().giveExperienceLevels(levels);
    }

    @Override
    public void setPosition(double x, double y, double z) {
        requireCurrent().setPos(x, y, z);
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        LocalPlayer player = requireCurrent();
        player.setYRot(yaw);
        player.setXRot(pitch);
    }

    @Override
    public void setVelocity(double x, double y, double z) {
        requireCurrent().setDeltaMovement(x, y, z);
    }

    @Override
    public void setFlying(boolean flying) {
        LocalPlayer player = requireCurrent();
        player.getAbilities().flying = flying;
        player.onUpdateAbilities();
    }

    @Override
    public void setMayFly(boolean mayFly) {
        LocalPlayer player = requireCurrent();
        player.getAbilities().mayfly = mayFly;
        player.onUpdateAbilities();
    }

    @Override
    public void setInvulnerable(boolean invulnerable) {
        LocalPlayer player = requireCurrent();
        player.getAbilities().invulnerable = invulnerable;
        player.onUpdateAbilities();
    }

    @Override
    public void setFlyingSpeed(float speed) {
        LocalPlayer player = requireCurrent();
        player.getAbilities().setFlyingSpeed(speed);
        player.onUpdateAbilities();
    }

    @Override
    public void setWalkingSpeed(float speed) {
        LocalPlayer player = requireCurrent();
        player.getAbilities().setWalkingSpeed(speed);
        player.onUpdateAbilities();
    }

    @Override
    public boolean addItem(ItemStack item) {
        return requireCurrent().getInventory().add(item);
    }

    @Override
    public void setInventoryItem(int slot, ItemStack item) {
        requireCurrent().getInventory().setItem(slot, item);
    }

    @Override
    public void setSelectedHotbarSlot(int slot) {
        Inventory inventory = requireCurrent().getInventory();
        inventory.setSelectedSlot(slot);
        inventory.setChanged();
    }

    @Override
    public void sendSystemMessage(Component message) {
        requireCurrent().displayClientMessage(message, false);
    }

    @Override
    public void sendOverlayMessage(Component message) {
        requireCurrent().displayClientMessage(message, true);
    }
}
