package dev.ixpu.leaguemechanics.rune.keystones.domination;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.rune.StackingRune;
import dev.ixpu.leaguemechanics.manager.DamageManager;
import dev.ixpu.leaguemechanics.player.PlayerStats;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.kyori.adventure.text.Component;

public class DarkHarvest extends StackingRune {

    private double BASE_ADAPTIVE_DAMAGE_PER_STACK = 2.5;
    private int LEVEL_COST_PER_STACK = 5;

    int COOLDOWN_DURATION_SECONDS = 60;

    private static final double HEALTH_THRESHOLD = 0.50;
    private static final int REAP_DELAY_TICKS = 75;

    private LeagueMechanics plugin;

    public DarkHarvest(ConfigurationSection config) {
        super("dark-harvest", RunePath.DOMINATION, RuneSlot.KEYSTONE, 20);
        ConfigurationSection section = config.getConfigurationSection("runes.keystones.domination.dark-harvest");
        if (section != null) {
            this.BASE_ADAPTIVE_DAMAGE_PER_STACK = section.getDouble("adaptive-damage-per-stack", this.BASE_ADAPTIVE_DAMAGE_PER_STACK);
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
        activateDarkHarvest(attacker, target, event);
    }

    @Override
    public void onProjectileHit(Player shooter, Entity target) {
        activateDarkHarvest(shooter, target, null);
    }

    private void activateDarkHarvest(Player player, Entity target, EntityDamageByEntityEvent event) {
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

        double damageOutput = bonusDamage(player, target);

        if (event != null) {
            event.setDamage(event.getDamage() + damageOutput);
        } else {
            livingTarget.damage(damageOutput);
        }

        if (isOnCooldown(player) || player.getLevel() < LEVEL_COST_PER_STACK) {
            return;
        }

        scheduleAddStack(player);
        resetCooldown(player);
    }

    private double bonusDamage(Player player, Entity target) {
        DamageManager damageManager = new DamageManager();
        damageManager.enablePerStackScaling();

        int currentStacks = getStacks(player);
        return damageManager.totalBonusDamage(player, target, currentStacks);
    }

    private void scheduleAddStack(Player attacker) {
        if (plugin == null) {
            stackCost(attacker);
            return;
        }
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> stackCost(attacker), REAP_DELAY_TICKS);
    }

    private void stackCost(Player player) {
        if (getStacks(player) >= maxStacks) {
            return;
        }

        player.setLevel(player.getLevel() - LEVEL_COST_PER_STACK);
        addStack(player);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0f, 1.0f);
    }

    @Override
    public void tick(Player player) {
        int currentStacks = getStacks(player);

        if (isOnCooldown(player)) {
            String runeDisplay = getRuneDisplay(player, RuneState.COOLDOWN, currentStacks);
            setPlayerDisplay(player, runeDisplay);
            return;
        }
        if (currentStacks > 0) {
            String runeDisplay = getRuneDisplay(player, RuneState.STACKING, currentStacks);
            setPlayerDisplay(player, runeDisplay);
            return;
        }
        String runeDisplay = getRuneDisplay(player, RuneState.IDLE, currentStacks);
        setPlayerDisplay(player, runeDisplay);
    }

    private void setPlayerDisplay(Player player, String runeDisplay) {
        PlayerStats playerStats = new PlayerStats();
        String statsDisplay = playerStats.getActionBarSections(player);

        String actionBarMessage = runeDisplay + " " + statsDisplay;
        player.sendActionBar(Component.text(actionBarMessage));
    }

    enum RuneState {
        COOLDOWN, STACKING, IDLE
    }

    private String getRuneDisplay(Player player, RuneState state, int currentStacks) {
        return switch (state) {
            case COOLDOWN -> "§7👻 " + currentStacks + "/" + maxStacks + " | " + getCooldownDisplay(player);
            case STACKING -> "§c👻 " + currentStacks + "/" + maxStacks;
            case IDLE -> "§c👻";
        };
    }
}