package dev.ixpu.leaguemechanics.rune.keystones.domination;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import dev.ixpu.leaguemechanics.rune.BaseRune;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import net.kyori.adventure.text.Component;
import org.bukkit.event.entity.EntityDamageByEntityEvent;


public class DarkHarvest extends BaseRune {

    private double BASE_PHYSICAL_DAMAGE_PER_STACK = 2.5;
    private int MAXIMUM_STACKS = 20;
    private int LEVEL_COST_PER_STACK = 5;

    int COOLDOWN_DURATION_SECONDS = 60;

    private static final double HEALTH_THRESHOLD = 0.50;
    private static final int REAP_DELAY_TICKS = 45;

    private final Map<UUID, Integer> playerStacks = new HashMap<>();
    private LeagueMechanics plugin;

    public DarkHarvest(ConfigurationSection config) {
        super("dark-harvest", RunePath.DOMINATION, RuneSlot.KEYSTONE);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.domination.dark-harvest");
        if (section != null) {
            this.BASE_PHYSICAL_DAMAGE_PER_STACK = section.getDouble("attack-damage-per-stack", this.BASE_PHYSICAL_DAMAGE_PER_STACK);
            this.MAXIMUM_STACKS = section.getInt("maximum-stacks", this.MAXIMUM_STACKS);
            this.LEVEL_COST_PER_STACK = section.getInt("level-cost-per-stack", this.LEVEL_COST_PER_STACK);
            this.COOLDOWN_DURATION_SECONDS = section.getInt("cooldown", COOLDOWN_DURATION_SECONDS);
        }
        this.setCooldownSeconds(COOLDOWN_DURATION_SECONDS);
    }

    @Override
    public void onEnable(Player player) {
        UUID uuid = player.getUniqueId();
        playerStacks.put(uuid, 0);
    }

    @Override
    public void onDisable(Player player) {
        UUID uuid = player.getUniqueId();
        clearPlayerCooldown(player);
        playerStacks.remove(uuid);
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        triggerDarkHarvest(attacker, target, event);
    }
    public void onProjectileHit(Player shooter, Entity target, EntityDamageByEntityEvent event) {
        triggerDarkHarvest(shooter, target, event);
    }
    private void triggerDarkHarvest(Player player, Entity target, EntityDamageByEntityEvent event) {
        UUID playerUUID = player.getUniqueId();

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        double targetHealth = livingTarget.getHealth();
        double maxHealth = livingTarget.getMaxHealth();
        double healthPercent = targetHealth / maxHealth;

        if (healthPercent >= HEALTH_THRESHOLD) {
            return;
        }

        int CURRENT_STACKS = playerStacks.getOrDefault(playerUUID, 0);
        double totalOutput = (CURRENT_STACKS * BASE_PHYSICAL_DAMAGE_PER_STACK) / 2;

        event.setDamage(event.getDamage() + totalOutput);


        if (isOnCooldown(player) || player.getLevel() < LEVEL_COST_PER_STACK) {
            return;
        }

        scheduleReapSoul(player);
        resetCooldown(player);
    }


    @Override
    public void tick(Player player) {
        UUID playerUUID = player.getUniqueId();
        int CURRENT_STACKS = playerStacks.getOrDefault(playerUUID, 0);

        if (isOnCooldown(player)) {
            displayCooldown(player, CURRENT_STACKS);
            return;
        }
        displaySoulInfo(player, CURRENT_STACKS);
    }

    private void scheduleReapSoul(Player attacker) {
        if (plugin == null) {
            reapSoul(attacker);
            return;
        }
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> reapSoul(attacker), REAP_DELAY_TICKS);
    }

    private void reapSoul(Player player) {
        UUID playerUUID = player.getUniqueId();
        int current = playerStacks.getOrDefault(playerUUID, 0);

        if (current >= MAXIMUM_STACKS) {
            return;
        }

        player.setLevel(player.getLevel() - LEVEL_COST_PER_STACK);
        current++;
        playerStacks.put(playerUUID, current);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0f, 1.0f);
    }

    private void displayCooldown(Player player, int CURRENT_STACKS) {
        String cooldownDisplay = getCooldownDisplay(player);
        player.sendActionBar(Component.text("§7👻 " + CURRENT_STACKS + "/" + MAXIMUM_STACKS + " | " + cooldownDisplay));
    }

    private void displaySoulInfo(Player player, int CURRENT_STACKS) {
        if (CURRENT_STACKS >= 1) {
                player.sendActionBar(Component.text()
                        .append(Component.text("§c👻 " + CURRENT_STACKS + "/" + MAXIMUM_STACKS))
                        .build());
        } else {
                player.sendActionBar(Component.text()
                        .append(Component.text("§c👻"))
                        .build());
        }
    }
}