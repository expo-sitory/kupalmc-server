package dev.ixpu.leaguerunes.rune;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class BaseRune {
    protected final String id;
    protected final RunePath path;
    protected final RuneSlot slot;
    protected boolean hasStacking;
    protected int maxStacks;
    protected double cooldownSeconds = 0.0; // Cooldown in seconds (0 = no cooldown)
    
    // Track cooldowns per player: playerUUID -> lastActivationTime (ms)
    protected final Map<UUID, Long> playerCooldowns = new HashMap<>();

    public BaseRune(String id, RunePath path, RuneSlot slot) {
        this.id = id;
        this.path = path;
        this.slot = slot;
        this.hasStacking = false;
        this.maxStacks = 0;
    }

    public String getId() {
        return id;
    }

    public RunePath getPath() {
        return path;
    }

    public RuneSlot getSlot() {
        return slot;
    }

    public boolean hasStacking() {
        return hasStacking;
    }

    public int getMaxStacks() {
        return maxStacks;
    }

    protected void setStacking(int maxStacks) {
        this.hasStacking = true;
        this.maxStacks = maxStacks;
    }

    public double getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(double seconds) {
        this.cooldownSeconds = seconds;
    }

    /**
     * Check if the rune is on cooldown for the player
     */
    public boolean isOnCooldown(Player player) {
        if (cooldownSeconds <= 0) {
            return false; // No cooldown
        }

        UUID uuid = player.getUniqueId();
        Long lastActivation = playerCooldowns.get(uuid);

        if (lastActivation == null) {
            return false; // Never activated
        }

        long currentTime = System.currentTimeMillis();
        long cooldownMs = (long) (cooldownSeconds * 1000);
        return (currentTime - lastActivation) < cooldownMs;
    }

    /**
     * Get remaining cooldown time in seconds
     */
    public double getRemainingCooldown(Player player) {
        if (cooldownSeconds <= 0) {
            return 0;
        }

        UUID uuid = player.getUniqueId();
        Long lastActivation = playerCooldowns.get(uuid);

        if (lastActivation == null) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long cooldownMs = (long) (cooldownSeconds * 1000);
        long elapsed = currentTime - lastActivation;
        long remaining = cooldownMs - elapsed;

        return Math.max(0, remaining / 1000.0);
    }

    /**
     * Reset the cooldown for the player (call when rune activates)
     */
    public void resetCooldown(Player player) {
        playerCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Clear cooldowns for a player (call on logout)
     */
    public void clearPlayerCooldown(Player player) {
        playerCooldowns.remove(player.getUniqueId());
    }

    public abstract void onEnable(Player player);

    public abstract void onDisable(Player player);

    public abstract void tick(Player player);

    public void onPlayerDamage(Player player, double damage) {}

    public void onPlayerDealDamage(Player player, double damage) {}

    public void onPlayerAttack(Player player) {}

    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {}

    public void onPlayerKill(Player player, Player killed) {}

    public void onPlayerDeath(Player player) {}
}
