package dev.ixpu.leaguemechanics.manager;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CritManager {

    private static final int MAX_STREAK = 5;

    private static final double[] LUCK_MODIFIER_TABLE = buildLuckModifierTable();

    private final Map<UUID, Integer> critFailureStreaks = new ConcurrentHashMap<>();

    private static final CritManager INSTANCE = new CritManager();

    public static CritManager getInstance() {
        return INSTANCE;
    }

    private static double[] buildLuckModifierTable() {
        double[] table = new double[201];
        for (int i = 0; i <= 200; i++) {
            double critChance = i / 2.0;
            table[i] = computeLuckModifier(critChance);
        }
        return table;
    }

    private static double computeLuckModifier(double critChance) {
        if (critChance <= 0) return 0;
        if (critChance >= 100) return 1;

        double C = critChance / 100.0;
        double a = C * (1 - C) * 2.0;
        return Math.max(0, Math.min(1, a));
    }

    public boolean rollCrit(Player player, double critChancePercent) {
        UUID uuid = player.getUniqueId();

        if (critChancePercent <= 0) {
            critFailureStreaks.put(uuid, 0);
            return false;
        }
        if (critChancePercent >= 100) {
            critFailureStreaks.put(uuid, 0);
            return true;
        }

        int failureStreak = critFailureStreaks.getOrDefault(uuid, 0);
        int cappedStreak = Math.min(failureStreak, MAX_STREAK);

        double cappedCritChance = Math.min(critChancePercent, 100.0);
        double maxPenalty = 0.20 * cappedCritChance;
        double penalty = Math.min(maxPenalty, 0.20 * cappedStreak);

        double effectiveCritChance = Math.max(0, cappedCritChance - penalty);
        boolean isCrit = Math.random() * 100 < effectiveCritChance;

        if (isCrit) {
            critFailureStreaks.put(uuid, 0);
        } else {
            critFailureStreaks.put(uuid, failureStreak + 1);
        }

        return isCrit;
    }

    public int getFailureStreak(Player player) {
        return critFailureStreaks.getOrDefault(player.getUniqueId(), 0);
    }

    public void resetFailureStreak(Player player) {
        critFailureStreaks.remove(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        critFailureStreaks.remove(player.getUniqueId());
    }

    public double getLuckModifierForPlayer(Player player, double critChancePercent) {
        int tableIndex = Math.min(200, Math.max(0, (int) Math.floor(critChancePercent * 2)));
        return LUCK_MODIFIER_TABLE[tableIndex];
    }

    public static double getAverageCritMultiplier(double critChancePercent, double bonusCritDamage) {
        double critChance = critChancePercent / 100.0;
        return 1.0 + (critChance * (1.0 + bonusCritDamage));
    }
}
