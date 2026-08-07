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
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.kyori.adventure.text.Component;


public class Conqueror extends StackingRune {

    private double BASE_PHYSICAL_DAMAGE_PER_STACK = 2.5;

    private static final int MAXIMUM_STACKS = 12;

    public Conqueror(ConfigurationSection config) {
        super("conqueror", RunePath.PRECISION, RuneSlot.KEYSTONE, 12, 100);
        enablePerTargetStacking();
        enablePerTargetExpiry();

        ConfigurationSection section = config.getConfigurationSection("runes.keystones.precision.conqueror");
        if (section != null) {
            this.BASE_PHYSICAL_DAMAGE_PER_STACK = section.getDouble("attack-damage-per-stack", this.BASE_PHYSICAL_DAMAGE_PER_STACK);
        }
    }

    @Override
    public void onEnable(Player player) {
        //
    }

    @Override
    public void onAttack(Player attacker, Entity target, EntityDamageByEntityEvent event) {
        activateConqueror(attacker, target, event);
    }

    private void activateConqueror(Player player, Entity target, EntityDamageByEntityEvent event) {

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }
        if (livingTarget.getMaxHealth() < 20) {
            return;
        }

        UUID targetUUID = target.getUniqueId();
        switchTarget(player, targetUUID);
        addStack(player, targetUUID);

        double damageOutput = bonusDamage(player, target);
        event.setDamage(event.getDamage() + damageOutput);

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

    private double bonusDamage(Player player, Entity target) {
        DamageManager damageManager = new DamageManager();
        damageManager.enablePerStackScaling();
        damageManager.enableAdaptiveScaling();

        int currentStacks = getStacks(player);
        return damageManager.totalBonusDamage(player, target, currentStacks);
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