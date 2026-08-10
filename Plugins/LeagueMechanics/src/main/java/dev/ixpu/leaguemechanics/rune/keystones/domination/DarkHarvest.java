package dev.ixpu.leaguemechanics.rune.keystones.domination;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.rune.StackingRune;
import dev.ixpu.leaguemechanics.manager.DamageManager;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import dev.ixpu.leaguemechanics.util.DebugLogger;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.configuration.ConfigurationSection;

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

    public void onProjectileHit(Player shooter, Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        double statsDamage = playerDamage(shooter, target);
        double newHealth = Math.max(0, Math.min(livingTarget.getMaxHealth(), livingTarget.getHealth() - playerDamage(shooter, target)));

        DebugLogger.debug(shooter, "§7[Debug] §f[§cDark Harvest§f] (Projectile) Stats Damage = §d" + Math.ceil(statsDamage * 100) / 100.0);
        DebugLogger.debug(shooter, "§7[Debug] §f[§cDark Harvest§f] (Projectile) Target New HP = §d" + Math.ceil(newHealth * 100) / 100.0);

        livingTarget.setHealth(newHealth);

        activateDarkHarvest(shooter, target);
    }

    public void onAttack(Player attacker, Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        double statsDamage = playerDamage(attacker, target);
        double newHealth = Math.max(0, Math.min(livingTarget.getMaxHealth(), livingTarget.getHealth() - statsDamage));

        livingTarget.setHealth(newHealth);

        DebugLogger.debug(attacker, "§7[Debug] §f[§cDark Harvest§f] (Melee) Stats Damage = §d" + Math.ceil(statsDamage * 100) / 100.0);
        DebugLogger.debug(attacker, "§7[Debug] §f[§cDark Harvest§f] (Melee) Target New HP = §d" + Math.ceil(newHealth * 100) / 100.0);

        activateDarkHarvest(attacker, target);
    }

    private void activateDarkHarvest(Player player, Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        double targetHealth = livingTarget.getHealth();
        double maxHealth = livingTarget.getMaxHealth();
        double healthPercent = targetHealth / maxHealth;
        double newHealth = Math.max(0, Math.min(livingTarget.getMaxHealth(), livingTarget.getHealth() - keystoneDamage(player, target)));

        if (healthPercent >= HEALTH_THRESHOLD) {
            return;
        }

        DebugLogger.debug(player, "§7[Debug] §f[§cDark Harvest§f] Keystone Damage = §d" + Math.ceil(keystoneDamage(player, target) * 100) / 100.0);
        DebugLogger.debug(player, "§7[Debug] §f[§cDark Harvest§f] Target New HP = §d" + Math.ceil(healthPercent * 100) / 100.0);

        livingTarget.setHealth(newHealth);

        if (isOnCooldown(player) || player.getLevel() < LEVEL_COST_PER_STACK) {
            return;
        }

        scheduleAddStack(player);
        resetCooldown(player);
    }

    private double keystoneDamage(Player player, Entity target) {
        DamageManager damageManager = new DamageManager();
        damageManager.enablePerStackScaling();
        damageManager.enableAdaptiveScaling();

        int currentStacks = getStacks(player);
        return damageManager.totalBonusDamage(player, target, currentStacks);
    }

    private double playerDamage(Player player, Entity target) {
        DamageManager damageManager = new DamageManager();
        return damageManager.totalBonusDamage(player, target, 0);
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
            case IDLE -> "§4👻";
        };
    }
}