package dev.ixpu.leaguemechanics.manager;

import dev.ixpu.leaguemechanics.player.PlayerStats;

import org.bukkit.entity.Player;

public class BuffManager {

    public double calculateBuffValue(Player player, double baseValue, double adPercentageMultiplier, double apPercentageMultiplier) {
        PlayerStats stats = new PlayerStats();

        double totalAD = stats.getPlayerAD(player)  / 2;
        double totalAP = stats.getPlayerAP(player) / 4;

        double adBonus = totalAD * adPercentageMultiplier;
        double apBonus = totalAP * apPercentageMultiplier;

        return baseValue + adBonus + apBonus;
    }
}