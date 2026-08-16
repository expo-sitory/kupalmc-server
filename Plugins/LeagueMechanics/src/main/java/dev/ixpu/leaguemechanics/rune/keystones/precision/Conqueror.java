package dev.ixpu.leaguemechanics.rune.keystones.precision;

import dev.ixpu.leaguemechanics.rune.RunePath;
import dev.ixpu.leaguemechanics.rune.RuneSlot;
import dev.ixpu.leaguemechanics.rune.StacksHandler;
import dev.ixpu.leaguemechanics.manager.DamageManager;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import dev.ixpu.leaguemechanics.listener.PlayerEventListener;

import java.util.UUID;

import dev.ixpu.leaguemechanics.util.DebugLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.configuration.ConfigurationSection;

import net.kyori.adventure.text.Component;


public class Conqueror extends StacksHandler {

    private double BASE_ADAPTIVE_DAMAGE_PER_STACK = 0.7;

    private static final int MAXIMUM_STACKS = 12;

    private PlayerEventListener listener;

    public Conqueror(ConfigurationSection config) {
        super("conqueror", RunePath.PRECISION, RuneSlot.KEYSTONE, 12, 100);
        this.listener = listener;
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
        activateConqueror(shooter, target);
    }

    public void onAttack(Player attacker, Entity target) {
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

        double statsDamage = keystoneDamage(player, target, getStacks(player, targetUUID));
        double newHealth = Math.clamp(livingTarget.getHealth() - statsDamage, 0, livingTarget.getMaxHealth());

        DebugLogger.debug(player, "§7[Debug] §f[§dAttacker§f] [§eConqueror§f] Keystone Damage = §d" + Math.ceil(statsDamage * 100) / 100.0);
        DebugLogger.debug(player, "§7[Debug] §f[§dTarget§f] Target New HP = §d" + Math.ceil(newHealth * 100) / 100.0);

        livingTarget.setHealth(newHealth);
    }

    private double keystoneDamage(Player player, Entity target, int currentStacks) {
        if (listener.isAnyHotbarOnCooldown(player)) {
            return 0.0;
        }
        DamageManager damageManager = new DamageManager();
        damageManager.enableAdaptiveScaling();
        damageManager.enablePerStackScaling();
        return damageManager.DamageCalculation(player, target, currentStacks, BASE_ADAPTIVE_DAMAGE_PER_STACK, 0);
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