package dev.ixpu.leaguemechanics.player;

import org.bukkit.entity.Player;

import java.util.*;

public class PlayerOrigin {
    public static double getPlayerCountryBaseAS(Player player) {
        boolean isOp = player.isOp();

        if (isOp) {
            return 0.700;
        }

        if (hasPermission(player, "country-philippines")) return 0.694;
        if (hasPermission(player, "country-singapore")) return 0.684;
        if (hasPermission(player, "country-thailand")) return 0.672;
        if (hasPermission(player, "country-cambodia")) return 0.656;
        if (hasPermission(player, "country-vietnam")) return 0.652;
        if (hasPermission(player, "country-myanmar")) return 0.635;
        if (hasPermission(player, "country-malaysia")) return 0.620;
        if (hasPermission(player, "country-brunei")) return 0.617;
        if (hasPermission(player, "country-timor-leste")) return 0.607;
        if (hasPermission(player, "country-laos")) return 0.596;
        if (hasPermission(player, "country-indonesia")) return 0.400;

        return 0.700;
    }

    public static double getPlayerRuneRatioAS(Player player) {
        boolean isOp = player.isOp();

        if (isOp) {
            return 0.700;
        }

        if (hasPermission(player, "primary-rune-path.domination")) return 0.676;
        if (hasPermission(player, "primary-rune-path.precision")) return 0.587;
        if (hasPermission(player, "primary-rune-path.resolve")) return 0.694;
        if (hasPermission(player, "primary-rune-path.inspiration")) return 0.626;
        if (hasPermission(player, "primary-rune-path.sorcery")) return 0.687;

        return 0.700;
    }

    private static boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }
}