package dev.ixpu.leaguemechanics.player;

import org.bukkit.entity.Player;


public class PlayerClass {

    public static double getPlayerClassBaseAD(Player player) {
        if (hasPermission(player, "class-fighter")) return 10.0;
        if (hasPermission(player, "class-bruiser")) return 7.5;
        if (hasPermission(player, "class-assasin")) return 4.5;
        if (hasPermission(player, "class-mage")) return 0.0;
        if (hasPermission(player, "class-tank")) return 3.5;
        if (hasPermission(player, "class-marksman")) return 6.0;

        return 0;
    }

    public static double getPlayerClassBaseAP(Player player) {
        if (hasPermission(player, "class-fighter")) return 0.0;
        if (hasPermission(player, "class-bruiser")) return 0.0;
        if (hasPermission(player, "class-assasin")) return 2.5;
        if (hasPermission(player, "class-mage")) return 15.0;
        if (hasPermission(player, "class-tank")) return 2.0;
        if (hasPermission(player, "class-marksman")) return 0.0;

        return 0;
    }

    public static double getPlayerClassBaseHP(Player player) {
        if (hasPermission(player, "class-fighter")) return 0.0;
        if (hasPermission(player, "class-bruiser")) return 3.0;
        if (hasPermission(player, "class-assasin")) return 0.0;
        if (hasPermission(player, "class-mage")) return 0.0;
        if (hasPermission(player, "class-tank")) return 6.0;
        if (hasPermission(player, "class-marksman")) return 0.0;

        return 0;
    }

    public static double getPlayerClassBaseAR(Player player) {
        if (hasPermission(player, "class-fighter")) return 10.0;
        if (hasPermission(player, "class-bruiser")) return 0.0;
        if (hasPermission(player, "class-assasin")) return 0.0;
        if (hasPermission(player, "class-mage")) return 0.0;
        if (hasPermission(player, "class-tank")) return 25.0;
        if (hasPermission(player, "class-marksman")) return 5.0;

        return 0;
    }

    public static double getPlayerClassBaseMR(Player player) {
        if (hasPermission(player, "class-fighter")) return 0.0;
        if (hasPermission(player, "class-bruiser")) return 15.0;
        if (hasPermission(player, "class-assasin")) return 0.0;
        if (hasPermission(player, "class-mage")) return 0.0;
        if (hasPermission(player, "class-tank")) return 15.0;
        if (hasPermission(player, "class-marksman")) return 0.0;

        return 0;
    }

    public static double getPlayerClassBaseAS(Player player) {
        if (hasPermission(player, "class-fighter")) return 0.694; // Divided by Ratio = 1.10
        if (hasPermission(player, "class-bruiser")) return 0.684; // Divided by Ratio = 1.00
        if (hasPermission(player, "class-assasin")) return 0.672; // Divided by Ratio = 1.10
        if (hasPermission(player, "class-mage")) return 0.656; // Divided by Ratio = 1.05
        if (hasPermission(player, "class-tank")) return 0.652; // Divided by Ratio = 0.8
        if (hasPermission(player, "class-marksman")) return 0.635; // Divided by Ratio = 1.30

        return 0;
    }

    public static double getPlayerClassAttackSpeedRatio(Player player) {
        if (hasPermission(player, "class-fighter")) return 0.654;
        if (hasPermission(player, "class-bruiser")) return 0.664;
        if (hasPermission(player, "class-assasin")) return 0.612;
        if (hasPermission(player, "class-mage")) return 0.626;
        if (hasPermission(player, "class-tank")) return 0.856;
        if (hasPermission(player, "class-marksman")) return 0.500;

        return 0;
    }

    private static boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }
}