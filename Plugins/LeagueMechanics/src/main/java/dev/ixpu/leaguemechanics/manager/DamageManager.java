package dev.ixpu.leaguemechanics.manager;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.player.PlayerStats;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;


public class DamageManager {

    protected boolean isAdaptive = false;
    protected boolean isPerStack = false;
    protected boolean isOnlyAP = false;
    protected boolean isOnlyAD = false;

    public void enableAdaptiveScaling() {
        this.isAdaptive = true;
    }
    public void enablePerStackScaling() {
        this.isPerStack = true;
    }
    public void enableOnlyAP() {
        this.isOnlyAP = true;
    }
    public void enableOnlyAD() {
        this.isOnlyAD = true;
    }

    public double totalBonusDamage(Player player, Entity target, int currentStacks) {
        PlayerStats stats = new PlayerStats();
        StatsManager statsManager = LeagueMechanics.getInstance().getStatsManager();

        double totalAD = (stats.getAttackerAD(player) + (statsManager != null ? statsManager.getPlayerAD(player) : 0)) / 2;
        double totalAP = (stats.getBaseAP() + (statsManager != null ? statsManager.getPlayerAP(player) : 0)) / 4;

        double totalArmor = stats.getTargetAR((LivingEntity) target) + (statsManager != null ? statsManager.getTargetAR((Player) target) : 0);
        double totalMR = stats.getBaseMR() + (statsManager != null ? statsManager.getTargetMR((Player) target) : 0);

        double stackMultiplier = isPerStack ? currentStacks : 1;

        double damageAD = (totalAD * stackMultiplier) - totalArmor;
        double damageAP = (totalAP * stackMultiplier) - totalMR;

        if (isOnlyAP) {
            return damageAP * levelBasedBonus(player);
        }
        if (isOnlyAD) {
            return damageAD * levelBasedBonus(player);
        }
        if (isAdaptive) {
            double adaptiveDamage = Math.max(damageAD, damageAP);
            return adaptiveDamage * levelBasedBonus(player);
        }
        return damageAD + damageAP;
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
}