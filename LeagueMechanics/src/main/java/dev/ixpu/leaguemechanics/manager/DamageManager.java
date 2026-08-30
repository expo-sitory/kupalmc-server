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

    private static final double CRIT_DAMAGE_MULTIPLIER = 1.75;

    private static final double DEFAULT_MAGIC_RATIO = 0.6;

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
        ItemStatsManager statsManager = LeagueMechanics.getInstance().getStatsManager();

        double doransBonus = getDoransOnHitAD(player);

        double attackerAD = (stats.getPlayerAD(player) + doransBonus) / 7.0;
        double attackerAP = stats.getPlayerAP(player) / 7.0;
        double targetAR = getTargetAR(target);
        double targetMR = getTargetMR(target);

        double baseDamage;

        if (isOnlyAP) {
            baseDamage = attackerAP;
        } else if (isTrueDamage) {
            baseDamage = stats.getPlayerTD(player)
                    + ((attackerAD + attackerAP) * (runesTrueDamage / 100.0));
        } else if (isAdaptiveDamage) {
            double adaptive = runesAdaptive * levelBasedBonus(player);
            if (isAdaptiveScaling) {
                adaptive += adaptive * stats.getPlayerAF(player);
            }
            boolean preferMagic = statsManager.getItemAP(player) > statsManager.getItemAD(player);
            baseDamage = applyResistance(adaptive, preferMagic, targetAR, targetMR);
        } else {
            double physical = applyResistance(attackerAD, false, targetAR, targetMR);
            double magic = applyResistance(attackerAP * DEFAULT_MAGIC_RATIO, true, targetAR, targetMR);
            baseDamage = physical + magic;
        }

        int stacks = isPerStack ? currentStacks : 1;
        return baseDamage * stacks;
    }

    private double applyResistance(double damage, boolean isMagic, double targetAR, double targetMR) {
        double resist = isMagic ? targetMR : targetAR;
        return damage / (1.0 + (resist / 100.0));
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

    public static boolean criticalChance(Player player, double playerCritChance) {
        return CritManager.getInstance().rollCrit(player, playerCritChance);
    }

    public static double getCritDamageMultiplier(Player player) {
        double bonus = PlayerStats.getOrCreate(player).getCritDamageBonus(player);
        return CRIT_DAMAGE_MULTIPLIER + bonus;
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

