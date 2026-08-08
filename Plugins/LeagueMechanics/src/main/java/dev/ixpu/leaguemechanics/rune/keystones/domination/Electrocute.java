package dev.ixpu.leaguemechanics.rune.keystones.domination;

import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.rune.StackingRune;
import dev.ixpu.leaguemechanics.manager.DamageManager;
import dev.ixpu.leaguemechanics.player.PlayerStats;

import java.util.UUID;

import org.bukkit.Sound;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.kyori.adventure.text.Component;


public class Electrocute extends StackingRune {

    private double BASE_ADAPTIVE_DAMAGE = 3.5;

    private static final int MAXIMUM_STACKS = 3;

    int COOLDOWN_DURATION_SECONDS = 25;

    public Electrocute(org.bukkit.configuration.ConfigurationSection config) {
        super("electrocute", RunePath.DOMINATION, RuneSlot.KEYSTONE, 3, 60);
        enablePerTargetStacking();
        enablePerTargetExpiry();

        ConfigurationSection section = config.getConfigurationSection("runes.keystones.domination.electrocute");
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

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        activateElectrocute(attacker, target, event);
    }

    public void onProjectileHit(Player shooter, Entity target) {
        activateElectrocute(shooter, target, null);
    }

    private void activateElectrocute(Player player, Entity target, EntityDamageByEntityEvent event) {
        UUID targetUUID = target.getUniqueId();

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }
        if (isOnCooldown(player)) {
            return;
        }

        switchTarget(player, targetUUID);
        addStack(player, targetUUID);

        int stacks = getStacks(player, targetUUID);
        if (stacks >= MAXIMUM_STACKS) {
            target.getWorld().strikeLightning(target.getLocation());
            resetStacksForTarget(player, targetUUID);
            resetCooldown(player);

            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0f, 1.2f);

            double damageOutput = bonusDamage(player, target);

            if (event != null) {
                event.setDamage(event.getDamage() + damageOutput);
            } else {
                livingTarget.damage(damageOutput);
            }
        }
    }

    private int trackPerTargetStacks(Player player) {
        tickStackExpiry(player);
        UUID lastTargetUUID = lastTarget.getOrDefault(player.getUniqueId(), null);
        int maxStacks = 0;

        if (lastTargetUUID != null) {
            maxStacks = getStacks(player, lastTargetUUID);
        }
        return maxStacks;
    }

    private double bonusDamage(Player player, Entity target) {
        DamageManager damageManager = new DamageManager();
        damageManager.enableAdaptiveScaling();
        return damageManager.totalBonusDamage(player, target, 0);
    }

    @Override
    public void tick(Player player) {
        int currentStacks = trackPerTargetStacks(player);

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
        DamageManager damageManager = new DamageManager();
        damageManager.enableAdaptiveScaling();

        String statsDisplay = playerStats.getActionBarSections(player);
        String actionBarMessage = runeDisplay + " " + statsDisplay;
        player.sendActionBar(Component.text(actionBarMessage));
    }

    enum RuneState {
        COOLDOWN, STACKING, IDLE
    }

    private String getRuneDisplay(Player player, RuneState state, int currentStacks) {
        return switch (state) {
            case COOLDOWN -> "§7⚡ " + getCooldownDisplay(player);
            case STACKING -> "§c⚡ " + currentStacks + "/" + MAXIMUM_STACKS;
            case IDLE -> "§4⚡";
        };
    }
}