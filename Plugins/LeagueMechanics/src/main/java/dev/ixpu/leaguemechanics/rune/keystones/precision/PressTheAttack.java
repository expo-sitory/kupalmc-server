package dev.ixpu.leaguemechanics.rune.keystones.precision;

import dev.ixpu.leaguemechanics.manager.DamageManager;
import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.rune.StackingRune;
import dev.ixpu.leaguemechanics.player.PlayerStats;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.configuration.ConfigurationSection;

import net.kyori.adventure.text.Component;
import org.bukkit.event.entity.EntityDamageByEntityEvent;


public class PressTheAttack extends StackingRune {

    private double BASE_ADAPTIVE_DAMAGE = 4.5;

    int COOLDOWN_DURATION_SECONDS = 6;

    private static final int MAX_STACKS = 3;

    public PressTheAttack(org.bukkit.configuration.ConfigurationSection config) {
        super("press-the-attack", RunePath.PRECISION, RuneSlot.KEYSTONE, 3, 60);
        enablePerTargetStacking();
        enablePerTargetExpiry();

        ConfigurationSection section = config.getConfigurationSection("runes.keystones.precision.press-the-attack");

        if (section != null) {
            this.BASE_ADAPTIVE_DAMAGE = section.getDouble("base-adaptive-damage", this.BASE_ADAPTIVE_DAMAGE);
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
        double newHealth = Math.max(0   , livingTarget.getHealth() - statsDamage);

        shooter.sendMessage(Component.text("§7[Debug] §f[§ePress The Attack§f] (Projectile) Stats Damage = " + statsDamage));
        shooter.sendMessage(Component.text("§7[Debug] §f[§ePress The Attack§f] (Projectile) Target New HP = " + newHealth));

        livingTarget.setHealth(newHealth);
    }
    
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        double statsDamage = playerDamage(attacker, target);
        double newHealth = Math.max(0   , livingTarget.getHealth() - statsDamage);

        attacker.sendMessage(Component.text("§7[Debug] §f[§ePress The Attack§f] (Melee) Stats Damage = " + statsDamage));
        attacker.sendMessage(Component.text("§7[Debug] §f[§ePress The Attack§f] (Melee) Target New HP = " + newHealth));

        livingTarget.setHealth(newHealth);

        activatePressTheAttack(attacker, target);
    }

    private void activatePressTheAttack(Player player, Entity target) {
        UUID targetUUID = target.getUniqueId();

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        switchTarget(player, targetUUID);
        int currentStacks = getStacks(player, targetUUID);

        if (currentStacks == 2) {
            double newHealth = Math.max(0, livingTarget.getHealth() - keystoneDamage(player, target));

            player.sendMessage(Component.text("§7[Debug] §f[§ePress The Attack§f] Keystone Damage = " + keystoneDamage(player, target)));
            player.sendMessage(Component.text("§7[Debug] §f[§ePress The Attack§f] Target New HP = " + newHealth));

            livingTarget.setHealth(newHealth);

            resetStacksForTarget(player, targetUUID);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "press-the-attack-stack-sound " + player.getName());
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 0.5f);
            resetCooldown(player);
        } else {
            addStack(player, targetUUID);
        }
    }

    private double keystoneDamage(Player player, Entity target) {
        DamageManager damageManager = new DamageManager();
        damageManager.enableAdaptiveScaling();
        return damageManager.totalBonusDamage(player, target, 0);
    }

    private double playerDamage(Player player, Entity target) {
        DamageManager damageManager = new DamageManager();
        return damageManager.totalBonusDamage(player, target, 0);
    }


    private int trackActiveStacks(Player player) {
        tickStackExpiry(player);
        UUID lastTargetUUID = lastTarget.getOrDefault(player.getUniqueId(), null);
        int currentStacks = 0;
        if (lastTargetUUID != null) {
            currentStacks = getStacks(player, lastTargetUUID);
        }
        return currentStacks;
    }

    @Override
    public void tick(Player player) {
        int currentStacks = trackActiveStacks(player);
        String cooldownDisplay = getCooldownDisplay(player);
        if (!cooldownDisplay.isEmpty()) {
            String runeDisplay = getRuneDisplay(RuneState.COOLDOWN, currentStacks, cooldownDisplay);
            setPlayerDisplay(player, runeDisplay);
            return;
        }
        RuneState state = currentStacks == 0 ? RuneState.IDLE : RuneState.ACTIVE;
        String runeDisplay = getRuneDisplay(state, currentStacks, cooldownDisplay);
        setPlayerDisplay(player, runeDisplay);
    }

    private void setPlayerDisplay(Player player, String runeDisplay) {
        PlayerStats playerStats = new PlayerStats();
        String statsDisplay = playerStats.getActionBarSections(player);

        String actionBarMessage = runeDisplay + " " + statsDisplay;
        player.sendActionBar(Component.text(actionBarMessage));
    }

    enum RuneState {
        COOLDOWN, IDLE, ACTIVE
    }

    private String getRuneDisplay(RuneState state, int stacks, String cooldown) {
        return switch (state) {
            case COOLDOWN -> "§7✽ " + cooldown;
            case ACTIVE -> "§e✽ " + stacks + "/" + MAX_STACKS;
            case IDLE -> "§6✽";
        };
    }
}