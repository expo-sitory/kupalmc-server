package dev.ixpu.leaguerunes.rune;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public abstract class BaseRune {
    protected final String id;
    protected final RunePath path;
    protected final RuneSlot slot;
    protected boolean hasStacking;
    protected int maxStacks;

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
