package dev.ixpu.leaguemechanics.manager;

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

        double totalPhysicalDamage = Math.max(0, (stats.getPlayerAD(player) - getTargetAR(target)) / 2);
        double totalMagicDamage = Math.max(0, (stats.getPlayerAP(player) - getTargetMR(target)) / 4);

        if (isOnlyAP) {
            return totalMagicDamage * levelBasedBonus(player);
        }
        if (isAdaptive) {
            double adaptiveDamage;
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