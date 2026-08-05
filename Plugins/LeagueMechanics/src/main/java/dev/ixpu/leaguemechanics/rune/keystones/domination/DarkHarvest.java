package dev.ixpu.leaguemechanics.rune.keystones.domination;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.rune.StackingRune;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.kyori.adventure.text.Component;


public class DarkHarvest extends StackingRune {

    private double BASE_PHYSICAL_DAMAGE_PER_STACK = 2.5;
    private int LEVEL_COST_PER_STACK = 5;

    int COOLDOWN_DURATION_SECONDS = 60;

    private static final double HEALTH_THRESHOLD = 0.50;
    private static final int REAP_DELAY_TICKS = 45;

    private LeagueMechanics plugin;

    public DarkHarvest(ConfigurationSection config) {
        super("dark-harvest", RunePath.DOMINATION, RuneSlot.KEYSTONE, 20);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.domination.dark-harvest");
        if (section != null) {
            this.BASE_PHYSICAL_DAMAGE_PER_STACK = section.getDouble("attack-damage-per-stack", this.BASE_PHYSICAL_DAMAGE_PER_STACK);
            this.LEVEL_COST_PER_STACK = section.getInt("level-cost-per-stack", this.LEVEL_COST_PER_STACK);
            this.COOLDOWN_DURATION_SECONDS = section.getInt("cooldown", COOLDOWN_DURATION_SECONDS);
        }
        this.setCooldownSeconds(COOLDOWN_DURATION_SECONDS);
    }

    @Override
    public void onEnable(Player player) {
        //
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        triggerDarkHarvest(attacker, target, event);
    }

    public void onProjectileHit(Player shooter, Entity target) {
        triggerDarkHarvest(shooter, target, null);
    }

    private void triggerDarkHarvest(Player player, Entity target, EntityDamageByEntityEvent event) {
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

        int currentStacks = getStacks(player);
        double totalOutput = (currentStacks * BASE_PHYSICAL_DAMAGE_PER_STACK) / 2;

        if (event != null) {
            event.setDamage(event.getDamage() + totalOutput);
        } else {
            livingTarget.damage(totalOutput, player);
        }

        if (isOnCooldown(player) || player.getLevel() < LEVEL_COST_PER_STACK) {
            return;
        }

        scheduleAddStack(player);
        resetCooldown(player);
    }

    @Override
    public void tick(Player player) {
        int currentStacks = getStacks(player);

        if (isOnCooldown(player)) {
            displayCooldown(player, currentStacks);
            return;
        }
        displaySoulInfo(player, currentStacks);
    }

    private void scheduleAddStack(Player attacker) {
        if (plugin == null) {
            addStackWithCost(attacker);
            return;
        }
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> addStackWithCost(attacker), REAP_DELAY_TICKS);
    }

    private void addStackWithCost(Player player) {
        if (getStacks(player) >= maxStacks) {
            return;
        }

        player.setLevel(player.getLevel() - LEVEL_COST_PER_STACK);
        addStack(player);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0f, 1.0f);
    }

    private void displayCooldown(Player player, int currentStacks) {
        String cooldownDisplay = getCooldownDisplay(player);
        player.sendActionBar(Component.text("§7👻 " + currentStacks + "/" + maxStacks + " | " + cooldownDisplay));
    }

    private void displaySoulInfo(Player player, int currentStacks) {
        if (currentStacks >= 1) {
            player.sendActionBar(Component.text()
                    .append(Component.text("§c👻 " + currentStacks + "/" + maxStacks))
                    .build());
        } else {
            player.sendActionBar(Component.text()
                    .append(Component.text("§c👻"))
                    .build());
        }
    }
}