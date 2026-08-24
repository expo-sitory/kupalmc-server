package dev.ixpu.leaguemechanics.manager;

import dev.ixpu.leaguemechanics.LeagueMechanics;
import dev.ixpu.leaguemechanics.player.PlayerStats;
import dev.ixpu.leaguemechanics.util.ItemModifier;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;

import java.util.Random;


public class DamageManager {
    private final ItemStatsManager itemStatsManager;

    protected boolean isAdaptiveScaling = false;
    protected boolean isAdaptiveDamage = false;
    protected boolean isTrueDamage = false;
    protected boolean isPerStack = false;
    protected boolean isOnlyAP = false;

    private static final Random RANDOM = new Random();

    public DamageManager(ItemStatsManager itemStatsManager) {
        this.itemStatsManager = itemStatsManager;
    }

    public void enableAdaptiveScaling() {
        this.isAdaptiveScaling = true;
    }
    public void enableAdaptiveDamage() {
        this.isAdaptiveDamage = true;
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


    public double DamageCalculation(Player player, Entity target, int currentStacks, double runesAdaptive, double runesTrueDamage) {
        PlayerStats stats = PlayerStats.getOrCreate(player);
        ItemStatsManager itemStatsManager = LeagueMechanics.getInstance().getStatsManager();

        double doransBonus = getDoransOnHitAD(player);

        double attackerBonusAD = itemStatsManager.getItemAD(player);
        double attackerBonusAP = itemStatsManager.getItemAP(player);

        double attackerTotalAD = ((stats.getPlayerAD(player) + doransBonus) / 7);
        double attackerTotalAP = (stats.getPlayerAP(player)) / 7;

        double targetTotalAR = getTargetAR(target);
        double targetTotalMR = getTargetMR(target);

        double totalPhysicalDamage = attackerTotalAD / (1.0 + (targetTotalAR / 100.0));
        double totalMagicDamage = attackerTotalAP / (1.0 + (targetTotalMR / 100.0));

        double totalDamageOutput = totalPhysicalDamage + (totalMagicDamage * (60 / 100.0));

        if (criticalChance(getPlayerCritChance(player))) {
            totalDamageOutput *= 1.75;
        }

        double adaptiveDamage = runesAdaptive * levelBasedBonus(player);

        double adaptiveForce = runesAdaptive * levelBasedBonus(player);
        double bonusAdaptiveForce = adaptiveForce * stats.getPlayerAF(player);
        double totalAdaptiveForce = adaptiveForce + bonusAdaptiveForce;

        double rawDamage = attackerTotalAD + attackerTotalAP;
        double trueDamage = stats.getPlayerTD(player) + (rawDamage * (runesTrueDamage / 100));

        if (isOnlyAP) {
            return totalMagicDamage;
        }
        if (isAdaptiveDamage) {
            if (isAdaptiveScaling) {
                if (isPerStack) {
                    if (attackerBonusAD > attackerBonusAP) {
                        return (totalAdaptiveForce / (1.0 + (targetTotalAR / 100.0 ))) *currentStacks;
                    } else if (attackerBonusAP > attackerBonusAD) {
                        return (totalAdaptiveForce / (1.0 + (targetTotalMR / 100.0 ))) *currentStacks;
                    }
                }
            }
            if (isPerStack) {
                if (attackerBonusAD > attackerBonusAP) {
                    return (adaptiveDamage /  (1.0 + (targetTotalAR / 100.0))) * currentStacks;
                } else if (attackerBonusAP > attackerBonusAD) {
                    return (adaptiveDamage / (1.0 + (targetTotalMR / 100.0))) * currentStacks;
                } else {
                    return (adaptiveDamage /  (1.0 + (targetTotalAR / 100.0))) * currentStacks;
                }
            }
            if (attackerBonusAD > attackerBonusAP) {
                return adaptiveDamage / (1.0 + (targetTotalAR / 100.0));
            } else if (attackerBonusAP > attackerBonusAD) {
                return adaptiveDamage / (1.0 + (targetTotalMR / 100.0));
            } else {
                return adaptiveDamage / (1.0 + (targetTotalAR / 100.0));
            }
        }
        if (isTrueDamage) {
            return trueDamage;
        }
        return totalDamageOutput;
    }

    public double getPlayerCritChance(Player player) {
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
        if (!(target instanceof Player targetPlayer)) {
            return 0;
        }
        return PlayerStats.getOrCreate(targetPlayer).getPlayerAR(targetPlayer);
    }

    public double getTargetMR(Entity target) {
        if (!(target instanceof Player targetPlayer)) {
            return 0;
        }
        return PlayerStats.getOrCreate(targetPlayer).getPlayerMR(targetPlayer);
    }

    public double getDoransOnHitAD(Player player) {
        double bonus = 0;
        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir()) {
                continue;
            }

            String itemId = ItemModifier.getItemId(item);
            if (itemId != null && (itemId.equals("dorans-ring") || itemId.equals("dorans-shield"))) {
                bonus += 10;
            }
        }
        return bonus;
    }
}