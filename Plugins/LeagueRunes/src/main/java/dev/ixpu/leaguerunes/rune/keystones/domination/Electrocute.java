package dev.ixpu.leaguerunes.rune.keystones.domination;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import dev.ixpu.leaguerunes.rune.BaseRune;
import dev.ixpu.leaguerunes.rune.RunePath;
import dev.ixpu.leaguerunes.rune.RuneSlot;
import net.kyori.adventure.text.Component;

public class Electrocute extends BaseRune {
    private int MAX_STACKS = 3;
    private int STACK_DURATION_TICKS = 60;
    private int COOLDOWN_DURATION_TICKS = 400;

    private final Map<UUID, Map<UUID, Integer>> playerEntityStacks = new HashMap<>();
    private final Map<UUID, Map<UUID, Integer>> stackExpiryTicks = new HashMap<>();
    private final Map<UUID, Integer> cooldownTimers = new HashMap<>();

    public Electrocute(org.bukkit.configuration.ConfigurationSection config) {
        super("electrocute", RunePath.DOMINATION, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.domination.electrocute");
        if (section != null) {
            this.MAX_STACKS = section.getInt("max-stacks", 3);
            this.STACK_DURATION_TICKS = section.getInt("stack-duration", 60);
            this.COOLDOWN_DURATION_TICKS = section.getInt("cooldown", 400);
        }
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        playerEntityStacks.put(uuid, new HashMap<>());
        stackExpiryTicks.put(uuid, new HashMap<>());
        cooldownTimers.put(uuid, 0);
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        playerEntityStacks.remove(uuid);
        stackExpiryTicks.remove(uuid);
        cooldownTimers.remove(uuid);
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        UUID playerUUID = attacker.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        int cooldown = cooldownTimers.getOrDefault(playerUUID, 0);
        if (cooldown > 0) {
            return;
        }

        // Add stack to this specific entity
        addStack(attacker, targetUUID);

        // Check if triggered
        int stacks = playerEntityStacks.get(playerUUID).getOrDefault(targetUUID, 0);
        if (stacks >= MAX_STACKS) {
            triggerLightning(attacker, target);
            cooldownTimers.put(playerUUID, COOLDOWN_DURATION_TICKS);
            playerEntityStacks.get(playerUUID).remove(targetUUID);
            stackExpiryTicks.get(playerUUID).remove(targetUUID);
        }

        displayStackInfo(attacker, stacks);
    }


    // Handle projectile hits for bow/crossbow
    public void onProjectileHit(Player shooter, Entity target) {
        UUID playerUUID = shooter.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        int cooldown = cooldownTimers.getOrDefault(playerUUID, 0);
        if (cooldown > 0) {
            return;
        }

        // Add stack to this specific entity
        addStack(shooter, targetUUID);

        // Check if triggered
        int stacks = playerEntityStacks.get(playerUUID).getOrDefault(targetUUID, 0);
        if (stacks >= MAX_STACKS) {
            triggerLightning(shooter, target);
            shooter.playSound(shooter.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0f, 1.2f);
            cooldownTimers.put(playerUUID, COOLDOWN_DURATION_TICKS);
            playerEntityStacks.get(playerUUID).remove(targetUUID);
            stackExpiryTicks.get(playerUUID).remove(targetUUID);
        }

        displayStackInfo(shooter, stacks);
    }

    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Tick cooldown
        int cooldown = cooldownTimers.getOrDefault(playerUUID, 0);
        if (cooldown > 0) {
            cooldown--;
            cooldownTimers.put(playerUUID, cooldown);
        }

        // Tick stack expiries
        Map<UUID, Integer> entityStacks = playerEntityStacks.get(playerUUID);
        Map<UUID, Integer> expiries = stackExpiryTicks.get(playerUUID);

        for (UUID targetUUID : new java.util.ArrayList<>(entityStacks.keySet())) {
            int expiry = expiries.getOrDefault(targetUUID, 0);

            if (expiry > 0) {
                expiry--;
                expiries.put(targetUUID, expiry);

                if (expiry == 0) {
                    entityStacks.remove(targetUUID);
                    expiries.remove(targetUUID);
                }
            }
        }
    }

    private void addStack(Player player, UUID targetUUID) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, Integer> stacks = playerEntityStacks.get(playerUUID);
        int current = stacks.getOrDefault(targetUUID, 0);

        if (current < MAX_STACKS) {
            current++;
            stacks.put(targetUUID, current);
            stackExpiryTicks.get(playerUUID).put(targetUUID, STACK_DURATION_TICKS);
        }
    }

    private void triggerLightning(Player attacker, Entity target) {
        // Calculate damage based on XP level using bracket system
        double damage = getBonusDamageByLevel(attacker.getLevel());

        // Strike with lightning
        target.getWorld().strikeLightning(target.getLocation());

        // Apply damage to living entities
        if (target instanceof org.bukkit.entity.LivingEntity living) {
            double currentHealth = living.getHealth();
            living.setHealth(Math.max(0, currentHealth - (damage * 2)));
        }
    }

    private double getBonusDamageByLevel(int totalLevel) {
        if (totalLevel >= 100) {
            return 3.5;
        } else if (totalLevel >= 71) {
            return 3.0;
        } else if (totalLevel >= 51) {
            return 2.5;
        } else if (totalLevel >= 30) {
            return 2.0;
        } else {
            return 1.5;
        }
    }

    private void displayStackInfo(Player player, int stacks) {
        player.sendActionBar(Component.text()
                .append(Component.text("§c⚡ " + stacks + "/" + MAX_STACKS, net.kyori.adventure.text.format.NamedTextColor.WHITE))
                .build());
    }
}