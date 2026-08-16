package dev.ixpu.leaguemechanics.manager;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.listener.PlayerEventListener;
import dev.ixpu.leaguemechanics.player.PlayerStats;

import dev.ixpu.leaguemechanics.util.DebugLogger;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;

import java.util.Random;


public class DamageManager {

    protected boolean isAdaptive = false;
    protected boolean isTrueDamage = false;
    protected boolean isPerStack = false;
    protected boolean isOnlyAP = false;

    private static final Random RANDOM = new Random();
    private PlayerEventListener listener;

    public void enableAdaptiveScaling() {
        this.isAdaptive = true;
    }
    public void enableTrueDamage() {
        this.isTrueDamage = true;
    }
    public void enablePerStackScaling() {
        this.isPerStack = true;
    }
    public void enableOnlyAP() {
        this.isOnlyAP = true;
    }


    public double DamageCalculation(Player player, Entity target, int currentStacks, double bonusAdaptive, double bonusTrueDamage) {
        PlayerStats stats = new PlayerStats();

        double attackerAD = (stats.getPlayerAD(player) + getPlayerAdaptiveAD(player)) / 7;
        double attackerAP = (stats.getPlayerAP(player) + getPlayerAdaptiveAP(player)) / 7;

        double targetAR = getTargetAR(target);
        double targetMR = getTargetMR(target);

        double totalPhysicalDamage = attackerAD / (1.0 + (targetAR / 100.0));
        double totalMagicDamage = attackerAP / (1.0 + (targetMR / 100.0));

        double totalDamageOutput = totalPhysicalDamage + (totalMagicDamage * (60 / 100.0));

        if (criticalChance(getPlayerCritChance(player))) {
            totalDamageOutput *= 1.75;
        }

        double rawDamage = attackerAD + attackerAP;
        double trueDamage = stats.getPlayerTD(player) + (rawDamage * (bonusTrueDamage / 100));

        double adaptiveDamage = getPlayerAdaptiveAD(player) + getPlayerAdaptiveAP(player);

        if (isOnlyAP) {
            return totalMagicDamage;
        }
        if (isAdaptive) {
            if (isPerStack) {
                return ((adaptiveDamage + bonusAdaptive) * levelBasedBonus(player)) * currentStacks;
            }
            return (adaptiveDamage + bonusAdaptive) * levelBasedBonus(player);
        }
        if (isTrueDamage) {
            return trueDamage;
        }
        return totalDamageOutput;
    }

    public double getPlayerAdaptiveAD(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        PlayerStats playerStats = new PlayerStats();
        if (playerStats.getPlayerAP(player) > playerStats.getPlayerAD(player)) {
            return 0;
        }
        if (itemStatsManager != null) {
            return playerStats.getPlayerAF(player) * (0.1 * itemStatsManager.getItemAD(player));
        }
        return 0;
    }

    public double getPlayerAdaptiveAP(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        PlayerStats playerStats = new PlayerStats();
        if (playerStats.getPlayerAD(player) > playerStats.getPlayerAP(player)) {
            return 0;
        }
        if (itemStatsManager != null) {
            return playerStats.getPlayerAF(player) * (0.1 * itemStatsManager.getItemAP(player));
        }
        return 0;
    }

    public double getPlayerCritChance(Player player) {
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        if (itemStatsManager != null) {
            return itemStatsManager.getItemCC(player);
        }
        return 0;
    }

    public static boolean criticalChance(double playerCritChance) {
        if (playerCritChance <= 0) return false;
        if (playerCritChance >= 100) return true;
        return RANDOM.nextDouble() * 100 < playerCritChance;
    }

    private double levelBasedBonus(Player player) {
        double playerLevel = player.getLevel();
        if (playerLevel >= 300) {
            return 1.7;
        } else if (playerLevel >= 200) {
            return 1.5;
        } else if (playerLevel >= 100) {
            return 1.2;
        } else if (playerLevel >= 50) {
            return 1.07;
        } else {
            return 1.03;
        }
    }

    public double getTargetAR(Entity target) {
        PlayerStats stats = new PlayerStats();
        if (!(target instanceof Player targetPlayer)) {
            return 0;
        }
        return stats.getPlayerAR(targetPlayer);
    }

    public double getTargetMR(Entity target) {
        PlayerStats stats = new PlayerStats();
        if (!(target instanceof Player targetPlayer)) {
            return 0;
        }
        return stats.getPlayerMR(targetPlayer);
    }
}