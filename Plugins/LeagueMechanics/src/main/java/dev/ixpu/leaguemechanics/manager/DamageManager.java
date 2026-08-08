package dev.ixpu.leaguemechanics.manager;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.player.PlayerStats;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;


public class DamageManager {

    protected boolean isAdaptive = false;
    protected boolean isPerStack = false;
    protected boolean isOnlyAP = false;

    public void enableAdaptiveScaling() {
        this.isAdaptive = true;
    }
    public void enablePerStackScaling() {
        this.isPerStack = true;
    }
    public void enableOnlyAP() {
        this.isOnlyAP = true;
    }

    public double totalBonusDamage(Player player, Entity target, int currentStacks) {
        PlayerStats stats = new PlayerStats();

        double totalPhysicalDamage = (stats.getPlayerAD(player) - getTargetAR((Player) target))  / 2;
        double totalMagicDamage = (stats.getPlayerAP(player) - getTargetMR((Player) target))  / 4;

        if (isOnlyAP) {
            return totalMagicDamage * levelBasedBonus(player);
        }
        if (isAdaptive) {
            double adaptiveDamage = 0;
            if (isPerStack) {
                adaptiveDamage = Math.max(totalPhysicalDamage, totalMagicDamage);
                return (adaptiveDamage * levelBasedBonus(player)) * currentStacks;
            }
            adaptiveDamage = Math.max(totalPhysicalDamage, totalMagicDamage);
            return adaptiveDamage * levelBasedBonus(player);
        }
        return totalPhysicalDamage + totalMagicDamage;
    }

    public double levelBasedBonus(Player player) {
        double playerLevel = player.getLevel();
        if (playerLevel >= 100) {
            return 1.7;
        } else if (playerLevel >= 70) {
            return 1.5;
        } else if (playerLevel >= 40) {
            return 1.2;
        } else if (playerLevel >= 10) {
            return 1.07;
        } else {
            return 1.03;
        }
    }

    public double getTargetAR(Player target) {
        PlayerStats stats = new PlayerStats();
        double totalAR = 0;
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        if (itemStatsManager != null) {
            totalAR = stats.getPlayerAR(target) + itemStatsManager.getItemAR(target);
        }
        return totalAR;
    }

    public double getTargetMR(Player target) {
        PlayerStats stats = new PlayerStats();
        double totalMR = 0;
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();
        if (itemStatsManager != null) {
            totalMR = stats.getPlayerMR(target) + itemStatsManager.getItemMR(target);
        }
        return totalMR;
    }
}