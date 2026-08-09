package dev.ixpu.leaguemechanics.rune.keystones.precision;

import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.rune.StackingRune;
import dev.ixpu.leaguemechanics.manager.DamageManager;
import dev.ixpu.leaguemechanics.player.PlayerStats;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.configuration.ConfigurationSection;

import net.kyori.adventure.text.Component;
import org.bukkit.event.entity.EntityDamageByEntityEvent;


public class Conqueror extends StackingRune {

    private double BASE_ADAPTIVE_DAMAGE_PER_STACK = 2.5;

    private static final int MAXIMUM_STACKS = 12;

    public Conqueror(ConfigurationSection config) {
        super("conqueror", RunePath.PRECISION, RuneSlot.KEYSTONE, 12, 100);
        enablePerTargetStacking();
        enablePerTargetExpiry();

        ConfigurationSection section = config.getConfigurationSection("runes.keystones.precision.conqueror");
        if (section != null) {
            this.BASE_ADAPTIVE_DAMAGE_PER_STACK = section.getDouble("base-adaptive-damage-per-stack", this.BASE_ADAPTIVE_DAMAGE_PER_STACK);
        }
    }

    @Override
    public void onEnable(Player player) {
        //
    }

    public void onProjectileHit(Player shooter, Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        double statsDamage = keystoneDamage(shooter, target, getStacks(shooter));
        double newHealth = Math.max(0, livingTarget.getHealth() - statsDamage);

        shooter.sendMessage(Component.text("§7[Debug] §f[§eConqueror§f] (Projectile) Keystone Damage = " + statsDamage));
        shooter.sendMessage(Component.text("§7[Debug] §f[§eConqueror§f] (Projectile) Target New HP = " + newHealth));

        livingTarget.setHealth(newHealth);

        activateConqueror(shooter, target);
    }

    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        double statsDamage = keystoneDamage(attacker, target, getStacks(attacker));
        double newHealth = Math.max(0, livingTarget.getHealth() - statsDamage);

        attacker.sendMessage(Component.text("§7[Debug] §f[§eConqueror§f] (Melee) Keystone Damage = " + statsDamage));
        attacker.sendMessage(Component.text("§7[Debug] §f[§eConqueror§f] (Melee) Target New HP = " + newHealth));

        livingTarget.setHealth(newHealth);

        activateConqueror(attacker, target);
    }

    private void activateConqueror(Player player, Entity target) {
        UUID targetUUID = target.getUniqueId();

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }
        switchTarget(player, targetUUID);
        addStack(player, targetUUID);
    }

    private double keystoneDamage(Player player, Entity target, int currentStacks) {
        DamageManager damageManager = new DamageManager();
        damageManager.enableAdaptiveScaling();
        damageManager.enablePerStackScaling();
        return damageManager.totalBonusDamage(player, target, currentStacks);
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
    protected void onStackAdded(Player player, int newStackCount) {
        if (newStackCount == 1) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "conqueror-stack-sound " + player.getName());
        } else if (newStackCount == MAXIMUM_STACKS) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "conqueror-max-stack-sound " + player.getName());
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 0.5f);
        }
    }

    @Override
    protected void onStacksExpired(Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "conqueror-expired-sound " + player.getName());
    }

    @Override
    public void tick(Player player) {
        int stacks = trackActiveStacks(player);

        if (stacks > 0) {
            String runeDisplay = getRuneDisplay(RuneState.STACKING, stacks);
            setPlayerDisplay(player, runeDisplay);
            return;
        }

        String runeDisplay = getRuneDisplay(RuneState.IDLE, stacks);
        setPlayerDisplay(player, runeDisplay);
    }

    private void setPlayerDisplay(Player player, String runeDisplay) {
        PlayerStats playerStats = new PlayerStats();
        String statsDisplay = playerStats.getActionBarSections(player);

        String actionBarMessage = runeDisplay + " " + statsDisplay;
        player.sendActionBar(Component.text(actionBarMessage));
    }

    enum RuneState {
        STACKING, IDLE
    }

    private String getRuneDisplay(RuneState state, int currentStacks) {
        return switch (state) {
            case STACKING -> "§e🪓 " + currentStacks + "/" + MAXIMUM_STACKS;
            case IDLE -> "§6🪓";
        };
    }
}